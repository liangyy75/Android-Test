package com.liang.example.volleytest;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.liang.example.androidtest.R;
import com.liang.example.apttest.bind.InjectUtils;
import com.liang.example.apttest.bind.InjectView;
import com.liang.example.utils.ApiManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "VolleyTest";

    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    @InjectView(R.id.test_volley_image)
    private ImageView imageView;
    @InjectView(R.id.test_volley_net_image)
    private NetworkImageView networkImageView;
    private String image_url = "https://images.pexels.com/photos/67636/rose-blue-flower-rose-blooms-67636.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500";
    private String json_url = "http://www.weather.com.cn/data/sk/101010100.html";
    String string_url = "https://www.baidu.com";
    String xml_url = "http://flash.weather.com.cn/wmaps/xml/china.xml";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_volley);
        ApiManager.LOGGER.d(TAG, "onCreate -- start");

        HTTPSTrustManager.allowAllSSL();
        requestQueue = Volley.newRequestQueue(this);
        InjectUtils.getInstance().injectViews(this);

        testStringRequest();
        testJsonRequest();
        testImageRequest();
        getImageLoader();
        testImageLoader();
        testNetworkImageView();
        testXmlRequest();
        testGsonRequest();
    }

    private void testGsonRequest() {
        final Gson gson = new Gson();
        try {
            GsonRequest<Weather> gsonRequest = new GsonRequest<Weather>(json_url,
                    response -> ApiManager.LOGGER.d(TAG, "get json successfully. " + gson.toJson(response)),
                    error -> ApiManager.LOGGER.e(TAG, "get json failed.", error)) {
            };
            ApiManager.LOGGER.d(TAG, "gsonRequest's entityClass: " + gsonRequest.getEntityClass().getSimpleName());
            requestQueue.add(gsonRequest);
        } catch (Exception e) {
            ApiManager.LOGGER.e(TAG, "cast error!", e);
        }
    }

    private void testXmlRequest() {
        XmlRequest xmlRequest = new XmlRequest(xml_url, response -> {
            ApiManager.LOGGER.d(TAG, "get xml successfully. ");
            try {
                int eventType = response.getEventType();
                StringBuilder sb = new StringBuilder();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        String nodeName = response.getName();
                        if ("city".equals(nodeName)) {
                            String pName = response.getAttributeValue(0);
                            sb.append("pName is ").append(pName).append("\n");
                        }
                    }
                    eventType = response.next();
                }
                ApiManager.LOGGER.d(TAG, "content of xml is:\n%s", sb.toString());
            } catch (XmlPullParserException e) {
                ApiManager.LOGGER.d(TAG, "parse xml failed -- XmlPullParserException.", e);
            } catch (IOException e) {
                ApiManager.LOGGER.d(TAG, "parse xml failed -- IOException.", e);
            }
        }, error -> ApiManager.LOGGER.d(TAG, "get xml failed.", error));
        requestQueue.add(xmlRequest);
    }

    private void testNetworkImageView() {
        // networkImageView.setDefaultImageResId(R.drawable.placeholder);
        // networkImageView.setErrorImageResId(R.drawable.error);
        networkImageView.setImageUrl(image_url, imageLoader);
    }

    private void testImageLoader() {
        imageLoader.get(image_url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                ApiManager.LOGGER.d(TAG, "get image successfully");
                imageView.setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                ApiManager.LOGGER.d(TAG, "get image failed", error);
            }
        });
    }

    private void getImageLoader() {
        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            private LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(10 * 1024 * 1024) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }
            };

            @Override
            public Bitmap getBitmap(String url) {
                return lruCache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                lruCache.put(url, bitmap);
            }
        });
    }

    private void testImageRequest() {
        ImageRequest imageRequest = new ImageRequest(image_url, response -> {
            ApiManager.LOGGER.d(TAG, "get image successfully!");
            imageView.setImageBitmap(response);
        }, 200, 200, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565, error -> ApiManager.LOGGER.d(TAG, "get image failed", error));
        requestQueue.add(imageRequest);
    }

    private void testJsonRequest() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, json_url, null,
                response -> ApiManager.LOGGER.d(TAG, "json response: " + response.toString()), error -> ApiManager.LOGGER.e(TAG, "json error: ", error));
        requestQueue.add(jsonObjectRequest);
    }

    private void testStringRequest() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, string_url,
                response -> ApiManager.LOGGER.d(TAG, "string response: " + response), error -> ApiManager.LOGGER.e(TAG, "string error: ", error));
        requestQueue.add(stringRequest);
    }
}
