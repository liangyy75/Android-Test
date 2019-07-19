package liang.example.volleytest;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;

public abstract class GsonRequest<T> extends Request<T> {
    private final static Gson gson = new Gson();

    private final Object mLock = new Object();
    private Response.Listener<T> mListener;
    private Class<T> entityClass;

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public GsonRequest(String url, Response.Listener<T> mListener, Response.ErrorListener listener) {
        super(url, listener);
        this.mListener = mListener;
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        entityClass = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
    }

    public GsonRequest(int method, String url, Response.Listener<T> mListener, @Nullable Response.ErrorListener listener) {
        super(method, url, listener);
        this.mListener = mListener;
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        entityClass = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        String jsonString;
        try {
            jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            jsonString = new String(response.data);
        }
        return Response.success(gson.fromJson(jsonString, entityClass), HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(T response) {
        Response.Listener<T> listener;
        synchronized (mLock) {
            listener = mListener;
        }
        if (listener != null) {
            listener.onResponse(response);
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        synchronized (mLock) {
            mListener = null;
        }
    }
}
