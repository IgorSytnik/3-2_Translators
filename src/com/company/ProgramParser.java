package com.company;

import com.company.exceptions.ParserErrorException;
import com.company.helping.Ident;
import com.company.helping.Lable;
import com.company.helping.Symb;

import java.util.ArrayList;
import java.util.List;

public class ProgramParser {

    private final Symb[] tableOfSymb;
    public final List<Symb> postfixCode = new ArrayList<>();
    public final List<Symb> postfixCodeBuffered = new ArrayList<>();
    List<Ident> tableOfId;
    List<Lable> tableOfLabels = new ArrayList<>();
    private final int lengthTableOfSymb;
    private int numRow = 0;
    private int depth = 0;  // for numRow
    private int depthPostfixCode = 0;  // for postfixCode
    private int depthPostfixCodeBuffered = 0;  // for postfixCodeBuffered
    private Symb symb;
    private final StringBuilder stringBuilder = new StringBuilder();
    private String declarType;
    private boolean isBuff = false;

    public ProgramParser(List<Symb> tableOfSymb, List<Ident> tableOfId) {
        this.tableOfSymb = new Symb[tableOfSymb.size()];
        tableOfSymb.toArray(this.tableOfSymb);
        this.tableOfId = tableOfId;
        lengthTableOfSymb = tableOfSymb.size();
    }

    /**
     * Main method for parsing tableOfSymb
     * Program = instruction ProgName DoSection
     * DoSection = ’{’ StatementList ’}’
     */
    public boolean parseProgram() {
        try {
            parseBeginning();
            parseStatementList();
            parseEnd();

            System.out.println("Parser: Синтаксичний аналіз завершився успішно");

            System.out.println("postfixCode:");
            for (int i = 0; i < postfixCode.size(); i++) {
                System.out.println(i + ":" + postfixCode.get(i));
            }

            System.out.println("tableOfId:");
            for (Ident id:
                    tableOfId) {
                System.out.println(id);
            }

            System.out.println("tableOfLabels:");
            for (Lable lable:
                    tableOfLabels) {
                System.out.println(lable);
            }

        } catch (ParserErrorException e) {
            System.out.println("----------------------------------------------");
            System.out.println(e.getMessage());
            System.out.println("Parser: Аварійне завершення програми з кодом " + e.getCode());
            return false;
        }
        return true;
    }

    /**
     * Parses program beginning
     * @throws ParserErrorException
     * If tableOfSymb has an unexpected element
     */
    private void parseBeginning() throws ParserErrorException {

        tabOut("parseBeginning:");
        tabAddOne();

        parseToken("instruction", "keyword");
        parseByToken("ident");
        tableOfId.get(symb.index).setType("program_id");
        parseToken("{", "braces_left");

        tabDelLast();
    }

    /**
     * Parses program end
     * @throws ParserErrorException
     * If tableOfSymb has an unexpected element
     */
    private void parseEnd() throws ParserErrorException {
        tabOut("parseEnd():");
        tabAddOne();
        parseToken("}", "braces_right");
        tabDelLast();
    }

    /**
     * Parses by lexeme and token
     * @param lexeme
     * lexeme attribute that symb.lexeme should be equal to
     * @param token
     * token attribute that symb.token should be equal to
     * @throws ParserErrorException
     * If symb does not match the condition (symb.lexeme.equals(lexeme) && symb.token.equals(token))
     */
    private void parseToken(String lexeme, String token) throws ParserErrorException {
        if (numRow >= lengthTableOfSymb) {
            System.out.println("parseToken(): неочікуваний кінець програми");
            failParse(1001);
        }

        symb = getSymb();
        increment();

        if (symb.lexeme.equals(lexeme) && symb.token.equals(token)) {
            tabOut("parseToken(): В рядку " + symb.lineNumber + " токен ('" +
                    symb.lexeme + "', " + symb.token + ")");
        } else {
            System.out.println("parseToken(): Невідповідність токенів " + symb + ", ('" + lexeme + "', " + token + ")");
            failParse(1, token, lexeme);
        }
    }

