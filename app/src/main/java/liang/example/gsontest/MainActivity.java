package liang.example.gsontest;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import liang.example.androidtest.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fix_test);

        String json = " {\n" + "     \"type\": \"cmd\",\n" + "     \"cmd\": [\n"
                + "         \"sh -c ps | grep -e kiwi -e PID\",\n" + "         \"cd /sdcard/kiwi/logs/\",\n"
                + "         \"cat logs-last.xlog | wc -l\",\n" + "         \"ls | head -n 3\"\n" + "     ]\n" + " }\n";
        BasicMsg<Object> objectBasicMsg = new Gson().fromJson(json, new TypeToken<BasicMsg<Object>>(){}.getType());
        Log.d("TestGson", objectBasicMsg.getType());
        // TODO
    }
}
