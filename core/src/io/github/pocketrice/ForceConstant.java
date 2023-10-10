package io.github.pocketrice;

public enum ForceConstant { // https://nssdc.gsfc.nasa.gov/planetary/factsheet/planet_table_ratio.html
    EARTH_G(9.8f),
    MOON_G(0.166f * EARTH_G.gConst),
    MARS_G(0.377f * EARTH_G.gConst),
    VENUS_G(0.907f * EARTH_G.gConst);

    ForceConstant(float g) {
        gConst = g;
    }

    float gConst;
}