    /**
     * Parses only by token
     * @param token
     * token attribute that symb.token should be equal to
     * @throws ParserErrorException
     * If symb does not match the condition symb.lexeme.equals(lexeme)
     */
    private void parseByToken(String token) throws ParserErrorException {
        if (numRow >= lengthTableOfSymb) {
            System.out.println("parseByToken(): неочікуваний кінець програми");
            failParse(1001);
        }

        symb = getSymb();
        increment();

        if (symb.token.equals(token)) {
            tabOut("parseByToken(): В рядку " + symb.lineNumber + " токен ('" +
                    symb.lexeme + "', " + symb.token + ")");
        } else {
            System.out.println("parseByToken(): Невідповідність токенів " + symb + ", '" + token + "'");
            failParse(2, token);
        }
    }

    /**
     * Parses through tableOfSymb
     * Does not pass through the beginning and the end of the program
     * StatementList = { Statement }
     * @throws ParserErrorException
     * From method parseStatement
     */
    private void parseStatementList() throws ParserErrorException {
        tabOut("parseStatementList():");
        tabAddOne();

        while (parseStatement());

        tabDelLast();
    }

    /**
     * Parses one statement
     * Statement = (Assign | Inp | Out | ForStatement | IfStatement | Declaration) ‘;’
     * @return
     * Returns true if statement was found
     * @throws ParserErrorException
     * If found something unexpected in a statement
     */
    private boolean parseStatement() throws ParserErrorException {
        tabOut("parseStatement():");
        boolean st = false;
        symb = getSymb();
        tabAddOne();
        depth = 0;
        depthPostfixCode = 0;

        if (symb.token.equals("ident")) {
            parseAssign();
            parseToken(";", "semicolon");
            st = true;
        } else if (symb.token.equals("keyword")) {
            switch (symb.lexeme) {
                case "if":
                    parseIf();
                    parseToken(";", "semicolon");
                    st = true;
                    break;
                case "for":
                    parseFor();
                    parseToken(";", "semicolon");
                    st = true;
                    break;
                case "write":
                    parseWrite();
                    parseToken(";", "semicolon");
                    st = true;
                    break;
                case "read":
                    parseRead();
                    parseToken(";", "semicolon");
                    st = true;
                    break;
                case "int":
                    declarType = "int";
                    parseInt();
                    parseToken(";", "semicolon");
                    st = true;
                    break;
                case "real":
                    declarType = "real";
                    parseReal();
                    parseToken(";", "semicolon");
                    st = true;
                    break;
                case "bool":
                    declarType = "bool";
                    parseBool();
                    parseToken(";", "semicolon");
                    st = true;
                    break;
            }
        }

        tabDelLast();
        return st;
    }

    /**
     * For both boolean and arithmetic expression
     * Assign = Ident ’:=’ Expression
     * Expression = LodExpr | ArithmExpression
     * @throws ParserErrorException
     * If found something unexpected
     */
    private void parseAssign() throws ParserErrorException {
        tabOut("parseAssign():");
        tabAddOne();
        Symb postFSymb;

        symb = getSymb();
        parseByToken("ident");
        postFSymb = new Symb(symb);
        postfixCodeAddOne(postFSymb);

        parseToken(":=", "assign_op");
        postFSymb = new Symb(symb);
        depth = 0;
        depthPostfixCode = 0;
        parseArithmeticExpression();
        if (symb.token.equals("semicolon")) {
            postfixCodeAddOne(postFSymb);
            tabDelLast();
            return;
        }
        wrongStatement();
        parseLogExpression();
        postfixCodeAddOne(postFSymb);
        tabDelLast();
    }

