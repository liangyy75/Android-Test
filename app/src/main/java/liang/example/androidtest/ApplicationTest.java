package liang.example.androidtest;

import android.app.Application;
import android.util.Log;

import liang.example.utils.ApiManager;

/**
 * 能实现的功能:
 * 1. 数据传递
 * 2. 数据共享
 * 3. 数据缓存
 * 4. Activity监控
 * <p>
 * 注意点:
 * 1. 使用Application如果保存了一些不该保存的对象很容易导致内存泄漏。如果在Application的oncreate中执行比较耗时的操作，将直接影响的程序的启动时间。
 * 清理工作不能依靠onTerminate完成，因为android会尽量让你的程序一直运行，所以很有可能 onTerminate不会被调用。
 * 2.
 */
public class ApplicationTest extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ApplicationTest", "onCreate");
        ApiManager.init(this.getApplicationContext());
    }
}
