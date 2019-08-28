package liang.example.okhttptest;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import liang.example.androidtest.R;
import liang.example.apttest.bind.InjectUtils;
import liang.example.apttest.bind.InjectView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "OkHttpTest";
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    private OkHttpClient client;
    @InjectView(R.id.test_volley_image)
    private ImageView imageView;
    private String image_url = "https://images.pexels.com/photos/67636/rose-blue-flower-rose-blooms-67636.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500";
    private String json_url = "http://www.weather.com.cn/data/sk/101010100.html";
    private String xml_url = "http://flash.weather.com.cn/wmaps/xml/china.xml";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_volley);
        // TODO
        client = new OkHttpClient();
        InjectUtils.getInstance().injectViews(this);

        String get_url = "https://www.baidu.com";
        synchronousGet(get_url);
        asynchronousGet(get_url);
        String post_url = "";
        String post_body = "";
        Map<String, String> map = Collections.emptyMap();
        synchronousPost1(post_url, post_body);
        synchronousPost2(post_url, map);
        asynchronousStreamPost3("https://api.github.com/markdown/raw");
    }

    private void asynchronousStreamPost3(String url) {
        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MEDIA_TYPE_MARKDOWN;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.writeUtf8("Numbers\n");
                sink.writeUtf8("-------\n");
                for (int i = 2; i <= 997; i++) {
                    sink.writeUtf8(String.format(" * %s = %s\n", i, factor(i)));
                }
            }

            private String factor(int n) {
                for (int i = 2; i < n; i++) {
                    int x = n / i;
                    if (x * i == n) return factor(x) + " × " + i;
                }
                return Integer.toString(n);
            }
        };
        Request request = new Request.Builder().url(url).post(requestBody).build();
        asynchronousReq(request, "asynchronousStreamPost3");
    }

    private void asynchronousGet(String url) {
        Request request = new Request.Builder().tag(this).url(url).get().build();
        asynchronousReq(request, "asynchronousGet");
    }

    private void asynchronousReq(Request request, final String name) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, name + " onFailure", e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                showRes(response, name);
            }
        });
    }

    private void synchronousPost2(String url, Map<String, String> map) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        Request request = new Request.Builder().tag(this).url(url).post(builder.build()).build();
        synchronousReq(request, "synchronousPost2");
    }

    private void synchronousPost1(String url, String body) {
        // Request request = new Request.Builder().tag(this).url(url).post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"), body)).build();
        Request request = new Request.Builder().tag(this).url(url).post(FormBody.create(body, MediaType.parse("application/json;charset=utf-8"))).build();
        synchronousReq(request, "synchronousPost1");
    }

    private void synchronousGet(String url) {
        Request request = new Request.Builder().tag(this).url(url).get().build();  // 这里的 get 其实是没有必要的，默认的就是 get
        synchronousReq(request, "synchronousGet");
    }

    private void synchronousReq(Request request, String name) {
        try {
            Response response = client.newCall(request).execute();
            showRes(response, name);
        } catch (IOException e) {
            Log.e(TAG, name + " error: ", e);
        }
    }

    private void showRes(Response response, String name) throws IOException {
        if (response.isSuccessful()) {
            Log.d(TAG, name + ": " + Objects.requireNonNull(response.body()).string() + "; code: " + response.code());
            Headers headers = response.headers();
            for (int i = 0; i < headers.size(); i++) {
                Log.d(TAG, "index: " + i + "; " + headers.name(i) + ": " + headers.value(i));
            }
        } else {
            Log.d(TAG, name + " emtpy body: " + response.message() + "; code: " + response.code());
        }
    }
}
