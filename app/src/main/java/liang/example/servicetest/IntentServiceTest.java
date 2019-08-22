package liang.example.servicetest;

import android.app.IntentService;
import android.content.Intent;

// TODO
public class IntentServiceTest extends IntentService {

    public IntentServiceTest() {
        super("default name");
    }

    public IntentServiceTest(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //
    }
}
