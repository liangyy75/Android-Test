package liang.example.fragmenttest;

// TODO
class Constants {
    // 使用 ClassLoader 优化，不要一下全部加载 TODO
    // 使用 OpenFIleInput 等等来优化 TODO
    static final Class<?>[] classes = new Class<?>[]{
            liang.example.fragmenttest.bottombar.MainActivity.class,
            liang.example.fragmenttest.bottombar2.MainActivity.class,
    };

    static final String[] names = new String[]{
            "Test Bottom bar by Fragment and ViewPager",
            "Test Bottom bar by Fragment 、 FragmentTransaction and FragmentManager",
    };

    static final String[] descs = new String[]{
            "模仿QQ微信底部导航菜单",
            "模仿QQ微信底部导航菜单",
    };

    static final String[] authors = new String[]{
            "liangyy75",
            "liangyy75",
    };

    static final String[] created = new String[]{
            "2019-06-09",
            "2019-06-11",
    };

    static final String[] updated = new String[]{
            "2019-06-11",
            "2019-06-11",
    };
}