    /**
     * For boolean expression
     * LogExpr = (BoolExpr {LogOp BoolExpr})
     * @throws ParserErrorException
     * If found something unexpected
     */
    private boolean parseLogExpression() throws ParserErrorException {
        tabOut("parseExpression():");
        tabAddOne();
        boolean isLogEx;
        Symb postFSymb;

        isLogEx = parseBoolExpression();

        symb = getSymb();

        while (symb.token.equals("log_op") && isLogEx) {
            postFSymb = new Symb(symb);
            printSymb();
            increment();
            isLogEx = parseBoolExpression();
            postfixCodeAddOne(postFSymb);
        }

        tabDelLast();
        return isLogEx;
    }

    /**
     * For boolean expression without '||', '&&' operators
     * BoolExpr = (ArithmExpression RelOp ArithmExpression | BoolLit)
     * @throws ParserErrorException
     * If found something unexpected
     */
    private boolean parseBoolExpression() throws ParserErrorException {
        tabOut("parseBoolExpression():");
        tabAddOne();
        Symb postFSymb, postFSymb_2;
        boolean isBoolEx = false;
        depth = 0;
        depthPostfixCode = 0;
        symb = getSymb();

        if (symb.lexeme.equals("!") && symb.token.equals("log_op")) {
            postFSymb = new Symb(symb);
            parseToken("!", "log_op");
            symb = getSymb();
            if (symb.lexeme.equals("(") && symb.token.equals("brackets_left")) {
                parseToken("(", "brackets_left");
                parseLogExpression();
                parseToken(")", "brackets_right");
            } else if (symb.token.equals("ident")) {
                postFSymb_2 = symb;
                parseByToken("ident");
                postfixCodeAddOne(postFSymb_2);
            } else {
                System.out.println("parseBoolExpression(): неочікуваний елемент");
                failParse(3, symb.token, symb.lexeme, "BoolExpression");
            }
            postfixCodeAddOne(postFSymb);
            tabDelLast();
            return true;
        }
        if (symb.token.equals("bool")) {
            postFSymb = new Symb(symb);
            parseByToken("bool");
            postfixCodeAddOne(postFSymb);
            tabDelLast();
            return true;
        } else if (symb.token.equals("ident")) {
            postFSymb = new Symb(symb);
            parseByToken("ident");
            postfixCodeAddOne(postFSymb);
            symb = getSymb();
            if (symb.token.equals("log_op") ||      //  exceptions to continue
                    symb.token.equals("coma") ||
                    symb.token.equals("semicolon") ||
                    symb.token.equals("keyword")) {
                tabDelLast();
                return true;
            }
            wrongStatement();
        }

        if (parseArithmeticExpression() && symb.token.equals("rel_op")) {
            postFSymb = new Symb(symb);
            printSymb();
            increment();
            isBoolEx = parseArithmeticExpression();
            symb = getSymb();
            postfixCodeAddOne(postFSymb);
        } else {
            wrongStatement();
            symb = getSymb();
            if (symb.lexeme.equals("(") && symb.token.equals("brackets_left")) {
                parseToken("(", "brackets_left");
                isBoolEx = parseLogExpression();
                parseToken(")", "brackets_right");
            } else {
                System.out.println("parseBoolExpression(): неочікуваний елемент");
                failParse(3, symb.token, symb.lexeme, "BoolExpression");
            }
        }

        tabDelLast();
        return isBoolEx;
    }

    /**
     * Parses arithmetic expression
     * @return
     * true if there was an arithmetic expression and false if not
     * @throws ParserErrorException
     * if found something unexpected
     */
    private boolean parseArithmeticExpression() throws ParserErrorException {
        tabOut("parseArithmeticExpression():");
        boolean isArEx = true;
        boolean isFirstOp = true;
        tabAddOne();
        Symb postFSymb;

        symb = getSymb();

        if (!symb.token.equals("add_op")) {
            isFirstOp = false;
            isArEx = parseTerm();
            symb = getSymb();
        }

        while (symb.token.equals("add_op") && isArEx) {
            postFSymb = new Symb(symb);
            printSymb();
            increment();
            isArEx = parseTerm();
            if (isFirstOp) {
                postFSymb.lexeme = postFSymb.lexeme.equals("-") ? "NEG" : "POS";
                isFirstOp = false;
            }
            postfixCodeAddOne(postFSymb);
        }

        if (!isArEx){
            wrongStatement();
        }

        tabDelLast();
        return isArEx;
    }

