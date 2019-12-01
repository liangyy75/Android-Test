package com.liang.example.nativeremote;

import android.app.Application;
import android.content.Context;
import android.os.DropBoxManager;
import android.os.Environment;

import com.liang.example.remote.RemoteMsgManager;
import com.liang.example.remoteutils.JsonApiKt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class UtilMsgHandler extends AbsMsgHandler<StringReqOrRes, StringReqOrRes> {
    private static final String TAG = "UtilMsgHandler";

    private Map<String, Method> methodsCache;
    private Application application;  // 对 MTPApi.CONTEXT 的绝对信任，都不对 CONTEXT 的 application / context 进行判空了
    private Context context;
    private ArgTransfer argTransfer;
    private boolean nullable = false;

    private static String outputPath;

    public UtilMsgHandler(Application application) {
        super("utilReq", "utilRes");
        Class<?>[] classes = new Class[]{/*DeviceUtils.class, DeviceHelper.class, VersionUtil.class, Utils.class, SystemUtils.class, NetworkUtils.class, */UtilMsgHandler.class};
        methodsCache = new HashMap<>();
        for (Class clazz : classes) {
            String className = clazz.toString().substring(6);
            load(className);
        }
        this.application = application;
        this.context = application;
        outputPath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + application.getPackageName() + "/files/log/";
    }

    public void setArgTransfer(ArgTransfer argTransfer) {
        this.argTransfer = argTransfer;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    private boolean load(String className) {
        int targetModifier = Modifier.STATIC | Modifier.PUBLIC;
        Class clazz;
        try {
            clazz = Class.forName(className);
            // clazz = ClassLoader.getSystemClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            RemoteMsgManager.logger.e(TAG, "load -- className: %s, ClassNotFoundException: %s", className, e);
            return false;
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if ((method.getModifiers() & targetModifier) == targetModifier) {
                String methodName = method.getName();
                int id = 1;
                while (methodsCache.get(methodName + id) != null) {
                    id++;
                }
                methodsCache.put(methodName + id, method);
            }
        }
        return true;
    }

    @Override
    public void onMessage(String serverUrl, StringReqOrRes stringReq) {
        String[] commands = stringReq.getS().split("&&|\\|\\|");
        StringBuilder result = new StringBuilder();
        for (String command : commands) {
            List<String> commandAndArgs = getComAndArgStrings(command);
            if (commandAndArgs.get(0).equals("load") && commandAndArgs.size() == 2) {
                String className = commandAndArgs.get(1);
                result.append("load class of ").append(className).append(": ").append(load(className)).append("\n");
            } else {
                executeCommand(result, commandAndArgs);
            }
        }
        this.send(serverUrl, new StringReqOrRes(result.toString()));
    }

    private List<String> getComAndArgStrings(String command) {
        List<String> commandAndArgs = new ArrayList<>();
        Stack<Character> stack = new Stack<>();
        int len = command.length();
        int i = 0;
        while (i < len && command.charAt(i) == ' ') {
            i++;
        }
        StringBuilder argOrCommand = new StringBuilder();
        for (; i < len; i++) {
            Character ch = command.charAt(i);
            int last = i - 1;
            if (stack.empty() && ch == ' ' && (i == 0 || command.charAt(last) != '\\')) {
                commandAndArgs.add(argOrCommand.toString());
                argOrCommand.delete(0, argOrCommand.length());
                int next = i + 1;
                while (next < len && command.charAt(next) == ' ') {
                    i = next;
                    next++;
                }
            } else {
                argOrCommand.append(ch);
                if ((ch == '"' || ch == '\'') && (i == 0 || command.charAt(last) != '\\')) {
                    if (!stack.empty() && stack.peek() == ch) {
                        stack.pop();
                    } else {
                        stack.push(ch);
                    }
                }
            }
        }
        if (stack.empty() && argOrCommand.length() > 0) {
            commandAndArgs.add(argOrCommand.toString());
        }

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < commandAndArgs.size(); j++) {
            sb.append(commandAndArgs.get(j)).append(",");
        }
        RemoteMsgManager.logger.d(TAG, "getComAndArgStrings -- command: %s, commandAndArgs: %s", command, sb.toString());
        return commandAndArgs;
    }

    private void executeCommand(StringBuilder result, List<String> commandAndArgs) {
        if (commandAndArgs.size() == 0) {
            result.append("incorrect command\n");
            return;
        }
        int id = 1;
        String methodName = commandAndArgs.get(0);
        String error = null;  // "no corresponding method"
        String output = null;
        while (methodsCache.get(methodName + id) != null) {
            Method method = methodsCache.get(methodName + id);
            assert method != null;
            Class[] parameterClasses = method.getParameterTypes();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Object[] parameters = new Object[parameterClasses.length];
            int k = 1;
            int argSize = commandAndArgs.size();
            for (int l = 0; l < parameterClasses.length; l++) {
                if (parameterClasses[l].equals(Application.class)) {
                    parameters[l] = application;
                } else if (parameterClasses[l].equals(Context.class)) {
                    parameters[l] = context;
                } else if (k < argSize) {
                    if (argTransfer != null) {
                        parameters[l] = argTransfer.argTransfer(commandAndArgs.get(k++));
                    } else if (!transform(commandAndArgs, parameterClasses[l], parameters, k++, l)) {
                        RemoteMsgManager.logger.d(TAG, "executeCommand -- unhandled class type: %s", parameterClasses[l].toString());
                        error = String.format("executeCommand -- unhandled class type: %s", parameterClasses[l].toString());
                        break;
                    }
                } else if (nullable) {
                    parameters[l] = null;
                } else {
                    if (parameterAnnotations[l] != null) {
                        boolean flag = false;
                        for (Annotation annotation : parameterAnnotations[l]) {
                            if (annotation.annotationType().equals(Nullable.class) || annotation.getClass().equals(Nullable.class)) {
                                parameters[l] = null;
                                flag = true;
                                break;
                            }
                        }
                        if (flag) {
                            continue;
                        }
                    }
                    RemoteMsgManager.logger.d(TAG, "executeCommand -- not enough argument's type: %s", parameterClasses[l].toString());
                    error = String.format("executeCommand -- not enough argument's type: %s", parameterClasses[l].toString());
                    break;
                }
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parameterClasses.length; i++) {
                sb.append(parameters[i]).append(",");
            }
            RemoteMsgManager.logger.d(TAG, "executeCommand -- methodName: %s, parameters: %s", methodName, sb.toString());
            if (error != null) {
                id++;
            } else {
                try {
                    output = String.valueOf(method.invoke(null, parameters));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    RemoteMsgManager.logger.e(TAG, "executeCommand -- invoke method's error", e);
                    output = String.format("invoke method's error: %s", e);
                }
                break;
            }
        }
        if (output != null) {
            result.append(output).append("\n");
        } else {
            result.append("no corresponding method").append("\n");
        }
    }

    private boolean transform(List<String> commandAndArgs, Class parameterClass, Object[] parameters, int k, int l) {
        // 基本类型
        if (parameterClass.equals(String.class)) {
            parameters[l] = commandAndArgs.get(k);
        } else if (parameterClass.equals(char.class)) {
            parameters[l] = commandAndArgs.get(k).charAt(0);
        } else if (parameterClass.equals(byte.class)) {
            parameters[l] = Byte.valueOf(commandAndArgs.get(k));
        } else if (parameterClass.equals(short.class)) {
            parameters[l] = Short.valueOf(commandAndArgs.get(k));
        } else if (parameterClass.equals(int.class)) {
            parameters[l] = Integer.valueOf(commandAndArgs.get(k));
        } else if (parameterClass.equals(long.class)) {
            parameters[l] = Long.valueOf(commandAndArgs.get(k));
        } else if (parameterClass.equals(float.class)) {
            parameters[l] = Float.valueOf(commandAndArgs.get(k));
        } else if (parameterClass.equals(double.class)) {
            parameters[l] = Double.valueOf(commandAndArgs.get(k));
        } else if (parameterClass.equals(boolean.class)) {
            parameters[l] = Boolean.valueOf(commandAndArgs.get(k));
        }
        // 数组类型
        else if (parameterClass.equals(String[].class)) {
            parameters[l] = commandAndArgs.get(k).split(",");
        } else if (parameterClass.equals(char[].class)) {
            parameters[l] = commandAndArgs.get(k).toCharArray();
        } else if (parameterClass.equals(byte[].class)) {
            parameters[l] = commandAndArgs.get(k).getBytes();
        } else if (parameterClass.equals(short[].class)) {
            String[] ss = commandAndArgs.get(k).split(",");
            short[] arr = new short[ss.length];
            for (int m = 0; m < ss.length; m++) {
                arr[m] = Short.valueOf(ss[m]);
            }
            parameters[l] = arr;
        } else if (parameterClass.equals(int[].class)) {
            String[] ss = commandAndArgs.get(k).split(",");
            int[] arr = new int[ss.length];
            for (int m = 0; m < ss.length; m++) {
                arr[m] = Integer.valueOf(ss[m]);
            }
            parameters[l] = arr;
        } else if (parameterClass.equals(long[].class)) {
            String[] ss = commandAndArgs.get(k).split(",");
            long[] arr = new long[ss.length];
            for (int m = 0; m < ss.length; m++) {
                arr[m] = Long.valueOf(ss[m]);
            }
            parameters[l] = arr;
        } else if (parameterClass.equals(float[].class)) {
            String[] ss = commandAndArgs.get(k).split(",");
            float[] arr = new float[ss.length];
            for (int m = 0; m < ss.length; m++) {
                arr[m] = Float.valueOf(ss[m]);
            }
            parameters[l] = arr;
        } else if (parameterClass.equals(double[].class)) {
            String[] ss = commandAndArgs.get(k).split(",");
            double[] arr = new double[ss.length];
            for (int m = 0; m < ss.length; m++) {
                arr[m] = Double.valueOf(ss[m]);
            }
            parameters[l] = arr;
        } else if (parameterClass.equals(boolean[].class)) {
            String[] ss = commandAndArgs.get(k).split(",");
            boolean[] arr = new boolean[ss.length];
            for (int m = 0; m < ss.length; m++) {
                arr[m] = Boolean.valueOf(ss[m]);
            }
            parameters[l] = arr;
        }
        // 复杂类型
        else {
            Object temp = JsonApiKt.parseJson(commandAndArgs.get(k), parameterClass);
            if (temp == null) {
                return false;
            }
            parameters[l] = temp;
        }
        return true;
    }

    public static String getDropBoxInfoToFile(@Nullable String outputPath, @Nullable Long time, @Nullable Integer count, Application application) {
        if (outputPath == null) {
            outputPath = UtilMsgHandler.outputPath;
        } else if (!outputPath.endsWith("/")) {
            outputPath = outputPath + "/";
        }
        if (time == null) {
            time = 0L;
        }
        if (count == null) {
            count = 5;
        }
        RemoteMsgManager.logger.d(TAG, "getDropBoxInfoToFile -- outputPath: %s, time: %d, count: %d", outputPath, time, count);
        DropBoxManager dropBoxManager = (DropBoxManager) application.getSystemService(Context.DROPBOX_SERVICE);
        if (dropBoxManager == null) {
            return "could not get dropBoxManager";
        }
        int i = 0;
        DropBoxManager.Entry entry;
        StringBuilder error = new StringBuilder();
        while (i <= count && (entry = dropBoxManager.getNextEntry(null, time)) != null) {
            String fileName = outputPath + entry.getTag() + ".log";
            InputStream is = null;
            OutputStream os = null;
            i++;
            try {
                is = entry.getInputStream();
                File file = new File(fileName);
                int tryCount = 0;
                while (!file.exists() && !file.createNewFile() && tryCount < 3) {
                    tryCount++;
                }
                if (!file.exists()) {
                    error.append("file(").append(fileName).append(") can't be created\n");
                    continue;
                }
                os = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len = 0;
                while ((len = is.read(buf, 0, 1024)) != 0) {
                    os.write(buf, 0, len);
                }
                error.append("file(").append(fileName).append(") has be created\n");
            } catch (IOException e) {
                RemoteMsgManager.logger.e(TAG, "getDropBoxInfoToFile -- IOException, could not get %s's input stream: %s", fileName, e);
                error.append("could not get ").append(fileName).append("'s input stream: ").append(e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        RemoteMsgManager.logger.e(TAG, "getDropBoxInfoToFile -- IOException, close inputStream error: %s", e);
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        RemoteMsgManager.logger.e(TAG, "getDropBoxInfoToFile -- IOException, close outputStream error: %s", e);
                    }
                }
            }
        }
        return error.toString();
    }

    public interface ArgTransfer {
        Object argTransfer(String arg);
    }

    // public static void pull(String localPath, String remotePath) {
    //     // TODO: 需要后台协作，否则没有公网ip是没法操作的 https://blog.csdn.net/kylsen/article/details/53213288 https://blog.csdn.net/u014026084/article/details/73124157
    // }
    //
    // public static void push(String remotePath, String localPath) {
    //     // TODO: 需要后台协作，否则没有公网ip是没法操作的 https://blog.csdn.net/kylsen/article/details/53213288 https://blog.csdn.net/u014026084/article/details/73124157
    // }

    // adb tcpip
    // adb connect
}
