import argparse
import errno
import json
import logging
import os
import struct
import sys
import threading
import time
from base64 import b64encode
from hashlib import sha1
from socket import error as SocketError, socket
from socketserver import StreamRequestHandler, TCPServer, ThreadingMixIn
from typing import Union, Tuple

'''
+-+-+-+-+-------+-+-------------+-------------------------------+
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-------+-+-------------+-------------------------------+
|F|R|R|R|op_code|M| Payload len |    Extended payload length    |
|I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
|N|V|V|V|       |S|             |   (if payload len==126/127)   |
| |1|2|3|       |K|             |                               |
+-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
|     Extended payload length continued, if payload len == 127  |
+ - - - - - - - - - - - - - - - +-------------------------------+
|                     Payload Data continued ...                |
+---------------------------------------------------------------+
'''

FIN = 0x80
OP_CODE = 0x0f
MASKED = 0x80
PAYLOAD_LEN = 0x7f
PAYLOAD_LEN_EXT16 = 0x7e
PAYLOAD_LEN_EXT64 = 0x7f

OP_CODE_CONTINUATION = 0x0
OP_CODE_TEXT = 0x1
OP_CODE_BINARY = 0x2
OP_CODE_CLOSE_CONN = 0x8
OP_CODE_PING = 0x9
OP_CODE_PONG = 0xA

# closing frame status codes.
STATUS_NORMAL = 1000
STATUS_GOING_AWAY = 1001
STATUS_PROTOCOL_ERROR = 1002
STATUS_UNSUPPORTED_DATA_TYPE = 1003
STATUS_STATUS_NOT_AVAILABLE = 1005
STATUS_ABNORMAL_CLOSED = 1006
STATUS_INVALID_PAYLOAD = 1007
STATUS_POLICY_VIOLATION = 1008
STATUS_MESSAGE_TOO_BIG = 1009
STATUS_INVALID_EXTENSION = 1010
STATUS_UNEXPECTED_CONDITION = 1011
STATUS_BAD_GATEWAY = 1014
STATUS_TLS_HANDSHAKE_ERROR = 1015

VALID_CLOSE_STATUS = (
    STATUS_NORMAL,
    STATUS_GOING_AWAY,
    STATUS_PROTOCOL_ERROR,
    STATUS_UNSUPPORTED_DATA_TYPE,
    STATUS_INVALID_PAYLOAD,
    STATUS_POLICY_VIOLATION,
    STATUS_MESSAGE_TOO_BIG,
    STATUS_INVALID_EXTENSION,
    STATUS_UNEXPECTED_CONDITION,
    STATUS_BAD_GATEWAY,
)


def define_log(log_dir_path: str, max_retain: int):
    log_dir_path = os.path.abspath(log_dir_path)
    if not os.path.exists(log_dir_path) or not os.path.isdir(log_dir_path):
        print('log path not exists and now create it: {}.'.format(log_dir_path))
        os.mkdir(log_dir_path)
    log_file_name = '{}.log'.format(time.strftime(
        "%Y-%m-%d %H-%M-%S", time.localtime()))
    log_file_path = os.path.abspath(os.path.join(log_dir_path, log_file_name))

    log_file_list = sorted(os.listdir(log_dir_path), reverse=True)
    for out_date_log_file_name in log_file_list[max_retain - 1:]:
        out_date_log_file_path = os.path.abspath(
            os.path.join(log_dir_path, out_date_log_file_name))
        print('too many log file, now delete one: {}'.format(
            out_date_log_file_path))
        os.remove(out_date_log_file_path)

    log_formatter = logging.Formatter(
        "%(asctime)s - %(name)s - [%(threadName)-12.12s] - [%(levelname)-5.5s] -  %(message)s")
    root_logger = logging.getLogger()
    root_logger.handlers = []  # 似乎原有的就有 console_handler 了
    file_handler = logging.FileHandler(log_file_path)
    file_handler.setFormatter(log_formatter)
    root_logger.addHandler(file_handler)
    # console_handler = logging.StreamHandler(sys.stdout)
    # console_handler.setFormatter(log_formatter)
    # root_logger.addHandler(console_handler)
    root_logger.setLevel(logging.NOTSET)


