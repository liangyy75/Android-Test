package liang.example.hotfixtest;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class FixDexUtil {
    private static final String DEX_SUFFIX = ".dex";
    private static final String APK_SUFFIX = ".apk";
    private static final String JAR_SUFFIX = ".jar";
    private static final String ZIP_SUFFIX = ".zip";
    public static final String DEX_DIR = "odex";
    private static final String OPTIMIZE_DEX_DIR = "optimize_dex";
    private static HashSet<File> loadedDex = new HashSet<>();

    static {
        loadedDex.clear();
    }

    // 加载补丁，使用默认目录：data/data/包名/files/odex，dex合并之前的dex
    public static void loadFixedDex(Context context) {
        doDexInject(context, loadedDex);
    }

    // 验证是否需要热修复
    public static boolean isGoingToFix(@NonNull Context context, String dexDir) {
        Log.d("Android Fix", "start isGoingToFix");
        boolean canFix = false;
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        if (dexDir == null) {
            dexDir = DEX_DIR;
        }
        File fileDir = externalStorageDirectory != null ?
                new File(externalStorageDirectory, "007") :  // /storage/emulated/0/007
                new File(context.getFilesDir(), dexDir);  // data/data/包名/files/odex（这个可以任意位置）
        Log.d("Android Fix", fileDir.getAbsolutePath());
        // 遍历所有的修复dex , 因为可能是多个dex修复包
        File[] listFiles = fileDir.listFiles();
        if (listFiles != null) {
            Log.d("Android Fix", "listFiles.length: " + listFiles.length);
            for (File file : listFiles) {
                if (file.getName().startsWith("classes") &&
                        (file.getName().endsWith(DEX_SUFFIX)
                                || file.getName().endsWith(APK_SUFFIX)
                                || file.getName().endsWith(JAR_SUFFIX)
                                || file.getName().endsWith(ZIP_SUFFIX))) {
                    Log.d("Android Fix", "classes: " + file.getAbsolutePath());
                    loadedDex.add(file);
                    canFix = true;  // 有目标dex文件, 需要修复
                }
            }
        }
        Log.d("Android Fix", "end isGoingToFix");
        return canFix;
    }

    private static void doDexInject(Context appContext, HashSet<File> loadedDex) {
        Log.d("Android Fix", "start doDexInject");
        // data/data/包名/files/optimize_dex（这个必须是自己程序下的目录）
        String optimizeDir = appContext.getFilesDir().getAbsolutePath() + File.separator + OPTIMIZE_DEX_DIR;
        File fopt = new File(optimizeDir);
        if (!fopt.exists()) {
            // noinspection ResultOfMethodCallIgnored
            fopt.mkdirs();
        }
        try {
            // 1.加载应用程序dex的Loader
            PathClassLoader pathLoader = (PathClassLoader) appContext.getClassLoader();
            for (File dex : loadedDex) {
                // 2.加载指定的修复的dex文件的Loader
                DexClassLoader dexLoader = new DexClassLoader(
                        dex.getAbsolutePath(),  // 修复好的dex（补丁）所在目录
                        fopt.getAbsolutePath(),  // 存放dex的解压目录（用于jar、zip、apk格式的补丁）
                        null,  // 加载dex时需要的库
                        pathLoader  // 父类加载器
                );
                Object dexPathList = getPathList(dexLoader);
                Object pathPathList = getPathList(pathLoader);
                Object leftDexElements = getDexElements(dexPathList);
                Object rightDexElements = getDexElements(pathPathList);
                Object dexElements = combineArray(leftDexElements, rightDexElements);
                Object pathList = getPathList(pathLoader);  // 一定要重新获取，不要用pathPathList，会报错
                setField(pathList, pathList.getClass(), "dexElements", dexElements);
            }
            Toast.makeText(appContext, "修复完成", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Android Fix", "end doDexInject");
    }

    private static void setField(Object obj, Class<?> cl, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cl.getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        declaredField.set(obj, value);
    }

    private static Object getField(Object obj, Class<?> cl, String field) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    private static Object getPathList(Object baseDexClassLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getDexElements(Object pathList) throws NoSuchFieldException, IllegalAccessException {
        return getField(pathList, pathList.getClass(), "dexElements");
    }

    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> clazz = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);  // 得到左数组长度（补丁数组）
        int j = Array.getLength(arrayRhs);  // 得到原dex数组长度
        int k = i + j;  // 得到总数组长度（补丁数组+原dex数组）
        Object result = Array.newInstance(clazz, k);  // 创建一个类型为clazz，长度为k的新数组
        System.arraycopy(arrayLhs, 0, result, 0, i);
        System.arraycopy(arrayRhs, 0, result, i, j);
        return result;
    }
}
