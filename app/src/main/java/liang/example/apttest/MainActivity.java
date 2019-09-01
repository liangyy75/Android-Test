package liang.example.apttest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import liang.example.androidtest.ActivityItem;
import liang.example.androidtest.R;
import liang.example.recyclerviewtest.RVAdapterTest;
import liang.example.recyclerviewtest.RVViewHolderTest;
import liang.example.utils.ApiManager;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "AptTestMainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ApiManager.LOGGER.d(TAG, "onCreate -- begin creating Activity List");
        int length = Constants.classes.length;
        List<ActivityItem> activityItemList = new ArrayList<>(length);
        for (int i = 0; i < length; i++)
            activityItemList.add(new ActivityItem(Constants.names[i], Constants.descs[i],
                    Constants.authors[i], Constants.created[i], Constants.updated[i], Constants.classes[i]));

        RecyclerView activityList = findViewById(R.id.test_activity_list);
        activityList.setHasFixedSize(true);
        LinearLayoutManager activityLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        activityLayoutManager.setInitialPrefetchItemCount(4);
        activityLayoutManager.setItemPrefetchEnabled(true);
        activityList.setLayoutManager(activityLayoutManager);

        RVAdapterTest<ActivityItem> activityAdapter = new RVAdapterTest<ActivityItem>(activityItemList, this, R.layout.item_activity_list, activityList) {
            public void bindView(RVViewHolderTest viewHolder, ActivityItem data, int position) {
                viewHolder.getViewById(R.id.test_activity_item_image).setBackgroundResource(R.mipmap.ic_launcher);
                ((TextView) viewHolder.getViewById(R.id.test_activity_item_name)).setText(data.getName());
                ((TextView) viewHolder.getViewById(R.id.test_activity_item_message)).setText(
                        String.format("%s / %s / %s", data.getAuthor(), data.getCreated(), data.getUpdated()));
                ((TextView) viewHolder.getViewById(R.id.test_activity_item_desc)).setText(data.getDesc());
            }
        };
        activityAdapter.setOnItemClickListener(new RVAdapterTest.OnItemClickListener<ActivityItem>() {
            @Override
            public void onItemClick(View view, ActivityItem data, int position) {
                startActivity(new Intent(MainActivity.this, data.getClazz()));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }

            @Override
            public boolean onItemLongClick(View view, ActivityItem data, int position) {
                Toast.makeText(MainActivity.this, data.getDesc(), Toast.LENGTH_LONG).show();
                return false;
            }
        });
        activityList.setAdapter(activityAdapter);
        ApiManager.LOGGER.d(TAG, "onCreate -- finish creating Activity List");
    }
}
