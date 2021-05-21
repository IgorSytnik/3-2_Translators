package com.company;

import com.company.exceptions.LexerErrorException;
import com.company.helping.Ident;
import com.company.helping.Symb;
import com.company.helping.Type;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Optional;
import java.util.stream.IntStream;

public class Lex {

    final int initState = 0;
    int state = initState;
    int numChar = 0;
    int numCharInLine = 0;
    char ch;
    Type classCh;

    String sourceCode;
    int lenCode;
    int numLine = 1;
    StringBuilder lexeme = new StringBuilder();

    final int[][] stfNodes = {{0, 0},     {0, 0},     {0, 1},     {0, 2},     {0, 3},
                        {3, 3},     {3, 3},     {3, 4},     {0, 5},     {5, 5},
                        {5, 6},     {0, 7},     {7, 7},     {7, 5},     {7, 8},
                        {0, 9},     {9, 10},    {9, 105},   {0, 11},    {11, 12},
                        {11, 104},  {0, 13},    {13, 103},  {13, 14},   {0, 15},
                        {15, 16},   {15, 18},   {0, 17},    {17, 18},   {17, 102},
                        {0, 20},    {20, 18},   {20, 19},   {0, 101},   {0, 21},    {0, 22}};

    final Type[] stfTypes = { Type.WS,     Type.NL, Type.Operators, Type.Semicolon, Type.Letter,
                        Type.Digit,     Type.Letter,Type.Other,     Type.Dot,       Type.Digit,
                        Type.Other,     Type.Digit, Type.Digit,     Type.Dot,       Type.Other,
                        Type.Colon,     Type.Eq,    Type.Other,     Type.And,       Type.And,
                        Type.Other,     Type.Or,    Type.Other,     Type.Or,        Type.ExM,
                        Type.Other,     Type.Eq,    Type.Eq,        Type.Eq,        Type.Other,
                        Type.MoreLess,  Type.Eq,    Type.Other,     Type.Other,     Type.Other,     Type.Coma };

    final String[] tokenNames = {     "keyword",          "keyword",      "keyword",      "keyword",
                                "keyword",          "keyword",      "keyword",      "keyword",
                                "keyword",          "bool",         "bool",         "assign_op",
                                "add_op",           "add_op",       "mult_op",      "mult_op",
                                "mult_op",          "pow_op",       "rel_op",       "rel_op",
                                "rel_op",           "rel_op",       "rel_op",       "rel_op",
                                "log_op",           "log_op",       "log_op",       "brackets_left",
                                "brackets_right",   "braces_left",  "braces_right", "dot",
                                "semicolon",        "nl",           "nl",           "ws",
                                "ws",               "eof",          "keyword",      "keyword",
                                "keyword",          "coma"};

    final String[] tokenStrings = {   "instruction",  "read",     "write",    "for",
                                "endfor",       "if",       "then",     "else",
                                "endif",        "true",     "false",    ":=",
                                "+",            "-",        "*",        "/",
                                "%",            "^",        "<",        "<=",
                                "==",           ">=",       ">",        "!=",
                                "&&",           "||",       "!",        "(",
                                ")",            "{",        "}",        ".",
                                ";",            "\n",       "\r",       " ",
                                "\t",           "\0",       "int",      "real",
                                "bool",         ","};

    final int[] F;
    final int FSemicolon = 2;
    final int FNl = 21;
    final int[] FOther = {1,10,12,14,18,21,22};
    final int[] FRelWierd = {16,19};
    final int[] FError = {101,102,103,104,105};
    final int[] FStar;
    final int[] FIdentFloatInt = {4,6,8};
    final String[] tableIdentFloatInt = {"ident", "real", "int"};

    List<Symb> tableOfSymb = new ArrayList<>();
    List<Ident> tableOfId = new ArrayList<>();
    List<String> tableOfLit = new ArrayList<>();

