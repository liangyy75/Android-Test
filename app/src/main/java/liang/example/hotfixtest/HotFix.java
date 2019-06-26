package liang.example.hotfixtest;

import android.content.Context;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class HotFix {
    private static void injectDexToClassLoader(Context context, String fixDexFilePath) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        // 读取 baseElements
        PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
        Object basePathList = getPathList(pathClassLoader);
        Object baseElements = getDexElements(basePathList);
        // 读取 fixElements
        String baseDexAbsolutePath = context.getDir("dex", 0).getAbsolutePath();
        DexClassLoader fixDexClassLoader = new DexClassLoader(fixDexFilePath, baseDexAbsolutePath, fixDexFilePath, context.getClassLoader());
        Object fixPathList = getPathList(fixDexClassLoader);
        Object fixElements = getDexElements(fixPathList);
        // 合并两份Elements
        Object newElements = combineArray(baseElements, fixElements);
        // 一定要重新获取，不要用basePathList，会报错
        Object basePathList2 = getPathList(pathClassLoader);
        // 新的dexElements对象重新设置回去
        setField(basePathList2, basePathList2.getClass(), "dexElements", newElements);
    }

    private static Object combineArray(Object baseElements, Object fixElements) {
        // 合拼dexElements ,并确保 fixElements 在 baseElements 之前
        Class componentType = fixElements.getClass().getComponentType();
        int fixLength = Array.getLength(fixElements);
        int totalLength = Array.getLength(baseElements) + fixLength;
        Object newInstance = Array.newInstance(componentType, totalLength);
        for (int i = 0; i < totalLength; i++) {
            if (i < fixLength) {
                Array.set(newInstance, i, Array.get(fixElements, i));
            } else {
                Array.set(newInstance, i, Array.get(baseElements, i - fixLength));
            }
        }
        return null;
    }

    private static Object getDexElements(Object object) throws NoSuchFieldException, IllegalAccessException {
        return getField(object, object.getClass(), "dexElements");
    }

    private static Object getPathList(Object object) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(object, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getField(Object object, Class clazz, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = clazz.getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        return declaredField.get(object);
    }

    private static void setField(Object object, Class clazz, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = clazz.getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        declaredField.set(object, value);
    }
}