class WebsocketServer(ThreadingMixIn, TCPServer):
    """
        A websocket server waiting for clients to connect.

    Args:
        port(int): Port to bind to
        host(str): Hostname or IP to listen for connections. By default 127.0.0.1
            is being used. To accept connections from any client, you should use
            0.0.0.0.

    Properties:
        clients(list): A list of connected clients. A client is a dictionary
            like below.
                {
                 'id'      : id,
                 'handler' : handler,
                 'address' : (addr, port)
                }
    """
    TAG = "WebsocketServer"

    allow_reuse_address = True
    daemon_threads = True  # comment to keep threads alive until finished

    def __init__(self, port: int, host: str = '127.0.0.1'):
        TCPServer.__init__(self, (host, port), WebSocketHandler)
        self.port = self.socket.getsockname()[1]
        logging.debug("{}.__init__(port: {}, host: {})".format(self.TAG, self.port, host))
        self.new_client = None
        self.client_left = None
        self.message_received = None
        self.ping_received = None
        self.clients = dict()
        self.id_counter = -1

    def run_forever(self):
        try:
            logging.debug(
                "{}.run_forever - Listening on port {} for clients..".format(self.TAG, self.port))
            self.serve_forever()
        except KeyboardInterrupt:
            self.server_close()
            logging.debug("{}.run_forever - Server terminated.".format(self.TAG))
        except Exception as e:
            logging.error("{}.run_forever - Server exception: {}".format(self.TAG, e),
                          exc_info=True)
            exit(1)

    def _message_received_(self, handler, msg: str):
        logging.debug(
            "{}._message_received_(clientId: {}, msg: {})".format(self.TAG, handler.id, msg))
        if self.message_received is None:
            logging.debug(
                "{}._message_received_(clientId: {}, msg: {}) -- message_received is None".format(
                    self.TAG, handler.id, msg))
        elif handler.id not in self.clients.keys():
            logging.debug(
                "{}._message_received_(clientId: {}, msg: {}) -- client has been removed".format(
                    self.TAG, handler.id, msg))
        else:
            self.message_received(self.clients[handler.id], self, msg)

    def _ping_received_(self, handler, msg: str):
        logging.debug("{}._ping_received_(clientId: {}, msg: {})".format(self.TAG, handler.id, msg))
        handler.send_pong(msg)
        if self.ping_received is None:
            logging.debug(
                "{}._ping_received_(clientId: {}, msg: {}) -- ping_received is None".format(
                    self.TAG, handler.id, msg))
        elif handler.id not in self.clients.keys():
            logging.debug(
                "{}._ping_received_(clientId: {}, msg: {}) -- client has been removed".format(
                    self.TAG, handler.id, msg))
        else:
            self.ping_received(self.clients[handler.id], self, msg)

    def _pong_received_(self, handler, msg: str):
        logging.debug("{}._pong_received_(clientId: {}, msg: {})".format(self.TAG, handler.id, msg))

    def _new_client_(self, handler):
        self.id_counter += 1
        logging.debug("{}._new_client_(clientId: {}, clientAddress: {})".format(
            self.TAG, self.id_counter, handler.client_address))
        client = {
            'id': self.id_counter,
            'handler': handler,
            'address': handler.client_address
        }
        handler.id = self.id_counter
        self.clients[self.id_counter] = client
        if self.new_client is not None:
            self.new_client(client, self)
        else:
            logging.debug(
                '{}._new_client_(clientId: {}, clientAddress: {}) -- new_client is None'.format(
                    self.TAG, self.id_counter, handler.client_address))

    def _client_left_(self, handler):
        logging.debug("{}._client_left_(clientId: {}, clientAddress: {})".format(
            self.TAG, handler.id, handler.client_address))
        if handler.id in self.clients.keys():
            client = self.clients[handler.id]
        else:
            logging.debug(
                "{}._client_left_(clientId: {}, clientAddress: {}) -- client has been removed".format(
                    self.TAG, handler.id, handler.client_address))
            return
        if self.client_left is not None:
            self.client_left(client, self)
        else:
            logging.debug(
                '{}._client_left_(clientId: {}, clientAddress: {}) -- client_left is None'.format(
                    self.TAG, client['id'], handler.client_address))
        if handler.id in self.clients.keys():
            self.clients.pop(handler.id)

    def send_message(self, client: dict, msg: str):
        logging.debug("{}.send_message(clientId: {}, clientAddress: {}, msg: {})".format(
            self.TAG, client['id'], client['handler'].client_address, msg))
        client['handler'].send_message(msg)

    def send_message_to_all(self, msg: str):
        logging.debug("{}.send_message(msg: {})".format(self.TAG, msg))
        for client in self.clients:
            self.send_message(client, msg)

    def send_close(self, client: dict, status: int = STATUS_NORMAL, reason: str = "normal close"):
        client_id = client['id']
        client_handler = client['handler']
        logging.debug(
            "{}.send_close(clientId: {}, clientAddress: {}, reason: {}, status: {})".format(
                self.TAG, client_id, client_handler, reason, status))
        client_handler.send_close(status, reason)
        # self._client_left_(client_handler)


