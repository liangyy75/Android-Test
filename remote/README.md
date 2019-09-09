## 0. background

1. 为了能远程调试手机，获取手机信息，就设计了这个远程控制模块。这个远程是指只要用户手机能够访问到remote_server.py中指定的WebSocket端口，不需要数据线，就像远程adb连接一样。
2. 但是该设计是能够扩展的，虽然现在默认是只支持shell指令，而且还无法像adb那样获取root权限。但是该设计是可扩展的，虽然现在也不知道能扩展什么(ノへ￣、)。

## 1. remote_server

1. **远程控制的后台**，会建立一个WebSocket Server来监听某一接口，可以接受工具端和移动端的WebSocket长连接。主要文件就是remote_server文件夹下面的remote_server.py和remote_server.exe。
2. remote_server.exe是使用pipenv和pyinstaller这两个python库将remote_server.py打包而成的可执行文件。需要pipenv是因为pyinstaller在anaconda环境下会打包多余的dll文件，所以需要pipenv来构建虚拟环境。
3. remote_server.py的使用方法: ``remote_server.exe --host 172.18.33.203 --port 8099 --log_files ./smg``，其中host应该是自己的本机ip，port的端口号，log_files是日志文件存放地方。
    1. host应该是要本地ip的，但不清楚127.0.0.1起不起效，没有尝试过。但向外暴露我觉得最好还是本地ip吧。host的默认值是我本机ip，而port默认值是9001，一般需要重新设置。
    2. log_files是日志文件存放的文件夹，默认是当前exe所在目录的server_logs文件夹下(没有就创建)，每次运行remote_server.py就会新生成一个日志文件，但是不会超过10份，多余的会删除较老的那些。
    3. 由于没有做过于复杂的日志文件切割逻辑，所以remote_server.py的每次运行只会生成一个日志文件，过长时间的运行，可能会形成过大的日志文件，形成问题。
    4. 在命令行运行remote_server.py之后，需要自己手动关闭命令行或者ctrl+c打断，否则会一直运行。
    5. remote_server.py是使用原生python实现的，不需要其他库。

## 2. remote_controller

1. **远程控制的工具端**，分为两个文件，一个是命令行模式的，一个是界面模式的。这两个python文件都需要0.56.0这个版本的websocket-client库，而有界面的那个websocket_gui_controller.py还需要5.13.0这个版本的pyqt5库。
2. 工具端也分exe和py文件。
3. 命令行模式的工具端的使用方式略显复杂，但是主要也是 --ws_url / --userId / --guid / --type 这几个参数，ws_url是远程控制后台的WebSocket地址，如ws://172.18.33.203:9001，而--userId则是用户标识，guid是每台设备的标识，同一用户的不同设备是通过这个来标识的。
4. 界面模式的工具端使用起来和命令行模式的工具差不多。
    1. 有三个输入框可以输入WebSocket后台的地址、用户Id、设备Id。还有一个协议选择框可以选择协议，现在只支持shell指令和一个类似于echo的协议，即你发送什么指令，它就返回什么指令，当然加上一些字符串表示是来自客户端的。
    2. 可以通过 connect / stop 这两个按钮来多次建立连接而已，或者单纯的在连接断开后重新用connect按钮建立连接。
    3. 有一个文本展示框用于展示发送的指令以及远程指令执行返回的结果，还可以通过clear按钮来清空文本展示框里面的内容，又或者通过save按钮来保存文本框里面的内容。
    4. 指令框可以通过enter键来发送指令，或者通过send按钮。指令框是有历史记录的，最多保存30条指令，可以通过"↑"和"↓"两个键盘按键查找历史。
5. 工具端也是有日志文件的产生的。默认是exe当前目录的cmd_logs和gui_logs文件夹。
6. **注意**，在打包有界面的工具端时由于pyinstaller和pyqt5的一些不兼容问题，没有将Qt5Core.dll这个dll打包进去，所以最好还是使用py文件，或者将dll放到C:/windows/system32这个文件夹下。

## 3. com.liang.example.shelltest 与 remote 模块

1. **远程控制的移动端**，主要是remote模块与app模块中的com.liang.example.shelltest包，当然还有com.liang.example.androidtest下的ApplicationTest.java文件(用于初始化remote模块)。
2. 移动端的操作则非常简单，安装AndroidTest项目后，运行后进入主页面，滑动到最下面，然后点击最后的那个Test Remote Controller的这个条目，进入后可以看有三个输入框，包括WebSocket地址、用户Id和设备Id，以及两个按钮，一个start用于连接后台，一个stop用于断开连接。
3. **重点**: 最后整合三部分的使用方法是
    1. 运行后台: ``python remote_server.py --host xx.xx.xx.xx --port xxxx``。
    2. 运行工具端: ``python remote_gui_controller.py``。
    3. 运行移动端，进入Test Remote Controller所在的Activity页面。
    4. 然后点击工具端的connect，之后立刻点击移动端的start test这个按钮。由于后台每60秒就检查一下建立的工具端连接有没有对应的移动端连接，如果没有就会关闭工具端连接，所以在建立工具端连接后需要立刻建立移动端的连接。而移动端的连接在建立时如果没有对应的工具端连接就直接关闭了，连60秒的缓冲时间也没有，所以只能先建立工具端的长连接，之后建立移动端的长连接。
    5. 在应用进入后台时触发的onPause和onStop都会startService，让前台Service启动，目的是为了维护长连接(结果无效)。
    6. 当然，应该修改这种模式。。。

## 4. problems

1. 很多国内手机厂商修改了安卓内部的机制，在应用进入后台或者是手机熄屏一定时间后就会限制应用的网络等等资源，这个时候建立的长连接也会断开，当然也有可能是Android 6新出的doze模式，但是我在针对使用doze模式申请了权限之后还是无效，按了home键后应用建立的长连接还是关闭了。下面是针对长连接断开我尝试过的方法:
    1. 针对doze模式申请了免除节电模式的权限。
    2. 使用前台Service。前台Service这里是建立了一个Timer，定时向后台建立长连接，这样就不用点击了，当然也可以定制Notification，但是没有这个时间。而在进入后台一段时候后这个Timer就暂停了，无法向后台发送请求。查看相关的Log日志，看到的是: close sockets ，即使我申请了各种权限。而且后台什么请求都没收到，可以看到是限制了网络。
    3. 申请了WakeLock。
2. 我们又不像QQ、微信那样进入白名单，而华为手机上只有在充电时才会取消这个模式，所以(ノへ￣、)。