    Lex() {
        int ifiLen = FIdentFloatInt.length;
        int wierdLen = FRelWierd.length;

        FStar = new int[ifiLen + wierdLen];
        System.arraycopy(FIdentFloatInt, 0, FStar, 0, ifiLen);
        System.arraycopy(FRelWierd, 0, FStar, ifiLen, wierdLen);

        int aLen = FStar.length;
        int bLen = FOther.length;
        int cLen = FError.length;
        int dLen = FRelWierd.length;

        F = new int[aLen + bLen + cLen + dLen + 2];
        System.arraycopy(FStar, 0, F, 0, aLen);
        System.arraycopy(FOther, 0, F, aLen, bLen);
        System.arraycopy(FError, 0, F, aLen + bLen, cLen);
        System.arraycopy(FRelWierd, 0, F, aLen + bLen + cLen, dLen);
        F[aLen + bLen + cLen + dLen] = FSemicolon;
        F[aLen + bLen + cLen + dLen + 1] = FNl;
    }

    public void setSourceCode(String filePath) {

        try {
            this.sourceCode =  new String ( Files.readAllBytes( Paths.get(filePath) ) );
            this.lenCode = this.sourceCode.length();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean lex() {
        try {
            while (numChar < lenCode) {
                ch = nextChar();
                classCh = classOfChar(ch);
                state = nextState();
                if (ch == '\n') {
                    numLine++;
                    numCharInLine = 0;
                } else if (isFinalNode()) {
                    processing();
                } else if (state == initState) {
                    lexeme = new StringBuilder();
                } else {
                    lexeme.append(ch);
                }
            }
            System.out.println("Lexer: Лексичний аналіз завершено успішно");
            printTables();
        } catch (LexerErrorException e) {
            System.out.println("----------------------------------------------");
            System.out.println(lexeme);
            System.out.println(e.getMessage());
            System.out.println("Lexer: Аварійне завершення програми з кодом " + e.getState());
            return false;
        }
        return true;
    }

    private void processing() throws LexerErrorException {
        String token;
        if (IntStream.of(FStar).anyMatch(x -> x == state)) {    // keyword, ident, real, int, <, >, !
            token = getToken();
            if (!token.equals("keyword") && !token.equals("bool")) {
                tableOfSymb.add(
                        new Symb(numLine, lexeme.toString(), token, indexIdConst()));
            } else {
                tableOfSymb.add(
                        new Symb(numLine, lexeme.toString(), token));
            }
            tableOfSymb.get(tableOfSymb.size()-1).printfStuff();
            lexeme = new StringBuilder();
            putCharBack();
            state = initState;
        } else
        if (IntStream.of(FOther).anyMatch(x -> x == state)
                || state == FSemicolon) {   // log_op, rel_op, operators, assign, ;
            lexeme.append(ch);
            token = getToken();
            tableOfSymb.add(new Symb(numLine, lexeme.toString(), token));
            tableOfSymb.get(tableOfSymb.size()-1).printfStuff();
            lexeme = new StringBuilder();
            state = initState;
        } else
        if (IntStream.of(FError).anyMatch(x -> x == state)) {   // error
            fail();
        }
    }

    private void printTables() {
        System.out.println("----------------------------------------------");
        System.out.printf("%-12s", "tableOfSymb: {");
        if (!tableOfSymb.isEmpty()) {
            System.out.print(1 + ": " + tableOfSymb.get(0));
            for (int i = 2; i <= tableOfSymb.size(); i++) {
                System.out.println(",");
                System.out.printf("%14s%s", " ", i + ": " + tableOfSymb.get(i-1));
            }
        }
        System.out.println("}");

        System.out.print("tableOfId: {  ");
        if (!tableOfId.isEmpty()) {
            System.out.print(1 + ": " + tableOfId.get(0));
            for (int i = 2; i <= tableOfId.size(); i++) {
                System.out.println(",");
                System.out.printf("%14s%s", " ", i + ": " + tableOfId.get(i-1));
            }
        }
        System.out.println("}");

        System.out.print("tableOfLit: {");
        if (!tableOfLit.isEmpty()) {
            System.out.print('\'' + tableOfLit.get(0) + "': " + 1);
            for (int i = 2; i <= tableOfLit.size(); i++) {
                System.out.print(", '" + tableOfLit.get(i-1) + "': " + i);
            }
        }
        System.out.println("}");
    }

    private void fail() throws LexerErrorException {
        boolean isPrintableChar = !Character.isISOControl(ch) &&
                                    ch != KeyEvent.CHAR_UNDEFINED &&
                                    Character.UnicodeBlock.of( ch ) != null &&
                                    Character.UnicodeBlock.of( ch ) != Character.UnicodeBlock.SPECIALS;
        System.out.println(numLine);
        switch (state) {
            case 101:
                throw new LexerErrorException(
                        "Lexer: у рядку " + numLine + ":" + numCharInLine +
                                " неочікуваний символ " + "\"" +
                                ((isPrintableChar) ? ch : "char code: " + (int)ch) + "\"",
                        state);
            case 102:
            case 105:
                throw new LexerErrorException(
                        "Lexer: у рядку " + numLine + ":" + numCharInLine +
                                " очікувався символ \"=\", а не \"" +
                                ((isPrintableChar) ? ch : "char code: " + (int)ch) + "\"",
                        state);
            case 103:
                throw new LexerErrorException(
                        "Lexer: у рядку " + numLine + ":" + numCharInLine +
                                " очікувався символ \"|\", а не \"" +
                                ((isPrintableChar) ? ch : "char code: " + (int)ch) + "\"",
                        state);
            case 104:
                throw new LexerErrorException(
                        "Lexer: у рядку " + numLine + ":" + numCharInLine +
                                " очікувався символ \"&\", а не \"" +
                                ((isPrintableChar) ? ch : "char code: " + (int)ch) + "\"",
                        state);
        }
    }

    private int indexIdConst() {
        int index = 0;
        if (state == 6 || state == 8) { // real || int
            index = tableOfLit.indexOf(lexeme.toString());
            if (index == -1) {
                index = tableOfLit.size();
                tableOfLit.add(lexeme.toString());
            }
        } else
        if (state == 4) { // ident
            index = tableOfId.stream()
                    .filter(i -> lexeme.toString().equals(i.getName()))
                    .findFirst()
                    .or(() -> Optional.of(new Ident(-1, "", "", "")))
                    .get().index;
            if (index == -1) {
                index = tableOfId.size();

                tableOfId.add(new Ident(index, lexeme.toString(), "", ""));
            }
        }
        return index;
    }

    private String getToken() {
        for (int i = 0; i < tokenStrings.length; i++) {
            if (tokenStrings[i].equals(lexeme.toString())) {
                return tokenNames[i];
            }
        }
        return tableIdentFloatInt[Arrays.binarySearch(FStar, state)];
    }

    private char nextChar() {
        numCharInLine++;
        return sourceCode.charAt(numChar++);
    }

    private void putCharBack() {
        numChar--;
    }

    private Type classOfChar(char ch) {
        String character = String.valueOf(ch);
        for (int i = 0; i < Type.values().length; i++) {
            if (Type.values()[i].getStr().contains(character)) {
                return Type.values()[i];
            }
        }
        return Type.Other;
    }

    private int nextState() {
        int k = -1;
        for (int i = 0; i < stfNodes.length; i++) {
            if (stfNodes[i][0] == state && stfTypes[i].equals(classCh)) {
                k = i;
                break;
            }
        }
        if (k == -1)
            for (int i = 0; i < stfNodes.length; i++) {
                if (stfNodes[i][0] == state && stfTypes[i].equals(Type.Other)) {
                    k = i;
                    break;
                }
            }
        try {
            if (k == -1) throw new Exception("State: " + state + " Class: " + classCh.name());
        } catch (Exception e) {
            System.out.println(lexeme);
            e.printStackTrace();
        }
        return stfNodes[k][1];
    }

    private boolean isFinalNode() {
        return IntStream.of(F).anyMatch(x -> x == state);
    }
}