class WebSocketHandler(StreamRequestHandler):
    TAG = "WebSocketHandler"

    def __init__(self, sock: socket, addr: str, server: WebsocketServer):
        self.server = server
        self.keep_alive = True
        self.handshake_done = False
        self.valid_client = False
        self.sock = sock
        self.id = -1
        StreamRequestHandler.__init__(self, sock, addr, server)

    def handle(self):
        logging.debug("{} -- begin handle".format(self.TAG))
        while self.keep_alive:
            if not self.handshake_done:
                self.handshake()
            elif self.valid_client:
                self.read_next_message()
        logging.debug("{} -- finish handle".format(self.TAG))

    def read_bytes(self, num: int) -> bytes:
        result_bytes = self.rfile.read(num)
        logging.debug(
            "{} -- read_bytes(num: {}) - result_bytes: {}".format(self.TAG, num, result_bytes))
        return result_bytes

    def read_next_message(self):
        try:
            b1, b2 = self.read_bytes(2)
        except SocketError as ex:  # to be replaced with ConnectionResetError for py3
            if ex.errno == errno.ECONNRESET:
                logging.debug("{}.read_next_message -- Client closed connection.".format(self.TAG))
                self.keep_alive = False
                return
            logging.debug("{}.read_next_message -- socket error: {}".format(self.TAG, ex))
            b1, b2 = 0, 0
        except ValueError as ex:
            logging.debug("{}.read_next_message -- other error: {}".format(self.TAG, ex))
            b1, b2 = 0, 0

        fin = b1 & FIN
        op_code = b1 & OP_CODE
        masked = b2 & MASKED
        payload_length = b2 & PAYLOAD_LEN
        logging.debug(
            "{}.read_next_message -- fin: {}, op_code: {}, masked: {}, payload_length: {}".format(
                self.TAG, fin, op_code, masked, payload_length))

        if op_code == OP_CODE_CLOSE_CONN:
            logging.debug(
                "{}.read_next_message -- Client asked to close connection.".format(self.TAG))
            self.keep_alive = False
            return
        if not masked:
            logging.warning(
                "{}.read_next_message -- Client must always be masked.".format(self.TAG))
            self.keep_alive = False
            return
        if op_code == OP_CODE_CONTINUATION:
            logging.warning(
                "{}.read_next_message -- Continuation frames are not supported.".format(self.TAG))
            return
        if op_code == OP_CODE_BINARY:
            logging.warning(
                "{}.read_next_message -- Binary frames are not supported.".format(self.TAG))
            return
        if op_code == OP_CODE_TEXT:
            op_code_handler = self.server._message_received_
        elif op_code == OP_CODE_PING:
            op_code_handler = self.server._ping_received_
        elif op_code == OP_CODE_PONG:
            op_code_handler = self.server._pong_received_
        else:
            logging.warning("%s.read_next_message -- Unknown op_code %#x." % (self.TAG, op_code))
            self.keep_alive = False
            return

        if payload_length == 126:
            payload_length = struct.unpack(">H", self.rfile.read(2))[0]
        elif payload_length == 127:
            payload_length = struct.unpack(">Q", self.rfile.read(8))[0]

        masks = self.read_bytes(4)
        message_bytes = bytearray()
        for message_byte in self.read_bytes(payload_length):
            message_byte ^= masks[len(message_bytes) % 4]
            message_bytes.append(message_byte)
        logging.debug("{}.read_next_message -- message_bytes: {}".format(self.TAG, message_bytes))
        op_code_handler(self, message_bytes.decode('utf8'))

    def send_message(self, message: str):
        self.send_text(message)

    def send_pong(self, message: str):
        self.send_text(message, OP_CODE_PONG)

    def send_close(self, status: int = STATUS_NORMAL, reason: str = "normal close"):
        if status < 0 or status >= 1 << 16:
            raise ValueError("code is invalid range")
        if self.keep_alive:
            self.keep_alive = False
            self.send_text(struct.pack('!H', status) + bytes(reason, 'utf8'), OP_CODE_CLOSE_CONN)

    def send_text(self, message: Union[str, bytes], op_code: int = OP_CODE_TEXT):
        if not isinstance(message, (bytes, str)):
            logging.warning(
                '{}.send_text -- Can\'t send message, message has to be a string or bytes. Given type is {}'
                .format(self.TAG, type(message)))
            return
        header = bytearray()
        payload = encode_to_utf8(message) if isinstance(message, str) else message
        payload_length = len(payload)
        header.append(FIN | op_code)

        # Normal payload
        if payload_length <= 125:
            header.append(payload_length)
        # Extended payload
        elif 126 <= payload_length <= 65535:
            header.append(PAYLOAD_LEN_EXT16)
            header.extend(struct.pack(">H", payload_length))
        # Huge extended payload
        elif payload_length < 18446744073709551616:
            header.append(PAYLOAD_LEN_EXT64)
            header.extend(struct.pack(">Q", payload_length))
        else:
            ex = Exception("Message is too big. Consider breaking it into chunks.")
            logging.warning('{}.send_text -- payload exception: {}'.format(self.TAG, ex))
            raise ex

        self.request.send(header + payload)

    def read_http_headers(self) -> dict:
        headers = {}
        # first line should be HTTP GET
        http_get = self.rfile.readline().decode().strip()
        assert http_get.upper().startswith('GET')
        # remaining should be headers
        while True:
            header = self.rfile.readline().decode().strip()
            if not header:
                break
            head, value = header.split(':', 1)
            headers[head.lower().strip()] = value.strip()
        logging.debug("{}.read_http_headers -- headers: {}".format(self.TAG, headers))
        return headers

    def handshake(self):
        logging.debug("{}.handshake".format(self.TAG))
        headers = self.read_http_headers()

        try:
            assert headers['upgrade'].lower() == 'websocket'
        except AssertionError as ex:
            self.keep_alive = False
            logging.warning('{}.handshake -- AssertionError: {}'.format(self.TAG, ex))
            return

        try:
            key = headers['sec-websocket-key']
        except KeyError as ex:
            logging.warning(
                "{}.handshake -- Client tried to connect but was missing a key -- {}".format(
                    self.TAG, ex))
            self.keep_alive = False
            return

        response = self.make_handshake_response(key)
        self.handshake_done = self.request.send(response.encode())
        self.valid_client = True
        self.server._new_client_(self)

    @classmethod
    def make_handshake_response(cls, key: str) -> str:
        logging.debug("{}.make_handshake_response(key: {})".format(cls.TAG, key))
        return \
            'HTTP/1.1 101 Switching Protocols\r\n' \
            'Upgrade: websocket\r\n' \
            'Connection: Upgrade\r\n' \
            'Sec-WebSocket-Accept: %s\r\n' \
            '\r\n' % cls.calculate_response_key(key)

    @classmethod
    def calculate_response_key(cls, key: str) -> bytes:
        guid = '258EAFA5-E914-47DA-95CA-C5AB0DC85B11'
        hash_value = sha1(key.encode() + guid.encode())
        response_key = b64encode(hash_value.digest()).strip().decode('ASCII')
        logging.debug("{}.calculate_response_key(key: {}) -- response_key: {}".format(cls.TAG, key,
                                                                                      response_key))
        return response_key

    def finish(self):
        logging.debug("{}.finish".format(self.TAG))
        self.server._client_left_(self)