    private boolean parsePow() throws ParserErrorException {
        tabOut("parsePow():");
        boolean isPow;
        tabAddOne();
        Symb postFSymb;

         do {
            printSymb();
            increment();
            isPow = parseFactor();
            symb = getSymb();
            if (symb.token.equals("pow_op") && isPow) {
                postFSymb = new Symb(symb);
                parsePow();
                postfixCodeAddOne(postFSymb);
            }
        } while (symb.token.equals("pow_op") && isPow);

        if (!isPow){
            wrongStatement();
        }
        tabDelLast();
        return isPow;
    }

    /**
     * Parses term
     * Term = Factor | Term MultOp Factor | Term PowOp Factor
     * @return
     * true if found term, otherwise false
     * @throws ParserErrorException
     * if found something unexpected
     */
    private boolean parseTerm() throws ParserErrorException {
        tabOut("parseTerm():");
        boolean isTerm;
        tabAddOne();
        Symb postFSymb;

        isTerm = parseFactor();

        symb = getSymb();

        while ((symb.token.equals("mult_op") || symb.token.equals("pow_op")) && isTerm) {
            if (symb.token.equals("pow_op")) {
                postFSymb = new Symb(symb);
                printSymb();
                isTerm = parsePow();
                postfixCodeAddOne(postFSymb);
                symb = getSymb();
                continue;
            }
            postFSymb = new Symb(symb);
            printSymb();
            increment();
            isTerm = parseFactor();
            symb = getSymb();
            postfixCodeAddOne(postFSymb);
        }

        if (!isTerm){
            wrongStatement();
        }
        tabDelLast();
        return isTerm;
    }

    /**
     * Parses factor
     * Factor = Ident | Literal | ’(’ ArithmExpression ’)’
     * @return
     * true if found factor, otherwise false
     * @throws ParserErrorException
     * if found something unexpected
     */
    private boolean parseFactor() throws ParserErrorException {
        tabOut("parseFactor():");
        boolean isFactor;
        tabAddOne();
        Symb postFSymb;

        symb = getSymb();
        increment();

        switch (symb.token) {
            case "ident":
            case "int":
            case "real":
                postFSymb = new Symb(symb);
                printSymb();
                isFactor = true;
                postfixCodeAddOne(postFSymb);
                break;
            case "brackets_left":
                printSymb();
                if (parseArithmeticExpression()) {
                    parseToken(")", "brackets_right");
                    isFactor = true;
                    break;
                }
                isFactor = false;
                break;
            default:
                decrement();
                isFactor = false;
        }

        tabDelLast();
        return isFactor;
    }

    /**
     * Parses integer variable declaration
     * Declaration = Type IdenttList
     * IdenttList = IdentA {’,’ IdentA}
     * IdentA = Ident | Ident ‘:=’ ArithmExpression
     * @throws ParserErrorException
     * if found something unexpected
     */
    private void parseInt() throws ParserErrorException {
        tabOut("parseInt():");
        tabAddOne();
        Symb postFSymbIdent;
        Symb postFSymbAssign;

        symb = getSymb();
        printSymb();

        do {
            increment();
            parseByToken("ident");
            if (!tableOfId.get(symb.index).type.isEmpty())
                failParse(4, symb.lexeme, tableOfId.get(symb.index).type);
            tableOfId.get(symb.index).setType(declarType);
            postFSymbIdent = new Symb(symb);
            symb = getSymb();
            if (symb.token.equals("assign_op")) {
                postfixCodeAddOne(postFSymbIdent);
                postFSymbAssign = new Symb(symb);
                parseToken(":=", "assign_op");
                depth = 0;
                depthPostfixCode = 0;
                if (!parseArithmeticExpression()) {
                    failParse(5, symb.token, symb.lexeme);
                }
                postfixCodeAddOne(postFSymbAssign);
            }
            symb = getSymb();
        } while (symb.token.equals("coma"));

        tabDelLast();

    }

