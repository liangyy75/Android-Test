package com.liang.example.spi_impl2;

import com.liang.example.spi_interface.SpiDisplay;

public class SpiDisplay2 implements SpiDisplay {
    @Override
    public String display() {
        return "SpiDisplay2: This is display in module spi_impl2";
    }
}
