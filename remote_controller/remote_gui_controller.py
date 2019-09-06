"""
@author liangyuyin
@since 2019/7/30
@update 2019/8/8
"""
# [logger configuration to log to file and print to stdout]
# (https://stackoverflow.com/questions/13733552/logger-configuration-to-log-to-file-and-print-to-stdout)
# [Python logging 日志](https://docs.python.org/zh-cn/3/howto/logging.html)
# [Python websocket-client](https://pypi.org/project/websocket_client/)
import json
import logging
import os
import ssl
import sys
import threading
import time
from abc import abstractmethod, ABCMeta
from typing import List, Tuple, Union

import websocket
from PyQt5 import QtCore, QtGui, QtWidgets

# TODO: 多线程安全问题 -- logging && q_thread && timer -- threading.RLock
# TODO: 过于频繁的操作似乎会导致 <b>crash<b>


TEST_FLAG = True
TEST_UID = 50042533
TEST_GUID = '0e74af97aa48135d0c5528db29dbb6fe'


def define_log(log_dir_path: str, max_retain: int):
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
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setFormatter(log_formatter)
    root_logger.addHandler(console_handler)
    root_logger.setLevel(logging.NOTSET)


def _define_widget(target: Union[QtWidgets.QWidget, QtWidgets.QLineEdit, QtWidgets.QLabel], geometry: List[
    int], font: QtGui.QFont, object_name: str) -> Union[QtWidgets.QWidget, QtWidgets.QLineEdit, QtWidgets.QLabel]:
    target.setGeometry(QtCore.QRect(*geometry))
    target.setFont(font)
    target.setObjectName(object_name)
    logging.debug('finish defining widget: {}'.format(object_name))
    return target


def _define_label(target: QtWidgets.QLabel, geometry: List[
    int], font: QtGui.QFont, tool_tip_duration: int, object_name: str) -> QtWidgets.QLabel:
    target.setToolTipDuration(tool_tip_duration)
    return _define_widget(target, geometry, font, object_name)


def _define_edit(target: QtWidgets.QLineEdit, geometry: List[
    int], font: QtGui.QFont, text: str, object_name: str) -> QtWidgets.QLineEdit:
    target.setText(text)
    return _define_widget(target, geometry, font, object_name)


class FocusLabel(QtWidgets.QLabel):
    tag = 'FocusLabel'

    def __init__(self, parent: QtWidgets.QWidget = None, target: QtWidgets.QWidget = None):
        super(FocusLabel, self).__init__(parent)
        self.target = target
        self.name1 = self.objectName()
        self.name2 = self.target.objectName() if self.target is not None else ''
        logging.debug('{}: init'.format(self.tag))

    def set_target(self, target: QtWidgets.QWidget) -> None:
        self.target = target
        self.name2 = self.target.objectName()
        logging.debug('{}: {} -- set target -- {}'.format(
            self.tag, self.name1, self.name2))

    def mousePressEvent(self, ev: QtGui.QMouseEvent) -> None:
        if self.target is not None:
            self.target.setFocus()
            logging.debug('{}: {} -- set focus -- {}'.format(
                self.tag, self.name1, self.name2))
        else:
            logging.debug(
                '{}: cannot set focus, target is None'.format(self.tag))


