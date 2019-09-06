package com.liang.example.hotfixtest;

import android.content.Context;
import android.widget.Toast;

class BugClass {
    BugClass(Context context) {
        Toast.makeText(context, "这是一个优美的bug", Toast.LENGTH_LONG).show();
        // Toast.makeText(context,"你很优秀！bug已修复\uD83D\uDE02\uD83D\uDE02\uD83D\uDE02",Toast.LENGTH_SHORT).show();
    }
}
