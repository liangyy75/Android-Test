package liang.example.apttest.bind;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class InjectUtils {
    private static final String TAG = "apt.base.InjectUtils";

    private InjectUtils() {}

    private static volatile InjectUtils INSTANCE = null;

    static InjectUtils getInstance() {
        if (INSTANCE == null) {
            synchronized (InjectUtils.class) {
                if (INSTANCE == null) {
                    INSTANCE = new InjectUtils();
                }
            }
        }
        return INSTANCE;
    }

    private Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        String argStrs;
        if (args.length > 0) {
            StringBuilder argStrBuilder = new StringBuilder(args[0].getSimpleName());
            for (int i = 1; i < args.length; i++) {
                argStrBuilder.append(", ").append(args[i].getSimpleName());
            }
            argStrs = argStrBuilder.toString();
        } else {
            argStrs = "";
        }
        try {
            Method result = clazz.getMethod(name, args);
            result.setAccessible(true);
            Log.d(TAG, "find method successfully: " + clazz.getSimpleName() + "." + name + "(" + argStrs + ")");
            return result;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.d(TAG, "find method failed: " + clazz.getSimpleName() + "." + name + "(" + argStrs + ")");
            return null;
        }
    }

    void injectViews(Activity activity) {
        Log.d(TAG, "begin injecting views");
        if (activity == null) return;
        Class<? extends Activity> activityClass = activity.getClass();
        Field[] declaredFields = activityClass.getDeclaredFields();
        Method findViewById = getMethod(activityClass, "findViewById", int.class);
        if (findViewById == null) return;
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(InjectView.class)) {
                field.setAccessible(true);
                InjectView annotation = field.getAnnotation(InjectView.class);
                assert annotation != null;
                int id = annotation.value();
                try {
                    Object view = findViewById.invoke(activity, id);
                    field.set(activity, view);
                    Log.d(TAG, "inject view(" + field.getName() + ") successfully");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    Log.d(TAG, "inject view(" + field.getName() + ") failed");
                }
            }
        }
        Log.d(TAG, "finish injecting views");
    }

    void injectEvents(Activity activity) {
        Log.d(TAG, "begin injecting events");
        if (activity == null) return;
        Class<? extends Activity> activityClass = activity.getClass();
        Method findViewById = getMethod(activityClass, "findViewById", int.class);
        if (findViewById == null) return;
        Method[] declaredMethods = activityClass.getDeclaredMethods();
        Class<? extends Annotation>[] annotationClasses = new Class[]{
                OnClick.class,
                OnLongClick.class,
        };
        Method[] listenerSetMethods = new Method[annotationClasses.length];
        EventType[] eventTypes = new EventType[annotationClasses.length];
        String[] listenerMethodNames = new String[annotationClasses.length];
        for (int i = 0; i < annotationClasses.length; i++) {
            EventType eventType = annotationClasses[i].getAnnotation(EventType.class);
            assert eventType != null;
            String eventTypeStr = eventType.targetType().getSimpleName() + ", " + eventType.listenerSetterName()
                    + ", " + eventType.listenerType().getSimpleName() + ", " + eventType.listenerMethodName();
            Log.d(TAG, "get eventType: " + annotationClasses[i].getSimpleName() + "(" + eventTypeStr + ") successfully");
            eventTypes[i] = eventType;
            listenerMethodNames[i] = eventType.listenerMethodName();
            listenerSetMethods[i] = getMethod(eventType.targetType(), eventType.listenerSetterName(), eventType.listenerType());
            if (listenerSetMethods[i] == null) {
                return;
            }
        }
        for (Method method : declaredMethods) {
            for (int i = 0; i < annotationClasses.length; i++) {
                if (method.isAnnotationPresent(annotationClasses[i])) {
                    Annotation annotation = method.getAnnotation(annotationClasses[i]);
                    assert annotation != null;
                    int[] ids;
                    if (annotationClasses[i] == OnClick.class) {
                        ids = ((OnClick) annotation).value();
                    } else {
                        ids = ((OnLongClick) annotation).value();
                    }
                    Class listenerType = eventTypes[i].listenerType();
                    method.setAccessible(true);
                    ProxyHandler proxyHandler = new ProxyHandler(activity);
                    proxyHandler.mapMethod(listenerMethodNames[i], method);
                    Object listener = Proxy.newProxyInstance(listenerType.getClassLoader(), new Class[]{listenerType}, proxyHandler);
                    try {
                        for (int id : ids) {
                            View view = (View) findViewById.invoke(activity, id);
                            assert view != null;
                            listenerSetMethods[i].invoke(view, listener);
                            Log.d(TAG, "bind method(" + method.toGenericString() + ") to " + eventTypes[i].listenerType().getSimpleName()
                                    + "." + listenerMethodNames[i] + " successfully");
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        Log.d(TAG, "bind " + method.toGenericString() + " to " + eventTypes[i].listenerType().getSimpleName()
                                + "." + listenerMethodNames[i] + " failed");
                    }
                }
            }
        }
        Log.d(TAG, "finish injecting events");
    }
}
