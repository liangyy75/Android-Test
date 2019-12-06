package com.liang.example.apttest.dagger2.demo2;

import javax.inject.Inject;

public class Car2 {
    @Inject
    Engine2 engine2;

    public Car2() {
        // DaggerCarComponent2.create().inject(this);
        DaggerCarComponent2.builder().build().inject(this);
        // DaggerCarComponent2.builder().carModule2(new CarModule2()).build().inject(this);
    }

    public Engine2 getEngine() {
        return engine2;
    }
}