    /**
     * Parses real variable declaration
     * Declaration = Type IdenttList
     * IdenttList = IdentA {’,’ IdentA}
     * IdentA = Ident | Ident ‘:=’ ArithmExpression
     * @throws ParserErrorException
     * if found something unexpected
     */
    private void parseReal() throws ParserErrorException {
        tabOut("parseReal():");
        tabAddOne();
        Symb postFSymbIdent;
        Symb postFSymbAssign;

        symb = getSymb();
        printSymb();

        do {
            increment();
            parseByToken("ident");
            if (!tableOfId.get(symb.index).type.isEmpty())
                failParse(4, symb.lexeme, tableOfId.get(symb.index).type);
            tableOfId.get(symb.index).setType(declarType);
            postFSymbIdent = new Symb(symb);
            symb = getSymb();
            if (symb.token.equals("assign_op")) {
                postfixCodeAddOne(postFSymbIdent);
                postFSymbAssign = new Symb(symb);
                parseToken(":=", "assign_op");
                depth = 0;
                depthPostfixCode = 0;
                if (!parseArithmeticExpression()) {
                    failParse(5, symb.token, symb.lexeme);
                }
                postfixCodeAddOne(postFSymbAssign);
            }
            symb = getSymb();
        } while (symb.token.equals("coma"));

        tabDelLast();
    }

    /**
     * Parses boolean variable declaration
     * Declaration = Type IdenttList
     * IdenttList = IdentA {’,’ IdentA}
     * IdentA = Ident | Ident ‘:=’ LogExpr
     * @throws ParserErrorException
     * if found something unexpected
     */
    private void parseBool() throws ParserErrorException {
        tabOut("parseBool():");
        tabAddOne();
        Symb postFSymbIdent;
        Symb postFSymbAssign;

        symb = getSymb();
        printSymb();

        do {
            increment();
            parseByToken("ident");
            if (!tableOfId.get(symb.index).type.isEmpty())
                failParse(4, symb.lexeme, tableOfId.get(symb.index).type);
            tableOfId.get(symb.index).setType(declarType);
            postFSymbIdent = new Symb(symb);
            symb = getSymb();
            if (symb.token.equals("assign_op")) {
                postfixCodeAddOne(postFSymbIdent);
                postFSymbAssign = new Symb(symb);
                parseToken(":=", "assign_op");
                depth = 0;
                depthPostfixCode = 0;
                if (!parseLogExpression()) {
                    failParse(5, symb.token, symb.lexeme);
                }
//                parseLogExpression();
                postfixCodeAddOne(postFSymbAssign);
            }
            symb = getSymb();
        } while (symb.token.equals("coma"));

        tabDelLast();
    }

    /**
     * Parses read statement
     * Inp = read ’(’ IdenttList ’)’
     * @throws ParserErrorException
     * if found something unexpected
     */
    private void parseRead() throws ParserErrorException {
        tabOut("parseRead():");
        tabAddOne();

        parseToken("read", "keyword");
        parseToken("(", "brackets_left");
        parseIdentListIn();
        parseToken(")", "brackets_right");

        tabDelLast();
    }

    /**
     * Parses identifier list for read statement
     * IdentList = Ident {',' Ident}
     * @throws ParserErrorException
     * if found something unexpected
     */
    private void parseIdentListIn() throws ParserErrorException {
        tabOut("parseIdentList():");
        tabAddOne();
        Symb postFSymb;
        symb = getSymb();

        parseByToken("ident");
        postFSymb = new Symb(symb);
        postfixCodeAddOne(postFSymb);
        postfixCodeAddOne(new Symb(symb.lineNumber, "IN", "in"));
        symb = getSymb();

        while (symb.token.equals("coma")){
            printSymb();
            increment();
            parseByToken("ident");
            postFSymb = new Symb(symb);
            postfixCodeAddOne(postFSymb);
            postfixCodeAddOne(new Symb(symb.lineNumber, "IN", "in"));
            symb = getSymb();
        }

        tabDelLast();
    }

