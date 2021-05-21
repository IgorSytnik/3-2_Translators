package com.company.exceptions;

public class ParserErrorException extends Exception {
    private final int code;

    public ParserErrorException(String s, int code) {
        super(s);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