class HistoryLineEdit(QtWidgets.QLineEdit):
    tag = 'HistoryLineEdit'

    def __init__(self, parent: QtWidgets.QWidget = None, retain_num: int = 30):
        super(HistoryLineEdit, self).__init__(parent)
        self.history = []
        self.current_index = -1
        self.retain_text = ''
        self.retain_num = retain_num
        logging.debug('{}: init'.format(self.tag))

    def keyPressEvent(self, event: QtGui.QKeyEvent):
        key = event.key()
        if key == 16777220:  # QtCore.Qt.Key_Enter
            logging.debug('{}: keyPressEvent, event.key: {}, meaning: {}, history: {}'.format(
                self.tag, key, 'enter', self.history))
            text = self.text()
            self.history = list(
                filter(lambda x: x.lower() != text.lower(), self.history))
            self.history.append(text)
            len_history = len(self.history)
            retain_index = len_history - self.retain_num if len_history > self.retain_num else 0
            self.history = self.history[retain_index:]
            self.current_index = -1
        elif key == 16777235:  # QtCore.Qt.Key_Up
            logging.debug('{}: keyPressEvent, event.key: {}, meaning: {}, history: {}, current_index: {}'.format(
                self.tag, key, 'up', self.history, self.current_index))
            if len(self.history) == 0:
                return
            if self.current_index == -1:
                self.retain_text = self.text()
                self.current_index = len(self.history) - 1
                self.setText(self.history[self.current_index])
            elif self.current_index > 0:
                self.current_index -= 1
                self.setText(self.history[self.current_index])
            elif self.current_index == 0:
                self.current_index = -1
                self.setText(self.retain_text)
            return
        elif key == 16777237:  # QtCore.Qt.Key_Down
            logging.debug('{}: keyPressEvent, event.key: {}, meaning: {}, history: {}, current_index: {}'.format(
                self.tag, key, 'down', self.history, self.current_index))
            if len(self.history) == 0:
                return
            if self.current_index == -1:
                self.retain_text = self.text()
                self.current_index = 0
                self.setText(self.history[self.current_index])
            elif self.current_index < len(self.history) - 1:
                self.current_index += 1
                self.setText(self.history[self.current_index])
            elif self.current_index == len(self.history) - 1:
                self.current_index = -1
                self.setText(self.retain_text)
            return
        else:
            self.current_index = -1
        return QtWidgets.QLineEdit.keyPressEvent(self, event)


def get_encoding():
    encoding = getattr(sys.stdin, "encoding", '')
    return "utf-8" if not encoding else encoding.lower()


OP_CODE_DATA = (websocket.ABNF.OPCODE_TEXT, websocket.ABNF.OPCODE_BINARY)
ENCODING = get_encoding()


class RepeatingTimer(threading.Timer):
    tag = 'RepeatingTimer'

    def run(self):
        while not self.finished.is_set():
            self.finished.wait(self.interval)
            self.function(*self.args, **self.kwargs)
            logging.debug('{}: run -- wait {}s -- execute {}'.format(
                self.tag, self.interval, self.function))


class AbsMsgController(metaclass=ABCMeta):

    def __init__(self, req_type: str, res_type: str):
        self.req_type = req_type
        self.res_type = res_type

    @abstractmethod
    def parse_command(self, command: str) -> dict:
        pass

    @abstractmethod
    def parse_result(self, result: dict) -> str:
        pass


class ShellMsgController(AbsMsgController):

    def __init__(self):
        super(ShellMsgController, self).__init__("shell", "output")

    def parse_command(self, command: str) -> dict:
        return {"shell": [command]}

    def parse_result(self, result: dict) -> str:
        return result['content'] if 'content' in result.keys() else ''


class TestMsgController(AbsMsgController):

    def __init__(self):
        super(TestMsgController, self).__init__("testReq", "testRes")

    def parse_command(self, command: str) -> dict:
        return {"command": command}

    def parse_result(self, result: dict) -> str:
        return result['command'] if 'command' in result.keys() else ''


msg_controllers = [ShellMsgController(), TestMsgController()]


def parse_command(req_type, message: str) -> str:
    result = None
    for msg_controller in msg_controllers:
        if msg_controller.req_type == req_type:
            result = {"type": req_type, "data": msg_controller.parse_command(message)}
    if result is None:
        raise ValueError('_type\'s value is wrong')
    result = json.dumps(result)
    logging.debug('parse_command(_type: {}, message: {}) -> result: {}'.format(
        req_type, str(message), result))
    return result


def parse_result(res_type, message: dict) -> str:
    s_msg = str(message)
    result = ''
    for msg_controller in msg_controllers:
        if msg_controller.res_type == res_type:
            result = msg_controller.parse_result(message)
    logging.debug('parse_result(_type: {}, message: {}) -> result: {}'.format(
        res_type, s_msg, result).strip())
    return result


