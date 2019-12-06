package com.liang.example.apttest.dagger2.demo2;

import dagger.Component;

@Component(modules = {CarModule2.class})
public interface CarComponent2 {
    void inject(Car2 car2);
}