    /**
     * Parses write statement
     * Out = write ’(’ List ’)’
     * @throws ParserErrorException
     * if found something unexpected
     */
    private void parseWrite() throws ParserErrorException {
        tabOut("parseWrite():");
        tabAddOne();

        parseToken("write", "keyword");
        parseToken("(", "brackets_left");
        parseListOut();
        parseToken(")", "brackets_right");
        postfixCodeAddOne(new Symb(symb.lineNumber, "OUT", "out"));

        tabDelLast();
    }

    /**
     * Parses list for read statement
     * List = ListElement {',' ListElement}
     * ListElement = ArithmExpression | LogExpr
     * @throws ParserErrorException
     * if found something unexpected
     */
    private void parseListOut() throws ParserErrorException {
        tabOut("parseList():");
        tabAddOne();
        symb = getSymb();

        depth = 0;
        depthPostfixCode = 0;
        parseArithmeticExpression();
        if (!(symb.token.equals("coma") || symb.token.equals("brackets_right"))) {
            wrongStatement();
            parseLogExpression();
        }

        symb = getSymb();

        while (symb.token.equals("coma")){
            printSymb();
            increment();
            symb = getSymb();
            depth = 0;
            depthPostfixCode = 0;
            parseArithmeticExpression();
            if (!(symb.token.equals("coma") || symb.token.equals("brackets_right"))) {
                wrongStatement();
                parseLogExpression();
            }
            symb = getSymb();
        }

        tabDelLast();
    }

    /**
     * Parses if statement
     * IfStatement = if LogExpr then DoBlockIf1 {else DoBlockIf2} endif
     * DoBlockIf1 = DoBlockIf
     * DoBlockIf2 = DoBlockIf
     * DoBlockIf = StatementList
     * @throws ParserErrorException
     * if found something unexpected
     */
    private void parseIf() throws ParserErrorException {
        tabOut("parseIf():");
        tabAddOne();

        parseToken("if", "keyword");
        depth = 0;
        depthPostfixCode = 0;
        if (!parseLogExpression()) {
            failParse(5, symb.token, symb.lexeme);
        }
//        parseLogExpression();
        parseToken("then", "keyword");
        //m1
        //JF
        Symb m1 = createLabel();
        postfixCodeAddOne(m1);
        postfixCodeAddOne(new Symb(symb.lineNumber, "JF", "jf"));
        parseStatementList();
        symb = getSymb();
        if (symb.lexeme.equals("else") && symb.token.equals("keyword")) {
            //m2
            //JMP
            Symb m2 = createLabel();
            postfixCodeAddOne(m2);
            postfixCodeAddOne(new Symb(symb.lineNumber, "JMP", "jump"));

            parseToken("else", "keyword");
            //m1
            //:
            setValueLabel(m1);
            parseStatementList();

            parseToken("endif", "keyword");
            //m2
            //:
            setValueLabel(m2);
        } else {
            parseToken("endif", "keyword");
            //m1
            //:
            setValueLabel(m1);
        }

        tabDelLast();
    }

