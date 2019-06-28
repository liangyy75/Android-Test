package liang.example.androidtest;

class Constants {
    // 使用 ClassLoader 优化，不要一下全部加载 TODO
    // 使用 OpenFIleInput 等等来优化 TODO
    static final Class<?>[] classes = new Class<?>[]{
            liang.example.handlertest.MainActivity.class,
            liang.example.recyclerviewtest.MainActivity.class,
            liang.example.fragmenttest.MainActivity.class,
            liang.example.hotfixtest.MainActivity.class,
    };

    static final String[] names = new String[]{
            "Test Handler",
            "Test RecyclerView",
            "Test Fragment",
            "Test Android Fix",
    };

    static final String[] descs = new String[]{
            "This's a test about MessageQueue / Message / Handler / Looper. And I have tested some examples about " +
                    "Handler.Callback / Handler.postAtTime / Handler.postDelayed / Handler.postAtFrontOfQueue / MessageQueue.postSyncBarrier",
            "This's a test about RecyclerView / Adapter/ ViewHolder / ItemDecoration / ItemAnimator and so on. And I have tested some examples about " +
                    "RecyclerView.bindView / Add or Remove Items / Add Header and Footer / Up fresh and Down more / Swpie remove and Drag change",
            "This's a test about Fragment / FragmentTransaction / FragmentManager. And I have tested some examples about " +
                    "Add and remove fragment dynamically / ViewPager with Fragment / 仿QQ|微信底部菜单.",
            "这是一个关于Android热更新/热修复的测试。我以及进行了关于热更新的测试",
    };

    static final String[] authors = new String[]{
            "liangyy75",
            "liangyy75",
            "liangyy75",
            "liangyy75",
    };

    static final String[] created = new String[]{
            "2019-06-08",
            "2019-06-08",
            "2019-06-09",
            "2019-06-24",
    };

    static final String[] updated = new String[]{
            "2019-06-08",
            "2019-06-10",
            "2019-06-11",
            "2019-06-24",
    };
}
