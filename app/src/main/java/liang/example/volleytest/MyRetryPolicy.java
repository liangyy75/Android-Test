package liang.example.volleytest;

import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;

// TODO
public class MyRetryPolicy implements RetryPolicy {
    @Override
    public int getCurrentTimeout() {
        return 0;
    }

    @Override
    public int getCurrentRetryCount() {
        return 0;
    }

    @Override
    public void retry(VolleyError error) throws VolleyError {}
}
