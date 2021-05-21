package com.company.exceptions;

public class InterpreterErrorException extends Exception {
    private final int code;

    public InterpreterErrorException(String s, int code) {
        super(s);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