    /**
     * Parses for statement
     * ForStatement = for ‘(‘ IdentFor ‘:=’ ArithmExpression1 ’;’ LogExpr ’;’ ArithmExpression2 ’)’ DoBlockFor
     * DoBlockFor = StatementList endfor
     * IdentFor = (int | real) Ident
     * ArithmExpression1 = ArithmExpression
     * ArithmExpression2 = ArithmExpression
     * @throws ParserErrorException
     * if found something unexpected
     */
    private void parseFor() throws ParserErrorException {
        tabOut("parseFor():");
        tabAddOne();
        Symb postFSymb;
        Symb postFSymbIdent;

        parseToken("for", "keyword");
        parseToken("(", "brackets_left");
        symb = getSymb();
        if (symb.lexeme.equals("int") && symb.token.equals("keyword")) {
            declarType = "int";
            parseToken("int", "keyword");
        } else if (symb.lexeme.equals("real") && symb.token.equals("keyword")) {
            declarType = "real";
            parseToken("real", "keyword");
        } else {
            System.out.println("parseFor(): неочікуваний елемент");
            failParse(3, symb.token, symb.lexeme, "int або real keyword");
        }
        parseByToken("ident");
        if (!tableOfId.get(symb.index).type.isEmpty())
            failParse(4, symb.lexeme, tableOfId.get(symb.index).type);
        postFSymbIdent = new Symb(symb);
        postfixCodeAddOne(postFSymbIdent);
        tableOfId.get(symb.index).setType(declarType);
        parseToken(":=", "assign_op");
        postFSymb = new Symb(symb);
//        parseArithmeticExpression();
        depth = 0;
        depthPostfixCode = 0;
        if (!parseArithmeticExpression()) {
            failParse(5, symb.token, symb.lexeme);
        }
        postfixCodeAddOne(postFSymb);
        //m0
        //JMP
        Symb m0 = createLabel();
        postfixCodeAddOne(m0);
        postfixCodeAddOne(new Symb(symb.lineNumber, "JMP", "jump"));
        parseToken(";", "semicolon");
        //m1
        //:
        Symb m1 = createLabel();
        setValueLabel(m1);

        //is buffered
        isBuff = true;
        parseLogExpression();
        //m2
        //JF
        Symb m2 = createLabel();
        postfixCodeAddOne(m2);
        postfixCodeAddOne(new Symb(symb.lineNumber, "JF", "jf"));
        isBuff = false;
        //is buffered^

        parseToken(";", "semicolon");
        postfixCodeAddOne(postFSymbIdent);
        parseArithmeticExpression();
        postfixCodeAddOne(new Symb(symb.lineNumber, ":=", "assign_op"));
        parseToken(")", "brackets_right");
        //m0
        //:
        setValueLabel(m0);
        //buffered here
        putBuff();

        parseStatementList();

        //m1
        //JMP
        postfixCodeAddOne(m1);
        postfixCodeAddOne(new Symb(symb.lineNumber, "JMP", "jump"));

        parseToken("endfor", "keyword");
        //m2
        //:
        setValueLabel(m2);

        tabDelLast();
    }

    /**
     *
     * @return
     * Symb from {@code tableOfSymb} with index {@code numRow}
     * @throws ParserErrorException
     * if {@code numRow} exceeds {@code lengthTableOfSymb}
     */
    private Symb getSymb() throws ParserErrorException {
        if (numRow >= lengthTableOfSymb) {
            System.out.println("getSymb(): неочікуваний кінець програми");
            failParse(1002);
        }
        return tableOfSymb[numRow];
    }

    /**
     * Prints Symb in convenient form
     * "в рядку " + symb.lineNumber + " - ('" + symb.lexeme + "', " + symb.token + ")"
     */
    private void printSymb() {
        tabOut("в рядку " + symb.lineNumber + " - ('" + symb.lexeme + "', " + symb.token + ")");
    }

    /**
     * Prints message {@code str} with {@code stringBuilder} (tab chars)
     * @param str
     * message to print
     */
    private void tabOut(String str) {
        System.out.println(stringBuilder + str);
    }

    /**
     * Deletes last element from {@code stringBuilder}
     */
    private void tabDelLast() {
        stringBuilder.delete(stringBuilder.length() - 1,
                stringBuilder.length());
    }

    /**
     * Appends "\t" to {@code stringBuilder}
     */
    private void tabAddOne() {
        stringBuilder.append("\t");
    }

    /**
     * Increments {@code numRow}
     * Increments {@code depth} in case for wrong statements
     */
    private void increment() {
        numRow++;
        depth++;
    }

    /**
     * Decrements {@code numRow}
     * Decrements {@code depth} in case for wrong statements
     */
    private void decrement() {
        numRow--;
        depth--;
    }

