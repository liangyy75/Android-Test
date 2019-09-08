"""
@author liangyuyin
@since 2019/7/24
@update 2019/8/8
"""

import argparse
import code
import json
import logging
import os
import ssl
import sys
import threading
import time

import websocket
from six.moves.urllib.parse import urlparse

TEST_FLAG = True
TEST_UID = 50042533
TEST_GUID = '0e74af97aa48135d0c5528db29dbb6fe'


def define_log(log_dir_path: str):
    log_dir_path = os.path.abspath(log_dir_path)
    if not os.path.exists(log_dir_path) or not os.path.isdir(log_dir_path):
        print('log path not exists and now create it: {}.'.format(log_dir_path))
        os.mkdir(log_dir_path)
    log_file_name = '{}.log'.format(time.strftime(
        "%Y-%m-%d %H-%M-%S", time.localtime()))
    log_file_path = os.path.abspath(os.path.join(log_dir_path, log_file_name))
    # if not os.path.exists(log_file_name) or not os.path.isfile(log_file_name):
    #     print('log file not exists and now create it: {}.'.format(log_file_name))
    #     open(log_file_path).close()

    log_file_list = sorted(os.listdir(log_dir_path), reverse=True)
    for out_date_log_file_name in log_file_list[5:]:
        out_date_log_file_path = os.path.abspath(
            os.path.join(log_dir_path, out_date_log_file_name))
        print('too many log file, now delete one: {}'.format(
            out_date_log_file_path))
        os.remove(out_date_log_file_path)

    log_formatter = logging.Formatter(
        "%(asctime)s - %(name)s - [%(threadName)-12.12s] - [%(levelname)-5.5s] -  %(message)s")
    root_logger = logging.getLogger()
    root_logger.handlers = []
    file_handler = logging.FileHandler(log_file_path)
    file_handler.setFormatter(log_formatter)
    root_logger.addHandler(file_handler)
    root_logger.setLevel(logging.NOTSET)


def get_encoding():
    encoding = getattr(sys.stdin, "encoding", '')
    encoding = "utf-8" if not encoding else encoding.lower()
    logging.debug('get_encoding: {}'.format(encoding))
    return encoding


OP_CODE_DATA = (websocket.ABNF.OPCODE_TEXT, websocket.ABNF.OPCODE_BINARY)
ENCODING = get_encoding()


class InteractiveConsole(code.InteractiveConsole):
    tag = 'InteractiveConsole'

    def __init__(self):
        super(InteractiveConsole, self).__init__()
        self.connect = False

    def write(self, data: str):
        logging.debug('{}: write(data: {})'.format(self.tag, data))
        data = json.loads(data)
        if 'type' in data.keys():
            result = parse_result(data['type'], data['data'])
            if result is not None:
                sys.stdout.transform('{}\n> '.format(result))
                sys.stdout.flush()
        elif 'msg' in data.keys() and data['msg'] == 'connect client successfully':
            self.connect = True

    def read(self):
        line = sys.stdin.readline()
        logging.debug('{}: read(line: {})'.format(self.tag, line))
        return line


class VAction(argparse.Action):

    def __call__(self, parser, args, values, option_string=None):
        if values is None:
            values = "1"
        try:
            values = int(values)
        except ValueError:
            values = values.count("v") + 1
        setattr(args, self.dest, values)


def parse_args():
    parser = argparse.ArgumentParser(
        description="WebSocket Simple Dump Tool", add_help=True)
    parser.add_argument("-url", metavar="--ws_url", default='ws://172.18.33.203:9001',
                        help="Websocket url. ex. ws://172.18.33.203:9001")
    parser.add_argument("-u", "--userId", type=int,
                        help="User id for connecting")
    parser.add_argument("-g", "--guid", type=str, help="Guid for connecting")
    parser.add_argument("-t", "--type", type=str,
                        default='shell', help="Type for remote shell command.")
    parser.add_argument("-p", "--proxy",
                        help="Proxy url. ex. http://127.0.0.1:8080")
    parser.add_argument("-v", "--verbose", default=0, nargs='?', action=VAction, dest="verbose",
                        help="Set verbose mode. If set to 1, show op_code. If set to 2, enable "
                             "to trace websocket module")
    parser.add_argument("--eof-wait", default=0, type=int,
                        help="Wait time(second) after 'EOF' received.")
    parser.add_argument("--timings", action="store_true",
                        help="Print timings in seconds")
    args = parser.parse_args()
    if TEST_FLAG:
        if args.userId is None:
            args.userId = TEST_UID
        if args.guid is None:
            args.guid = TEST_GUID
    logging.debug(
        'parse_args: url({}), userId({}), guid({}), type({}), proxy({}), verbose({}), eof-wait({}), timings({})'.format(
            args.url, args.userId, args.guid, args.type, args.proxy, args.verbose, args.eof_wait,
            args.timings))
    return args


def parse_command(_type, message):
    if _type == "shell":
        result = {"type": _type, "data": {"shell": [message]}}
    elif _type == "testReq":
        result = {"type": _type, "data": {"code": message}}
    else:
        raise ValueError('_type\' value is wrong')
    result = json.dumps(result)
    logging.debug(
        'parse_command(_type: {}, message: {}) -> result: {}'.format(_type, str(message), result))
    return result


def parse_result(_type, message: dict) -> str:
    s_msg = str(message)
    if _type == 'output' and 'content' in message.keys():
        result = message['content']
    elif _type == 'testRes' and 'result' in message.keys():
        result = message['result']
    else:
        # result = s_msg
        result = None
    logging.debug(
        'parse_result(_type: {}, message: {}) -> result: {}'.format(_type, s_msg, result).strip())
    return result


