package com.company;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Lex lex = new Lex();
        Scanner scanner = new Scanner(System.in);
//        lex.setSourceCode(scanner.nextLine());
        lex.setSourceCode("D:\\\\code.txt");

        if (!lex.lex()) return;
        System.out.println("----------------------------------------------");

        ProgramParser programParser = new ProgramParser(lex.tableOfSymb, lex.tableOfId);

        if (!programParser.parseProgram()) return;
        System.out.println("----------------------------------------------");

        PostfixInterpreter postfixInterpreter =
                new PostfixInterpreter(programParser.postfixCode, lex.tableOfId,
                        lex.tableOfLit, programParser.tableOfLabels);
        postfixInterpreter.postfixProcessing();

        System.out.println("----------------------------------------------");
    }
}