class MessageHandler:
    tag = 'MessageHandler'

    def __init__(self):
        self.connect = False
        logging.debug('{}: init'.format(self.tag))

    def transform(self, str_data: str, data_type: str = 'json') -> str:
        msg = ''
        if data_type == 'json':
            try:
                data = json.loads(str_data)
                if 'type' in data.keys():
                    result = parse_result(data['type'], data['data'])
                    if result is not None and len(result) > 0:
                        msg = result + '\n'
                elif 'msg' in data.keys() and data['msg'] == 'connect client successfully':
                    self.connect = True
            except json.decoder.JSONDecodeError as ex:
                msg = str(ex) + '\n'
                logging.error('{}: write\'s json.decoder.JSONDecodeError: {}'.format(
                    self.tag, str(ex)))
        elif data_type == 'str':
            # 如果疏忽的话还是会传进来 byte[] 类型的，所以先用 str 包裹一下，就什么错都没有了
            msg = str(str_data.strip()) + '\n'
        logging.debug('{}: write(data: {}, data_type: {}) and msg: {}'.format(
            self.tag, str_data.strip(), data_type, msg.strip()))
        if len(msg) > 0:
            return msg
        return ''


class WebSocketClient(QtCore.QThread):
    tag = 'WebSocketClient'
    sinOut = QtCore.pyqtSignal(str)  # 记得 connect

    def __init__(self, url: str, uid: str, guid: str, msg_type: str):
        super(WebSocketClient, self).__init__()
        self.url = url
        self.uid = uid
        self.guid = guid
        self.msg_type = msg_type
        self.handler = MessageHandler()
        self.ws = None  # web socket
        self.thread = None  # 用于接收
        self.timer = None  # 用于 ping
        self.has_connected = False
        self.working = True
        self.commands = []
        self.has_finished = False
        logging.debug('{}: __init__ with url({}), uid({}), guid({}), msg_type({})'.format(
            self.tag, self.url, self.uid, self.guid, self.msg_type))

    def __del__(self):
        self.finish(reason='unknown reason')

    def finish(self, reason: str):
        if not self.has_finished:
            self.has_finished = True
            self.working = False
            self.has_connected = False
            if self.ws is not None:
                try:
                    self.ws.close()
                except Exception as ex:
                    logging.error('{}: _finish\' exception: {}'.format(
                        self.tag, ex))
            self.ws = None
            if self.timer is not None and not self.timer.finished.is_set():
                self.timer.cancel()
            self.timer = None
            self.thread = None
            self.wait()
            logging.debug('{}: __destroy__ with url({}), uid({}), guid({}), msg_type({})'.format(
                self.tag, self.url, self.uid, self.guid, self.msg_type))
            self.sinOut.emit(self.handler.transform(
                'connection has been closed: {}.'.format(reason), 'str'))
        else:
            logging.debug('{}: duplicate finish\' call'.format(self.tag))

    def recv(self) -> Union[Tuple[int, bytes], Tuple[int, None]]:
        try:
            frame = self.ws.recv_frame()
        except websocket.WebSocketException as ex:
            logging.error('{}: recv\'s websocket.WebSocketException: {}'.format(
                self.tag, str(ex)))
            return websocket.ABNF.OPCODE_CLOSE, None
        if not frame:
            raise websocket.WebSocketException("Not a valid frame %s" % frame)
        elif frame.opcode in OP_CODE_DATA:
            return frame.opcode, frame.data
        elif frame.opcode == websocket.ABNF.OPCODE_CLOSE:
            self.ws.send_close()
            logging.error('{}: recv\'s send_close'.format(self.tag))
            return frame.opcode, None
        elif frame.opcode == websocket.ABNF.OPCODE_PING:
            self.ws.pong(frame.data)
            logging.error('{}: recv\'s pong'.format(self.tag))
        return frame.opcode, frame.data

    def recv_ws(self):
        logging.debug('{}: recv\'s thread start'.format(self.tag))
        while self.working:
            try:
                op_code, data = self.recv()
                logging.debug('{}: recv() -> op_code: {}, data: {}.'.format(
                    self.tag, op_code, data))
                msg = None
                if op_code == websocket.ABNF.OPCODE_BINARY and isinstance(data, bytes):
                    data = str(data, "utf-8")
                if op_code in OP_CODE_DATA:
                    msg = data
                if msg is not None:
                    logging.debug('{}: recv result -- write: {}.'.format(
                        self.tag, msg))
                    self.sinOut.emit(self.handler.transform(msg))
                if op_code == websocket.ABNF.OPCODE_CLOSE:
                    break
            except Exception as ex:
                str_ex = str(ex)
                logging.error('{}: recv_ws\' exception: {}'.format(
                    self.tag, str_ex))
                if str_ex == 'socket is already closed.':
                    self.working = False
        logging.debug('{}: recv\'s thread finish'.format(self.tag))

    def ping(self):
        try:
            self.ws.ping()
            logging.debug('{}: ping'.format(self.tag))
        except Exception as ex:
            str_msg = '{}: ping\'s Exception: {}, so close the connection now.'.format(
                self.tag, ex)
            logging.error(str_msg)
            self.sinOut.emit(self.handler.transform(str_msg, 'str'))
            # if str_ex == "'NoneType' object has no attribute 'ping'" or str_ex == "socket is already closed.":
            self.working = False

    def connect(self) -> Tuple[str, bool]:
        msg, flag = self._check_params(True)
        if not flag:
            return msg, False
        self.ws = websocket.create_connection(
            self.url, sslopt={"cert_reqs": ssl.CERT_NONE, "check_hostname": False})
        # self.ws = websocket.WebSocketApp(self.url, )  # TODO: 改成 WebSocketApp 应该会更好的

        # TODO: 可能是 bug 所在，或许应该用 QThread 来代替，
        #  QThread.started.connect(self.recv_ws)
        #  QThread.finished.connect(app.exit)  self.finished
        logging.debug('{}: start recv_ws\'s thread.'.format(self.tag))
        self.thread = threading.Thread(target=self.recv_ws)
        self.thread.daemon = True
        # thread = QtCore.QThread()
        # thread.started.connect(self.recv_ws)
        self.thread.start()

        # TODO: 可能是 bug 所在，可以用 QTimer(https://blog.csdn.net/qq_34710142/article/details/80913448) 来代替，
        logging.debug('{}: start ping\'s thread.'.format(self.tag))
        self.timer = RepeatingTimer(30, self.ping)
        self.timer.daemon = True
        self.timer.start()

        connect_msg = '{"UserId":' + str(
            self.uid) + ',"Web":"","Guid":"' + str(self.guid) + '"}'
        logging.debug('{}: begin connect -- send: {}.'.format(
            self.tag, connect_msg))
        begin_connect = time.time()
        self.ws.send(connect_msg)
        while True:
            time.sleep(3)
            # self.sleep(3)
            if self.handler.connect:
                self.sinOut.emit(self.handler.transform(
                    'connect client successfully!.\n', 'str'))
                logging.debug('{}: connect successfully!.'.format(self.tag))
                msg = 'connect successfully'
                break
            if time.time() - begin_connect > 10:
                self.sinOut.emit(self.handler.transform(
                    'cannot connect client.\n', 'str'))
                logging.debug('{}: connect timeout.'.format(self.tag))
                msg = 'connect timeout'
                break
            self.ws.send(connect_msg)
            logging.debug(
                '{}: re_connect -- send: {}.'.format(self.tag, connect_msg))
        self.has_connected = self.handler.connect
        logging.debug('{}: finish connection, result: {}.'.format(
            self.tag, self.has_connected))
        return msg, self.has_connected

    def _check_params(self, flag: bool) -> Tuple[str, bool]:
        wrong_msg = None
        if flag and (self.url is None or len(self.url) == 0):
            wrong_msg = 'Url should not be empty.'
            flag = False
        if flag and (self.uid is None or len(self.uid) == 0):
            wrong_msg = 'User Id should not be empty.'
            flag = False
        if flag and (self.guid is None or len(self.guid) == 0):
            wrong_msg = 'Guid should not be empty.'
            flag = False
        if not flag:
            self.sinOut.emit(self.handler.transform(wrong_msg, 'str'))
            logging.debug('{}: check_params -- wrong msg: {}'.format(
                self.tag, wrong_msg))
        return wrong_msg, flag

    def run(self):
        logging.debug('{}: begin run'.format(self.tag))
        msg, flag = '', False
        try:
            msg, flag = self.connect()
        except Exception as ex:
            msg = str(ex)
            logging.error('{}: run\' exception: {}'.format(self.tag, msg))
        if flag:
            try:
                logging.debug('{}: begin dealing with commands'.format(
                    self.tag))
                while self.working:
                    if len(self.commands) == 0:
                        time.sleep(0.5)
                        continue
                    command = self.commands.pop(0)
                    self.sinOut.emit(self.handler.transform(
                        '> {}'.format(command), 'str'))
                    logging.debug('{}: send command: {}'.format(
                        self.tag, command))
                    self.ws.send(parse_command(self.msg_type, command))
                msg = 'after running'
            except Exception as ex:
                msg = str(ex)
                self.sinOut.emit(self.handler.transform(msg, 'str'))
                logging.error('{}: send command\'s Exception: {}'.format(
                    self.tag, msg))
        else:
            logging.debug('{}: connect failed: {}'.format(self.tag, msg))
        self.finish(msg)
        logging.debug('{}: finish run'.format(self.tag))


