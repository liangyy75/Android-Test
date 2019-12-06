package com.liang.example.apttest;

// TODO
class Constants {

    static final Class<?>[] classes = new Class<?>[]{
            com.liang.example.apttest.bind.MainActivity.class,
            com.liang.example.apttest.dagger2.MainActivity.class,
            com.liang.example.apttest.route.MainActivity.class,
            com.liang.example.apttest.spi.MainActivity.class,
    };

    static final String[] names = new String[]{
            "Test Basic Apt",
            "Test Dagger2",
            "Test Route Apt",
            "Test Spi Apt",
    };

    static final String[] descs = new String[]{
            "反射注解与动态代理综合使用",
            "依赖注入的利器Dagger2",
            "Apt实现页面跳转代码自动生成",
            "Spi: IOC开发模式",
    };

    static final String[] authors = new String[]{
            "liangyy75",
            "liangyy75",
            "liangyy75",
            "liangyy75",
    };

    static final String[] created = new String[]{
            "2019-07-15",
            "2019-08-31",
            "2019-08-25",
            "2019-09-11",
    };

    static final String[] updated = new String[]{
            "2019-07-15",
            "2019-11-19",
            "2019-08-29",
            "2019-09-11",
    };
}
