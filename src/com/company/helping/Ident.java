package com.company.helping;

public class Ident {
    public int index;
    public String name;
    public String type;
    public String value;

    public Ident(int index, String name, String type, String value) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void printfStuff() {
        System.out.printf("%-12s  %-12s   %-12s   %4d \n", name, type, value, index);
    }

    @Override
    public String toString() {
        return "(" + name + ", '" + type + "', '" + value + "', " + index + ")";
    }
}
