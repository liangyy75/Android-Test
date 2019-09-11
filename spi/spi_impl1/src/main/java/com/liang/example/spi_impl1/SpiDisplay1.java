package com.liang.example.spi_impl1;

import com.liang.example.spi_interface.SpiDisplay;

public class SpiDisplay1 implements SpiDisplay {
    @Override
    public String display() {
        return "SpiDisplay1: This is display in module spi_impl1";
    }
}
