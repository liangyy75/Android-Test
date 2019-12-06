package com.liang.example.apttest.dagger2.demo2;

import dagger.Module;
import dagger.Provides;

@Module
public class CarModule2 {
    public CarModule2() {
    }

    @Provides
    Engine2 provideEngine2() {
        return new Engine2("引擎2转起来了~~~");
    }
}
