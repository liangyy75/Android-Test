package liang.example.apttest.bind;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

class ProxyHandler implements InvocationHandler {
    private WeakReference<Activity> handler;
    private HashMap<String, Method> methodHashMap;

    ProxyHandler(Activity activity) {
        handler = new WeakReference<>(activity);
        methodHashMap = new HashMap<>();
    }

    public void mapMethod(String name, Method method) {
        methodHashMap.put(name, method);
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        Activity activity = handler.get();
        if (null == activity) return null;
        Method realMethod = methodHashMap.get(method.getName());
        if (realMethod != null) {
            return realMethod.invoke(activity, args);
        }
        return null;
    }
}