def encode_to_utf8(data: str) -> Union[bytes, bool]:
    try:
        return data.encode('UTF-8')
    except UnicodeEncodeError as e:
        logging.error("unicode - Could not encode data(msg: {}) to UTF-8 -- {}".format(data, e))
        return False
    except Exception as e:
        logging.error("unicode - Other exception while encode data(msg: {}) -- {}".format(data, e))
        raise e


def try_decode_utf8(data: bytes) -> Union[str, bool]:
    try:
        return data.decode('utf-8')
    except UnicodeDecodeError as e:
        logging.error("unicode - Could not encode data(msg: {}) to UTF-8 -- {}".format(data, e))
        return False
    except Exception as e:
        logging.error("unicode - Other exception while decode data(msg: {}) -- {}".format(data, e))
        raise e


class RepeatingTimer(threading.Timer):

    def __init__(self, interval: int, function, log_interval: int = 1):
        self.finished = None
        self.interval = interval
        self.function = function
        self.args = None
        self.kwargs = None
        super(RepeatingTimer, self).__init__(interval, function)
        self.log_interval = log_interval
        self.log_counter = 0

    def run(self):
        temp = self.log_interval * self.interval
        while not self.finished.is_set():
            self.finished.wait(self.interval)
            self.function(*self.args, **self.kwargs)
            self.log_counter += 1
            if self.log_counter >= self.log_interval:
                logging.debug('RepeatingTimer: run -- wait {}s and log {}s -- execute {}'.format(
                    self.interval, temp, self.function))
                self.log_counter = 0