class RepeatingTimer(threading.Timer):
    tag = 'RepeatingTimer'

    def run(self):
        while not self.finished.is_set():
            self.finished.wait(self.interval)
            self.function(*self.args, **self.kwargs)
            logging.debug(
                '{}: run -- wait {}s -- execute {}'.format(self.tag, self.interval, self.function))


def main():
    tag = 'main'
    start_time = time.time()
    args = parse_args()
    if args.verbose > 1:
        websocket.enableTrace(True)
    options = {}
    if args.proxy:
        p = urlparse(args.proxy)
        options["http_proxy_host"] = p.hostname
        options["http_proxy_port"] = p.port
    if not args.userId:
        msg = 'User Id should not be empty.'
        print(msg)
        logging.debug('{}: {}'.format(tag, msg))
        return
    if not args.guid:
        msg = 'Guid should not be empty.'
        print(msg)
        logging.debug('{}: {}'.format(tag, msg))
        return
    opts = {"cert_reqs": ssl.CERT_NONE, "check_hostname": False}
    ws = websocket.create_connection(args.url, sslopt=opts, **options)
    console = InteractiveConsole()

    def recv():
        try:
            frame = ws.recv_frame()
        except websocket.WebSocketException as ex:
            logging.error('{}: recv\'s websocket.WebSocketException: {}'.format(tag, str(ex)))
            return websocket.ABNF.OPCODE_CLOSE, None
        if not frame:
            raise websocket.WebSocketException("Not a valid frame %s" % frame)
        elif frame.opcode in OP_CODE_DATA:
            return frame.opcode, frame.data
        elif frame.opcode == websocket.ABNF.OPCODE_CLOSE:
            ws.send_close()
            logging.error('{}: recv\'s send_close'.format(tag))
            return frame.opcode, None
        elif frame.opcode == websocket.ABNF.OPCODE_PING:
            ws.pong(frame.data)
            logging.error('{}: recv\'s pong'.format(tag))
        return frame.opcode, frame.data

    def recv_ws():
        logging.debug('{}: recv\'s thread start'.format(tag))
        while True:
            op_code, data = recv()
            logging.debug('{}: recv() -> op_code: {}, data: {}.'.format(tag, op_code, data))
            write_msg = None
            if op_code == websocket.ABNF.OPCODE_BINARY and isinstance(data, bytes):
                data = str(data, "utf-8")
            if op_code in OP_CODE_DATA:
                logging.debug('{}: recv result -- write: {}.'.format(tag, write_msg))
                write_msg = data
            if write_msg is not None:
                if args.timings:
                    console.write(str(time.time() - start_time) + ": " + write_msg)
                else:
                    console.write(write_msg)
            if op_code == websocket.ABNF.OPCODE_CLOSE:
                break
        logging.debug('{}: recv\'s thread finish'.format(tag))

    def ping():
        try:
            ws.ping()
            logging.debug('{}: ping'.format(tag))
        except Exception as ex:
            str_ex = str(ex)
            logging.error('{}: ping\'s Exception: {}'.format(tag, str_ex))
            print(str_ex)

    logging.debug('{}: start recv_ws\'s thread.'.format(tag))
    thread = threading.Thread(target=recv_ws)
    thread.daemon = True
    thread.start()

    logging.debug('{}: start ping\'s thread.'.format(tag))
    timer = RepeatingTimer(30, ping)  # threading.Timer(30, ping)
    timer.daemon = True
    timer.start()

    connect_msg = '{"UserId":' + str(
        args.userId) + ',"Web":"","Guid":"' + str(args.guid) + '"}'
    logging.debug('{}: begin connect -- send: {}.'.format(tag, connect_msg))
    begin_connect = time.time()
    ws.send(connect_msg)
    while True:
        time.sleep(3)
        if time.time() - begin_connect > 10:
            sys.stdout.transform('cannot connect client.\n')
            sys.stdout.flush()
            logging.debug('{}: connect timeout.'.format(tag))
            sys.exit(0)
        if console.connect:
            logging.debug('{}: connect successfully!.'.format(tag))
            break
        ws.send(connect_msg)
        logging.debug('{}: re_connect -- send: {}.'.format(tag, connect_msg))

    print("Press Ctrl+C to quit or input 'exit' to quit.\n> ", end='')
    while True:
        logging.debug('{}: begin processing command'.format(tag))
        try:
            message = console.read().strip()
            if message == 'exit':
                ws.close()
                logging.debug('{}: exit by user'.format(tag))
                break
            logging.debug('{}: send command: {}'.format(tag, message))
            message = parse_command(args.type, message)
            ws.send(message)
        except KeyboardInterrupt as ex:
            logging.error('{}: KeyboardInterrupt -- {}'.format(tag, ex))
            return
        except EOFError as ex:
            time.sleep(args.eof_wait)
            logging.error('{}: eof_wait -- {}, EOFError -- {}'.format(tag, args.eof_wait, ex))
            return
    logging.debug('{}: finish processing command'.format(tag))


if __name__ == "__main__":
    define_log('./cmd_logs/')
    try:
        logging.debug('app start')
        main()
        logging.debug('app end')
    except Exception as e:
        str_e = str(e)
        print(str_e)
        logging.error('app terminate: {}'.format(str_e))

# pyinstaller -F .\remote_cmd_controller.py
