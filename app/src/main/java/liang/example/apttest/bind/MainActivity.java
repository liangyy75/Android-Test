package liang.example.apttest.bind;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import liang.example.androidtest.R;

public class MainActivity extends AppCompatActivity {
    @InjectView(R.id.test_apt_base_ok)
    private Button ok;
    @InjectView(R.id.test_apt_base_cancel)
    private Button cancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apt_base);

        InjectUtils.getInstance().injectViews(this);
        // ok.setOnLongClickListener(view -> {
        //     Toast.makeText(MainActivity.this, "Ok", Toast.LENGTH_SHORT).show();
        //     return false;
        // });
        // cancel.setOnLongClickListener(view -> {
        //     Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
        //     return false;
        // });

        InjectUtils.getInstance().injectEvents(this);
    }

    @OnClick({R.id.test_apt_base_ok, R.id.test_apt_base_cancel})
    public void invokeBtnClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(this.getClass().getSimpleName());
        switch (view.getId()) {
            case R.id.test_apt_base_ok:
                builder.setMessage("Apt Base Ok")
                        .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());
                break;
            case R.id.test_apt_base_cancel:
                builder.setMessage("Apt Base Cancel")
                        .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());
        }
        builder.create().show();
    }

    // @OnLongClick({R.id.test_apt_base_ok, R.id.test_apt_base_cancel})
    // public boolean invokeBtnLongClick(View view) {
    //     switch(view.getId()) {
    //         case R.id.test_apt_base_ok:
    //             Toast.makeText(MainActivity.this, "Ok", Toast.LENGTH_SHORT).show();
    //             break;
    //         case R.id.test_apt_base_cancel:
    //             Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
    //     }
    //     return true;
    // }

    @OnLongClick({R.id.test_apt_base_ok})
    public boolean invokeBtnLongClickOk(View view) {
        Toast.makeText(MainActivity.this, "Ok", Toast.LENGTH_SHORT).show();
        return true;
    }

    @OnLongClick({R.id.test_apt_base_cancel})
    public boolean invokeBtnLongClickCancel(View view) {
        Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
        return true;
    }
}
