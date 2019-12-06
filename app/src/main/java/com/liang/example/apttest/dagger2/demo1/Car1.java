package com.liang.example.apttest.dagger2.demo1;

import javax.inject.Inject;

public class Car1 {
    @Inject
    Engine1 engine1;

    public Car1() {
        DaggerCarComponent1.builder().build().inject(this);
        // DaggerCarComponent1.create().inject(this);
    }

    public Engine1 getEngine() {
        return this.engine1;
    }
}