class RemoteShellServer:
    TAG = "RemoteShellServer"

    def __init__(self, port: int, host: str, check_timeout: int = 30, log_interval: int = 30):
        self.mobile_clients = dict()
        self.controller_clients = dict()
        self.labels = set()
        """
        clients中的client构成
        (-1/对应clientId, uid, guid, time)
        """
        # repeat check
        self.check_timeout = check_timeout + 2
        temp = 10
        self.check_timer = RepeatingTimer(temp, self.check, log_interval)
        self.check_timer.daemon = True
        self.check_timer.start()
        self.log_interval = log_interval
        self.log_counter = 0
        self.log_time_interval = self.log_interval * temp
        # remote server
        self.remote_server = WebsocketServer(port, host)
        self.remote_server.new_client = self.new_client
        self.remote_server.client_left = self.client_left
        self.remote_server.message_received = self.message_received
        self.remote_server.ping_received = self.ping_received

    def new_client(self, client: dict, server: WebsocketServer):
        msg = "New client connected and was given id %d" % client['id']
        # print(msg)
        logging.debug("{}.new_client -- {}".format(self.TAG, msg))
        server.send_message(client, '{"msg":"connect successfully"}')

    def client_left(self, client: dict, server: WebsocketServer):
        msg = "Client(%d) disconnected" % client['id']
        # print(msg)
        logging.debug("{}.client_left -- {}".format(self.TAG, msg))
        client_id = client['id']
        self.remove_client(client_id, client_id in self.controller_clients.keys())

    def message_received(self, client: dict, server: WebsocketServer, message: str):
        msg = "Client(%d) said: %s" % (client['id'], message)
        # print(msg)
        logging.debug("{}.message_received -- {}".format(self.TAG, msg))
        server.send_message(client, message)
        try:
            msg_dict = json.loads(message)
        except json.JSONDecodeError as ex:
            logging.error(
                "{}.message_received -- JSONDecodeError: {}; message are: {}".format(self.TAG, ex,
                                                                                     message))
            return
        except Exception as ex:
            logging.error(
                "{}.message_received -- Exception: {}; message are: {}".format(self.TAG, ex,
                                                                               message))
            return

        client_id = client['id']
        controller_flag = client_id in self.controller_clients.keys()
        try:
            if "UserId" in msg_dict:
                self.add_client(msg_dict, client, server)
            elif "type" in msg_dict:
                self.deal_with_command(client_id, controller_flag, msg_dict, message)
            else:
                logging.debug("{}.message_received -- invalid message: {}; from client: {}".format(
                    self.TAG, message, client_id))
        except KeyError as ex:
            logging.error("{}.message_received -- KeyError: {}, client_id: {}".format(self.TAG, ex,
                                                                                      client_id))
            # server.send_close(client)
            self.remove_client(client_id, controller_flag)
        except Exception as ex:
            logging.error("{}.message_received -- Exception: {}, client_id: {}".format(self.TAG, ex,
                                                                                       client_id))
            # server.send_close(client)
            self.remove_client(client_id, controller_flag)

    def deal_with_command(self, client_id: int, controller_flag: bool, msg_dict: dict,
                          message: str):
        msg1, msg2, first, second = self._controller_flag(controller_flag)
        first_client = first[client_id]
        second_id = first_client[0]
        if second_id in second.keys():
            second_client = second[second_id]
            self.remote_server.send_message(second_client[-1], message)
            logging.debug("{}.deal_with_command -- {}({}) to {}({})".format(
                self.TAG, msg1, first_client, msg2, second_client))
        else:
            logging.debug("{}.deal_with_command -- {}({}) has no correspond {} client".format(
                self.TAG, msg1, first_client, msg2))
            self.remove_client(client_id, controller_flag)

    def add_client(self, msg_dict: dict, client: dict, server: WebsocketServer):
        uid = msg_dict["UserId"]
        guid = msg_dict["Guid"]
        flag = "Web" in msg_dict.keys()
        label = ('{}--{}--Web' if flag else '{}--{}').format(uid, guid)
        if label in self.labels:
            logging.debug("{}.add_client -- client({}) already exists".format(self.TAG, label))
            server.send_close(client)
            return
        if flag:
            # TODO: 如果有推送系统和用户系统，则依靠推送系统找到对应用户，否则就像现在这样，假想用户已被找到并加入了这个server的列表
            # TODO: 建立通用的用户系统 -- android 和 后台
            # TODO: 建立通用的推送系统 -- android 和 后台
            server.send_message(client, '{"msg":"connect client successfully"}')
            target_clients = self.controller_clients
        else:
            correspond_label = label + '--Web'
            if correspond_label not in self.labels:
                logging.debug(
                    "{}.add_client -- client({}): no corresponding client".format(self.TAG, label))
                server.send_close(client)
                return
            target_clients = self.mobile_clients
        client_id = client['id']
        target_client = [-1, uid, guid, time.time(), client]
        if client_id in target_clients.keys():
            logging.debug(
                "{}.add_client -- client({}) already exists".format(self.TAG, target_client))
            server.send_close(client)
            return
        self.labels.add(label)
        target_clients[client_id] = target_client
        logging.debug("{}.add_client -- client({}) are added".format(self.TAG, label))
        if not flag:
            for key, controller in self.controller_clients.items():
                if controller[1] == uid and controller[2] == guid:
                    controller[0] = client_id
                    target_clients[client_id][0] = key
                    logging.debug(
                        "{}.add_client -- client({}) to client({})".format(self.TAG, client_id,
                                                                           key))
                    break

    def remove_client(self, client_id: int, controller_flag: bool):
        msg1, msg2, first, second = self._controller_flag(controller_flag)
        label1, label2 = ('{}--{}--Web', '{}--{}') if controller_flag else ('{}--{}', '{}--{}--Web')
        second_id = -1
        if client_id in first.keys():
            second_id = first[client_id][0]
            remote_client = first.pop(client_id)
            logging.debug(
                "{}.remove_client -- remove {} client: {}".format(self.TAG, msg1, remote_client))
            self.remote_server.send_close(remote_client[-1])
            self.labels.remove(label1.format(remote_client[1], remote_client[2]))
        else:
            logging.debug("{}.remove_client -- no such client_id: {}".format(self.TAG, client_id))
        if second_id != -1 and second_id in second.keys():
            remote_client = second.pop(second_id)
            self.remote_server.send_close(remote_client[-1])
            logging.debug(
                "{}.remove_client -- remove {} client: {}".format(self.TAG, msg2, remote_client))
            self.labels.remove(label2.format(remote_client[1], remote_client[2]))
        else:
            logging.debug("{}.remove_client -- no such client_id: {}".format(self.TAG, second_id))

    def ping_received(self, client: dict, server: WebsocketServer, message: str):
        client_id = client['id']
        now = time.time()
        logging.warning(
            "{}.ping_received -- client_id: {}, now: {}".format(self.TAG, client_id, now))
        if client_id in self.controller_clients.keys():
            self.controller_clients[client_id][3] = now
        elif client_id in self.mobile_clients.keys():
            self.mobile_clients[client_id][3] = now
        else:
            logging.warning("{}.ping_received -- invalid client_id: {}".format(self.TAG, client_id))

    def check(self):
        now = time.time()
        self.log_counter += 1
        if self.log_counter >= self.log_interval:
            logging.debug("{}.check -- begin checking at time: {}".format(self.TAG, now))
        self._iter_del_clients(True, now)
        self._iter_del_clients(False, now)
        if self.log_counter >= self.log_interval:
            logging.debug("{}.check -- finish checking at time: {}".format(self.TAG, now))
            self.log_counter = 0

    def _iter_del_clients(self, controller_flag: bool, now: float):
        msg1, msg2, first, second = self._controller_flag(controller_flag)
        for key in list(first.keys()):
            client = first[key]
            second_id = client[0]
            if second_id not in second.keys() or now - client[3] > self.check_timeout:
                self.remove_client(key, True)
                logging.debug(
                    "{}.check -- remove {} client: {}, client_id: {}".format(self.TAG, msg1, client,
                                                                             key))

    def _controller_flag(self, controller_flag: bool) -> Tuple[str, str, dict, dict]:
        if controller_flag:
            return 'controller', 'mobile', self.controller_clients, self.mobile_clients
        else:
            return 'mobile', 'controller', self.mobile_clients, self.controller_clients

    def start(self):
        self.remote_server.run_forever()


# TODO: 通用的 Channel WebSocket Server，为一些Client分类，分通道
if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="WebSocket Simple Server Tool", add_help=True)
    parser.add_argument("-ho", "--host", type=str, default='172.18.33.203',
                        help="Websocket host. ex. 172.18.33.203")
    parser.add_argument("-p", "--port", type=int, default=9001, help="Port for websocket listening")
    parser.add_argument("-l", "--log_files", type=str, default="./server_logs",
                        help="Folder for log files")
    args = parser.parse_args()
    print('parse_args: host({}), port({}), log_files({})'.format(
        args.host, args.port, args.log_files))

    define_log(args.log_files, 10)
    RemoteShellServer(args.port, args.host).start()

# pyinstaller -F .\remote_server.py
