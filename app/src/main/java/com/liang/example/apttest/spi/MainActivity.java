package com.liang.example.apttest.spi;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.liang.example.androidtest.R;
import com.liang.example.apttest.route.Route;
import com.liang.example.spi_interface.SpiDisplay;
import com.liang.example.utils.ApiManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

@Route(path = "apt_spi_main")
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apt_spi);
        ServiceLoader<SpiDisplay> loader = ServiceLoader.load(SpiDisplay.class);
        Iterator<SpiDisplay> iterator = loader.iterator();
        List<String> dataList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            String data = iterator.next().display();
            dataList.add(data);
            sb.append(data).append("\n");
        }
        ((ListView) findViewById(R.id.test_apt_spi_list)).setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList));
        ApiManager.LOGGER.d("Spi_Main", "data: %s", sb.toString());
    }
}
