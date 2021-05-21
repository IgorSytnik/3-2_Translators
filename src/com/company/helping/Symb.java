package com.company.helping;

public class Symb {
    public int lineNumber;
    public String lexeme;
    public String token;
    public int index;

    public Symb(int lineNumber, String lexeme, String token, int index) {
        this.lineNumber = lineNumber;
        this.lexeme = lexeme;
        this.token = token;
        this.index = index;
    }

    public Symb(int lineNumber, String lexeme, String token) {
        this.lineNumber = lineNumber;
        this.lexeme = lexeme;
        this.token = token;
        this.index = -1;
    }

    public Symb(Symb symb) {
        this.lineNumber = symb.lineNumber;
        this.lexeme = symb.lexeme;
        this.token = symb.token;
        this.index = symb.index;
    }

    public void printfStuff() {
        if (index != -1) {
            System.out.printf("%4d  %-12s   %-12s   %4d \n", lineNumber, lexeme, token, index);
        } else {
            System.out.printf("%4d  %-12s   %-12s \n", lineNumber, lexeme, token);
        }
    }

    @Override
    public String toString() {
        return index != -1
                ? "(" + lineNumber + ", '" + lexeme + "', '" + token + "', " + index + ")"
                : "(" + lineNumber + ", '" + lexeme + "', '" + token + "', '')";
    }
}
