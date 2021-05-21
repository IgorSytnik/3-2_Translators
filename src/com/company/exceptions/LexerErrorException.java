package com.company.exceptions;

public class LexerErrorException extends Exception {
    final int state;

    public LexerErrorException(String s, int state) {
        super(s);
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
