package com.liang.example.volleytest;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;

public class XmlRequest extends Request<XmlPullParser> {
    private final Object mLock = new Object();
    private Response.Listener<XmlPullParser> mListener;

    public XmlRequest(String url, Response.Listener<XmlPullParser> mListener, Response.ErrorListener listener) {
        super(url, listener);
        this.mListener = mListener;
    }

    public XmlRequest(int method, String url, Response.Listener<XmlPullParser> mListener, @Nullable Response.ErrorListener listener) {
        super(method, url, listener);
        this.mListener = mListener;
    }

    @Override
    public void cancel() {
        super.cancel();
        synchronized (mLock) {
            mListener = null;
        }
    }

    @Override
    protected Response<XmlPullParser> parseNetworkResponse(NetworkResponse response) {
        String xmlString;
        try {
            xmlString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            // return Response.error(new ParseError(e));
            xmlString = new String(response.data);
        }
        try {
            XmlPullParser xmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
            xmlPullParser.setInput(new StringReader(xmlString));
            return Response.success(xmlPullParser, HttpHeaderParser.parseCacheHeaders(response));
        } catch (XmlPullParserException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(XmlPullParser response) {
        Response.Listener<XmlPullParser> listener;
        synchronized (mLock) {
            listener = mListener;
        }
        if (listener != null) {
            listener.onResponse(response);
        }
    }
}