class UiForm(object):

    def __init__(self):
        self.tag = 'UiForm'
        self.form = None
        self.url_label = None
        self.url_edit = None
        self.uid_label = None
        self.uid_edit = None
        self.type_label = None
        self.type_combo_box = None
        self.guid_label = None
        self.guid_edit = None
        self.connect_btn = None
        self.stop_btn = None
        self.command_label = None
        self.command_edit = None
        self.send_btn = None
        self.output_text_browser = None
        self.websocket_client = None
        logging.debug('{}: init'.format(self.tag))

    def setup_ui(self, form: QtWidgets.QWidget):
        logging.debug('{}: begin ui\'s setup'.format(self.tag))
        font = QtGui.QFont()
        font.setPointSize(12)
        self.form = form
        self.form.setObjectName("Form")
        self.form.resize(1000, 630)
        self.form.setFont(font)

        logging.debug('{}: init {}\'s widgets'.format(self.tag, 'url'))
        self.url_label = _define_label(FocusLabel(
            form), [30, 20, 70, 20], font, 9, 'url_label')
        self.url_edit = _define_edit(QtWidgets.QLineEdit(
            form), [110, 20, 200, 20], font, '', 'url_edit')

        logging.debug('{}: init {}\'s widgets'.format(self.tag, 'uid'))
        self.uid_label = _define_label(FocusLabel(
            form), [330, 20, 70, 20], font, 9, 'uid_label')
        self.uid_edit = _define_edit(QtWidgets.QLineEdit(
            form), [410, 20, 200, 20], font, '', 'uid_edit')

        logging.debug('{}: init {}\'s widgets'.format(self.tag, 'type'))
        self.type_label = _define_label(FocusLabel(
            form), [630, 20, 80, 20], font, 9, 'type_label')
        self.type_combo_box = _define_widget(QtWidgets.QComboBox(
            form), [720, 20, 80, 22], font, 'type_combo_box')

        logging.debug('{}: init {}\'s widgets'.format(self.tag, 'output_text'))
        self.output_text_browser = _define_widget(
            QtWidgets.QTextBrowser(form), [30, 140, 940, 475], font, 'output_text_browser')
        self.output_text_browser.document().setMaximumBlockCount(3000)

        logging.debug('{}: init {}\'s widgets'.format(self.tag, 'guid'))
        self.guid_label = _define_label(FocusLabel(
            form), [30, 60, 70, 20], font, 9, 'guid_label')
        self.guid_edit = _define_edit(QtWidgets.QLineEdit(
            form), [110, 60, 501, 20], font, '', 'guid_edit')

        self.connect_btn = _define_widget(QtWidgets.QPushButton(
            form), [880, 20, 91, 23], font, 'connect_btn')
        self.stop_btn = _define_widget(QtWidgets.QPushButton(
            form), [880, 60, 91, 23], font, 'stop_btn')

        logging.debug('{}: init {}\'s widgets'.format(self.tag, 'command'))
        self.command_edit = _define_edit(HistoryLineEdit(
            form), [110, 100, 681, 20], font, '', 'command_edit')
        self.command_label = _define_label(FocusLabel(
            form), [30, 100, 70, 20], font, 9, 'command_label')

        self.send_btn = _define_widget(QtWidgets.QPushButton(
            form), [880, 100, 91, 23], font, 'send')

        self.re_translate_ui(form)
        self.init_events()
        QtCore.QMetaObject.connectSlotsByName(form)
        logging.debug('{}: finish ui\'s setup'.format(self.tag))

    def re_translate_ui(self, form):
        _translate = QtCore.QCoreApplication.translate
        form.setWindowTitle(_translate("Form", "远程控制"))
        self.url_edit.setText(_translate('Form', 'ws://157.255.228.135'))
        self.url_label.setText(_translate("Form", "后台Url："))
        self.uid_label.setText(_translate("Form", "用户Uid："))
        self.type_label.setText(_translate("Form", "协议类型："))
        self.guid_label.setText(_translate("Form", "用户Guid："))
        self.command_label.setText(_translate("Form", "输入指令："))
        self.connect_btn.setText(_translate("Form", "Connect"))
        self.stop_btn.setText(_translate("Form", "Stop"))
        self.send_btn.setText(_translate("Form", "Send"))
        if TEST_FLAG:
            self.uid_edit.setText(_translate('Form', str(TEST_UID)))
            self.guid_edit.setText(_translate('Form', TEST_GUID))
        logging.debug('{}: re translate ui'.format(self.tag))

    def add_type(self, msg_type: str):
        self.type_combo_box.addItem(msg_type)
        logging.debug('{}: call add_type(msg_type: {})'.format(
            self.tag, msg_type))

    def init_events(self):
        self.send_btn.clicked.connect(self.send_command)
        self.stop_btn.clicked.connect(self.stop)
        self.connect_btn.clicked.connect(self.connect)

        self.url_label.set_target(self.url_edit)
        self.uid_label.set_target(self.uid_edit)
        self.command_label.set_target(self.command_edit)
        self.type_label.set_target(self.type_combo_box)
        self.guid_label.set_target(self.guid_edit)

        self.output_text_browser.textChanged.connect(self.text_browser_end)

        self.url_edit.returnPressed.connect(self.uid_edit.setFocus)
        self.uid_edit.returnPressed.connect(self.guid_edit.setFocus)
        self.guid_edit.returnPressed.connect(self.connect)
        self.command_edit.returnPressed.connect(self.send_command)

        logging.debug('{}: init events'.format(self.tag))

    def text_browser_write(self, msg: str):
        # self.output_text_browser.insertPlainText(msg)
        msg = msg.strip()
        if len(msg) > 0:
            self.output_text_browser.append(msg)
            logging.debug('{}：call text_browser_write(str: {})'.format(
                self.tag, msg))

    def text_browser_end(self):
        self.output_text_browser.moveCursor(QtGui.QTextCursor.End)
        logging.debug('{}: call text_browser_end()'.format(self.tag))

    def stop(self):
        if self.websocket_client is not None:
            self._finish_websocket_client(
                '{}: {}', 'clicked the button, so the ' + (
                    'alive' if self.websocket_client.working else 'dead') + ' connection is broken.')
        else:
            self.text_browser_write('connection have broken.')

    def connect(self):
        logging.debug('{}: begin connection'.format(self.tag))
        url = self.url_edit.text().strip()
        uid = self.uid_edit.text().strip()
        guid = self.guid_edit.text().strip()
        msg_type = self.type_combo_box.currentText()
        if self.websocket_client is not None and self.websocket_client.working \
                and self.websocket_client.url == url and self.websocket_client.uid == uid \
                and self.websocket_client.guid == guid and self.websocket_client.msg_type == msg_type:
            msg = 'already established a connection with url({}), uid({}), guid({}), msg_type({})'.format(
                url, uid, guid, msg_type)
            self.text_browser_write(msg)
            logging.debug('{}: {}, therefore terminate connection.'.format(
                self.tag, msg))
            return
        if self.websocket_client is not None:
            self._finish_websocket_client(
                '{}: {}', 'new connection is required, but last connection is still alive, now close it.')
        self.text_browser_write('starting connect {} with userId: {}, guid: {} and msgType: {}\n'.format(
            url, uid, guid, msg_type))
        self.websocket_client = WebSocketClient(url, uid, guid, msg_type)
        self.websocket_client.sinOut.connect(self.text_browser_write)
        try:
            self.websocket_client.start()
            logging.debug('{}: finish connection'.format(self.tag))
        except Exception as ex:
            self._finish_websocket_client('{}: connect\'s exception: {}', ex)

    def send_command(self):
        logging.debug('{}: begin sending command'.format(self.tag))
        if self.websocket_client is None or not self.websocket_client.has_connected \
                or not self.websocket_client.working:
            QtWidgets.QMessageBox.information(
                self.form, '警告', '还未建立连接', QtWidgets.QMessageBox.Yes)
            logging.warning('{}: sending command before connection is not allowed'.format(
                self.tag))
            return
        command = self.command_edit.text().strip()
        if command == 'exit':
            QtWidgets.QMessageBox.information(
                self.form, '警告', '输入不合法指令(后续可能改为输入exit退出)', QtWidgets.QMessageBox.Yes)
            return
        # self.output_text_browser.insertPlainText('> {}\n'.format(command))
        self.websocket_client.msg_type = self.type_combo_box.currentText()
        try:
            self.websocket_client.commands.append(command)
            logging.debug('{}: finish sending command'.format(self.tag))
        except Exception as ex:
            self._finish_websocket_client(
                '{}: send command\'s exception: {}', ex)
        self.command_edit.setText('')

    def _finish_websocket_client(self, error_msg: str, ex: Union[Exception, str] = None):
        if self.websocket_client is not None:
            self.websocket_client.sinOut.disconnect(self.text_browser_write)
            str_ex = str(ex)
            self.websocket_client.finish(str_ex)
            self.websocket_client = None
            if ex is not None or not isinstance(ex, str):
                logging.error(error_msg.format(self.tag, str_ex))  # exception
            elif isinstance(ex, str):
                logging.debug(error_msg.format(self.tag, str_ex))  # reason
            self.text_browser_write(str_ex)


def exec_app():
    logging.debug('app start')
    app = QtWidgets.QApplication(sys.argv)
    widget = QtWidgets.QWidget()
    ui = UiForm()
    ui.setup_ui(widget)
    for msg_controller in msg_controllers:
        ui.add_type(msg_controller.req_type)
    # widget.setWindowIcon(QtGui.QIcon('./icon.png'))
    widget.show()
    status = app.exec_()
    logging.debug('app end')
    sys.exit(status)


# TODO:
if __name__ == '__main__':
    define_log('./gui_logs/', 10)
    try:
        exec_app()
    except Exception as e:
        logging.error('app\'s exception: {}'.format(str(e)))
        # TODO: re_exec_app

# uid: 50042533; guid: 0e74af97aa48135d0c5528db29dbb6fe; url: ws://157.255.228.135
# pyinstaller -F .\py\remote_gui_controller.py
# adb logcat -e "RemotePushWatcher|ShellMsgHandler|RemoteClient|RemoteMsgManager|WebSocketClientWrapper"
# https://jenkins.huya.com/job/kiwi_git-android/27249/console
# 27249 remoteshell.apk
# 27258 remoteshell2.apk
# 27259 remoteShell3.apk
# 27262 remoteShell4.apk
