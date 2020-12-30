package com.company;

import java.io.Serializable;

public class CityData implements Serializable {
    private final String name;
    private int count;

    public CityData(String name, int count) {
        this.count = count;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void addToCount(int count) {
        this.count += count;
    }

    public String toString() {
        return String.format("%s: %d", this.name, this.count);
    }
}
