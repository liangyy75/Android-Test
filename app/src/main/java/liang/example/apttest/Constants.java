package liang.example.apttest;

// TODO
class Constants {

    static final Class<?>[] classes = new Class<?>[]{
            liang.example.apttest.bind.MainActivity.class,
            liang.example.apttest.dagger2.MainActivity.class,
            liang.example.apttest.route.MainActivity.class,
    };

    static final String[] names = new String[]{
            "Test Basic Apt",
            "Test Dagger2",
            "Test Route Apt",
    };

    static final String[] descs = new String[]{
            "反射注解与动态代理综合使用",
            "依赖注入的利器Dagger2",
            "反射注解与动态代理综合使用",
            "Apt实现页面跳转代码自动生成",
    };

    static final String[] authors = new String[]{
            "liangyy75",
            "liangyy75",
            "liangyy75",
    };

    static final String[] created = new String[]{
            "2019-07-15",
            "2019-08-31",
            "2019-08-25",
    };

    static final String[] updated = new String[]{
            "2019-07-15",
            "2019-08-31",
            "2019-08-29",
    };
}
