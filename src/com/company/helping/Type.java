package com.company.helping;

public enum Type {
    Operators("+-{}()^*%/"),
    Semicolon(";"),
    Letter("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    Digit("0123456789"),
    Dot("."),
    Colon(":"),
    Eq("="),
    And("&"),
    Or("|"),
    ExM("!"),
    WS(" \t"),
    NL("\n\r"),
    MoreLess("><"),
    Coma(","),
    Other("");

    private final String str;

    Type(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }
}
