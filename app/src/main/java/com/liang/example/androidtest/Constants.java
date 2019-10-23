package com.liang.example.androidtest;

class Constants {
    // 使用 ClassLoader 优化，不要一下全部加载 TODO
    // 使用 OpenFIleInput 等等来优化 TODO
    static final Class<?>[] classes = new Class<?>[]{
            com.liang.example.handlertest.MainActivity.class,
            com.liang.example.recyclerviewtest.MainActivity.class,
            com.liang.example.fragmenttest.MainActivity.class,
            com.liang.example.hotfixtest.MainActivity.class,
            com.liang.example.apttest.MainActivity.class,
            com.liang.example.gsontest.MainActivity.class,
            com.liang.example.volleytest.MainActivity.class,
            com.liang.example.shelltest.MainActivity.class,
            com.liang.example.musictest.LoadActivity.class,
            com.liang.example.viewtest.MainActivity.class,
            com.liang.example.jetpacktest.MainActivity.class,
    };

    static final String[] names = new String[]{
            "Test Handler",
            "Test RecyclerView",
            "Test Fragment",
            "Test Android Fix",
            "Test Apt",
            "Test Gson",
            "Test Volley",
            "Test Remote Controller",
            "Test Music",
            "Test View",
            "Test Android Jetpack",
    };

    static final String[] descs = new String[]{
            "This's a test about MessageQueue / Message / Handler / Looper. And I have tested some examples about " +
                    "Handler.Callback / Handler.postAtTime / Handler.postDelayed / Handler.postAtFrontOfQueue / MessageQueue.postSyncBarrier",
            "This's a test about RecyclerView / Adapter/ ViewHolder / ItemDecoration / ItemAnimator and so on. And I have tested some examples about " +
                    "RecyclerView.bindView / Add or Remove Items / Add Header and Footer / Up fresh and Down more / Swpie remove and Drag change",
            "This's a test about Fragment / FragmentTransaction / FragmentManager. And I have tested some examples about " +
                    "Add and remove fragment dynamically / ViewPager with Fragment / 仿QQ|微信底部菜单.",
            "这是一个关于Android热更新/热修复的测试。我以及进行了关于热更新的测试",
            "Apt，即Annotation Processing Tool，就是可以在代码编译期间对注解进行处理，并且生成Java文件，减少手动的代码输入。",
            "Some test about gson",
            "Some test about volley: StringRequest / JsonRequest / ImageRequest / ImageLoader / NetworkImageView / XmlRequest / GsonRequest",
            "Some test about remote control",
            "Just some test about a future music app",
            "一些关于View的测试，包括自定义View、探索常用View和View常用机制",
            "Some tests about Jetpack, including lifecycle / livedata / viewmodel / dataBinding / paging / room / navigation / workManger / ...",
    };

    static final String[] authors = new String[]{
            "liangyy75",
            "liangyy75",
            "liangyy75",
            "liangyy75",
            "liangyy75",
            "liangyy75",
            "liangyy75",
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
            "2019-07-15",
            "2019-07-15",
            "2019-07-19",
            "2019-09-06",
            "2019-09-16",
            "2019-09-18",
            "2019-10-23",
    };

    static final String[] updated = new String[]{
            "2019-06-08",
            "2019-06-10",
            "2019-06-11",
            "2019-06-24",
            "2019-07-15",
            "2019-08-26",
            "2019-07-19",
            "2019-09-06",
            "2019-09-16",
            "2019-09-18",
            "2019-10-23",
    };
}
