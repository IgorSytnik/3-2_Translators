package com.company.helping;

public class Value {
    public int index;
    public boolean ident;
    public String value;
    public String type;

    public Value(int index, String value, String type) {
        this.ident = false;
        this.index = index;
        this.value = value;
        this.type = type;
    }

    public Value(boolean ident, int index, String value, String type) {
        this.ident = ident;
        this.index = index;
        this.value = value;
        this.type = type;
    }

    public Value(String value, String type) {
        this.index = -1;
        this.ident = false;
        this.value = value;
        this.type = type;
    }

//    public String getName() {
//        return name;
//    }
//
//    public void printfStuff() {
//        System.out.printf("%-12s  %-12s   %-12s   %4d \n", name, type, value, index);
//    }
//
    @Override
    public String toString() {
        return "(" + index + ", '" + type + "', '" + value + "')";
    }
}
