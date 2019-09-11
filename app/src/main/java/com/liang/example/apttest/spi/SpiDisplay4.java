package com.liang.example.apttest.spi;

import com.liang.example.spi_interface.SpiDisplay;

public class SpiDisplay4 implements SpiDisplay {
    @Override
    public String display() {
        return "SpiDisplay4: This is display in module app";
    }
}