    /**
     * For wrong statements
     */
    private void wrongStatement() {
        numRow -= depth;
        depth = 0;
        for (int i = 0; i < depthPostfixCode; i++) {
            postfixCode.remove(postfixCode.size() - 1);
        }
        for (int i = 0; i < depthPostfixCodeBuffered; i++) {
            postfixCodeBuffered.remove(postfixCodeBuffered.size() - 1);
        }
        depthPostfixCode = 0;
    }

    private void postfixCodeAddOne(Symb postFSymb) {
        if (isBuff) {
            bufferedPostfixCodeAddOne(postFSymb);
        } else {
            usualCodeAddOne(postFSymb);
        }
    }

    private void usualCodeAddOne(Symb postFSymb) {
        postfixCode.add(postFSymb);
        depthPostfixCode++;
    }

    private void bufferedPostfixCodeAddOne(Symb postFSymb) {
        postfixCodeBuffered.add(postFSymb);
        depthPostfixCodeBuffered++;
    }

    private void putBuff() {
        postfixCode.addAll(postfixCodeBuffered);
        postfixCodeBuffered.clear();
        depthPostfixCodeBuffered = 0;
    }
    
    private Symb createLabel() {
        int index = tableOfLabels.size();
        Lable lable = new Lable(index, "m" + index, -1);
        tableOfLabels.add(lable);
        return new Symb(symb.lineNumber, lable.name, "label", index);
    }

    private void setValueLabel(Symb m) {
        tableOfLabels.get(m.index).valueGoTo = postfixCode.size() - 1;
        postfixCodeAddOne(m);
        postfixCodeAddOne(new Symb(symb.lineNumber, ":", "colon"));
    }

    /**
     * Gets called when something fails (source code mistakes)
     * @param errorCode
     * error code
     * @param tuple
     * tuple[0] = token,
     * tuple[1] = lexeme,
     * tuple[2] = expected,
     * @throws ParserErrorException
     * by errorCode
     */
    private void failParse(int errorCode, String... tuple) throws ParserErrorException {
        switch (errorCode) {
            case 1001:
                throw new ParserErrorException("Parser ERROR: \n\t Неочікуваний кінець програми - " +
                        "в таблиці символів (розбору) немає запису з номером " + numRow +
                        ". \n\t Очікувалось - ('" + symb.token + "', " + symb.lexeme + ").",
                        errorCode);
            case 1002:
                throw new ParserErrorException("Parser ERROR: \n\t Неочікуваний кінець програми - " +
                        "в таблиці символів (розбору) немає запису з номером " + numRow +
                        ". \n\t Останній запис - " + tableOfSymb[tableOfSymb.length - 1],
                        errorCode);
            case 1:
                throw new ParserErrorException("Parser ERROR: \n\t В рядку " + symb.lineNumber +
                        " неочікуваний елемент ('" + symb.lexeme + "', " + symb.token +
                        "). \n\t Очікувався - ('" + tuple[1] + "', " + tuple[0] + ").",
                        errorCode);
            case 2:
                throw new ParserErrorException("Parser ERROR: \n\t В рядку " + symb.lineNumber +
                        " неочікуваний токен ('" + symb.lexeme + "', " + symb.token +
                        "). \n\t Очікувався токен - " + tuple[0] + ".",
                        errorCode);
            case 3:
                throw new ParserErrorException("Parser ERROR: \n\t В рядку " + symb.lineNumber +
                        " неочікуваний елемент ('" + tuple[1] + "', " + tuple[0] +
                        "). \n\t Очікувався - '" + tuple[2] + "'.",
                        errorCode);
            case 4:
                throw new ParserErrorException("Parser ERROR: \n\t В рядку " + symb.lineNumber +
                        " вже оголошена змінна ('" + tuple[1] + "', тип: " + tuple[0] +
                        ").",
                        errorCode);
            case 5:
                throw new ParserErrorException("Parser ERROR: \n\t В рядку " + symb.lineNumber +
                        " неочікуване значення операнда ('" + tuple[1] + "', тип: " + tuple[0] +
                        ").",
                        errorCode);
            default:
                throw new ParserErrorException("Parser ERROR: failParse()", errorCode);

        }
    }
}
