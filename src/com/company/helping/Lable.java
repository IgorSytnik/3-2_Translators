package com.company.helping;

public class Lable {
    public int index;
    public String name;
    public int valueGoTo;

    public Lable(int index, String name, int value) {
        this.index = index;
        this.name = name;
        this.valueGoTo = value;
    }

    public String getName() {
        return name;
    }

    public void setValue(int value) {
        this.valueGoTo = value;
    }

    public void printfStuff() {
        System.out.printf("%-12s  %-12s   %4d \n", name, valueGoTo, index);
    }

    @Override
    public String toString() {
        return "(" + name + ", " + valueGoTo + ", " + index + ")";
    }
}
