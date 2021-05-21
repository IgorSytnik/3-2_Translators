package com.company;

import com.company.exceptions.InterpreterErrorException;
import com.company.helping.Ident;
import com.company.helping.Lable;
import com.company.helping.Symb;
import com.company.helping.Value;

import java.util.*;

public class PostfixInterpreter {

    private final List<Symb> postfixCode;
    List<Ident> tableOfId;
    List<String> tableOfLit;
    List<Lable> tableOfLabels;
    Stack<Value> stack = new Stack<>();
    boolean toView = false;
    int step = 1;
    int maxStep;
    int nextInstr;

    public PostfixInterpreter(List<Symb> postfixCode, List<Ident> tableOfId,
                              List<String> tableOfLit, List<Lable> tableOfLabels) {
        this.postfixCode = postfixCode;
        this.tableOfId = tableOfId;
        this.tableOfLit = tableOfLit;
        this.tableOfLabels = tableOfLabels;
    }

    public void postfixProcessing() {
        maxStep = postfixCode.size();
        try {
            Symb symb;
            for (int i = 0; i < postfixCode.size(); i++) {
                nextInstr = i;
                symb = postfixCode.get(i);
                switch (symb.token) {
                    case "int":
                    case "real":
                    case "ident":
                    case "bool":
                    case "label":

                        if (symb.token.equals("ident")) {
                            if (tableOfId.get(symb.index).type.isEmpty())
                                failRunTime(7, symb.lexeme);
                            stack.push(new Value(true, symb.index, symb.lexeme, tableOfId.get(symb.index).type));
                        } else if (symb.token.equals("label")) {
                            if (tableOfLabels.get(symb.index).valueGoTo == -1)
                                failRunTime(8, symb.lexeme);
                            stack.push(new Value(true, symb.index, symb.lexeme, symb.token));
                        } else
                            stack.push(new Value(symb.index, symb.lexeme, symb.token));
                        break;
                    case "jump":
                        nextInstr = processingJump();
                        break;
                    case "jf":
                        nextInstr = processingJF();
                        break;
                    case "colon":
                        processingColon();
                        break;
                    case "in":
                        processingInput();
                        break;
                    case "out":
                        processingOutput();
                        break;
                    default:
                        unaryOrBinary(symb.lexeme, symb.token);
                        break;
                }
                if (toView)
                    configToPrint(symb);
                step++;
                i = nextInstr;
            }
            System.out.println("\ntableOfId: ");
            for (Ident id:
                    tableOfId) {
                System.out.println(id);
            }
            System.out.println("\ntableOfLit: ");
            for (int i = 0; i < tableOfLit.size(); i++) {
                System.out.println((i+1) + ": " + tableOfLit.get(i));
            }
        } catch (InterpreterErrorException e) {
            System.out.println("----------------------------------------------");
            System.out.println(e.getMessage());
            System.out.println("Interpreter: Аварійне завершення програми з кодом " + e.getCode());
        }
    }

    private void configToPrint(Symb symb) {
        if (step == 1) {
            System.out.println("====================================");
            System.out.println("Interpreter run");
        }

        System.out.println("\nКрок інтерпритації: " + step);
        switch (symb.token) {
            case "int":
            case "real":
                System.out.println("Лексема: " + symb.lexeme + " у таблиці констант: " + symb);
                break;
            case "ident":
                System.out.println("Лексема: " + symb.lexeme + " у таблиці ідентифікаторів: " +
                        tableOfId.get(symb.index));
                break;
            default:
                System.out.println("Лексема: " + symb);
                break;
        }

        System.out.println("\nstack: ");
        for (Value v:
             stack) {
            System.out.println(v);
        }
    }

    private int processingJump() throws InterpreterErrorException {
        Value value = stack.pop();
        if (!value.type.equals("label"))
            failRunTime(520);
        return tableOfLabels.get(value.index).valueGoTo;
    }

    private int processingJF() throws InterpreterErrorException {
        Value label = stack.pop();
        Value valueBool = stack.pop();
        if (!label.type.equals("label"))
            failRunTime(520);
        if (valueBool.ident) {
            if (tableOfId.get(valueBool.index).value.equals("false")) {
                return tableOfLabels.get(label.index).valueGoTo;
            }
        } else {
            if (valueBool.value.equals("false")) {
                return tableOfLabels.get(label.index).valueGoTo;
            }
        }
        return nextInstr;
    }

    private void processingColon() throws InterpreterErrorException {
        Value value = stack.pop();
        if (!value.type.equals("label"))
            failRunTime(520);
    }

    private void processingInput() throws InterpreterErrorException {
        Value value;
        String str = "";
        String type = "";
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input:");
        try {
            value = stack.pop();
            if (!value.ident)
                failRunTime(9);
            switch (value.type) {
                case "int":
                    type = "int";
                    str = String.valueOf(Integer.parseInt(scanner.nextLine().trim()));
                    break;
                case "real":
                    type = "real";
                    str = String.valueOf(Float.parseFloat(scanner.nextLine().trim()));
                    break;
                case "bool":
                    type = "bool";
                    str = scanner.nextLine().trim();
                    if (!(str.equals("true") || str.equals("false")))
                        failRunTime(11, type);
                    break;
                default:
                    failRunTime(10, value.type);
            }
            tableOfId.get(value.index).value = str;
        } catch (NumberFormatException e) {
            failRunTime(11, type);
        }
    }

    private void processingOutput() {
        System.out.println("Output:");
        List<String> values = new LinkedList<>();
        Value value;
        if (!stack.empty()) {
            value = stack.pop();
            values.add(value.ident ? tableOfId.get(value.index).value :
                    value.value);
        }
        while (!stack.empty()) {
            value = stack.pop();
            values.add(" ");
            values.add(value.ident ? tableOfId.get(value.index).value :
                    value.value);
        }
        for (int i = values.size()-1; i >= 0; i--) {
            System.out.print(values.get(i));
        }
        System.out.print("\n");
    }

    private void unaryOrBinary(String lexeme, String token) throws InterpreterErrorException {
        if ( (token.equals("add_op") && (lexeme.equals("NEG") || lexeme.equals("POS"))) ||
                (token.equals("log_op") && lexeme.equals("!")) || token.equals("in") ||
                token.equals("out")) {
            unary(lexeme, token);
            return;
        }
        binary(lexeme, token);
    }

    private void unary(String lexeme, String token) throws InterpreterErrorException {
        Value value = stack.pop();

        switch (token) {
            case "add_op":
                if (value.type.equals("bool")) {
                    failRunTime(4, value.value, value.type, lexeme);
                } else {
                    if (lexeme.equals("POS")) {
                        stack.push(value);
                        return;
                    }
                    processingSignChange(value, lexeme);
                }
                break;
            case "log_op":
                if (!value.type.equals("bool")) {
                    failRunTime(4, value.value, value.type, lexeme);
                } else {
                    processingBoolChange(value, lexeme);
                }
                break;
        }
    }

    private void binary(String lexeme, String token) throws InterpreterErrorException {
        Value rValue = stack.pop();
        Value lValue = stack.pop();

        if (lexeme.equals(":=") && token.equals("assign_op")) {
            if (!rValue.type.equals(lValue.type)) {
                failRunTime(1, lValue.value, lValue.type, lexeme, rValue.value, rValue.type);
            } else {
                tableOfId.get(lValue.index)
                        .setValue(rValue.value);
            }
        } else if (token.equals("add_op") ||
                token.equals("mult_op") ||
                token.equals("pow_op")) {
            if (!rValue.type.equals(lValue.type)) {
                failRunTime(2, lValue.value, lValue.type, lexeme, rValue.value, rValue.type);
            } else {
                processingAddMultPow(lValue, lexeme, rValue);
            }
        } else if (token.equals("rel_op")) {
            if (rValue.type.equals("bool") || lValue.type.equals("bool")) {
                failRunTime(5, lValue.value, lValue.type, lexeme, rValue.value, rValue.type);
            } else {
                processingBoolOp(lValue, lexeme, rValue);
            }
        } else if (token.equals("log_op")) {
            if (!rValue.type.equals(lValue.type)) {
                failRunTime(2, lValue.value, lValue.type, lexeme, rValue.value, rValue.type);
            } else if (!rValue.type.equals("bool")) {
                failRunTime(5, lValue.value, lValue.type, lexeme, rValue.value, rValue.type);
            } else {
                processingLogOp(lValue, lexeme, rValue);
            }
        }
    }

    private void processingSignChange(Value value, String lexeme) throws InterpreterErrorException {

        String val = valueNum(value);
        String tok = typeNum(value);

        getValueSignChange(val, tok, lexeme);
    }

    private void processingBoolChange(Value value, String lexeme) throws InterpreterErrorException {

        String val = valueBool(value);
        String tok = typeBool();

        getValueBoolChange(val, tok, lexeme);
    }

    private void processingAddMultPow(Value lValue, String lexeme, Value rValue)
            throws InterpreterErrorException {

        String valL = valueNum(lValue);
        String tokL = typeNum(lValue);

        String valR = valueNum(rValue);
        String tokR = typeNum(rValue);

        getValueAddMultPow(valL, tokL, lexeme, valR, tokR);
    }

    private void processingBoolOp(Value lValue, String lexeme, Value rValue)
            throws InterpreterErrorException {

        String valL = valueNum(lValue);
        String tokL = typeNum(lValue);

        String valR = valueNum(rValue);
        String tokR = typeNum(rValue);

        getValueBool(valL, tokL, lexeme, valR, tokR);
    }

    private void processingLogOp(Value lValue, String lexeme, Value rValue)
            throws InterpreterErrorException {

        String valL = valueBool(lValue);
        String valR = valueBool(rValue);

        getValueLog(valL, lexeme, valR);
    }

    private String valueNum(Value value)
            throws InterpreterErrorException {
        if (value.ident) {
            if (tableOfId.get(value.index).value.isEmpty()) {
                failRunTime(3,
                        tableOfId.get(value.index).name, tableOfId.get(value.index).type);
            }

            return tableOfId.get(value.index).value;
        } else {
            return value.value;
        }
    }

    private String typeNum(Value value)
            throws InterpreterErrorException {
        if (value.ident) {
            if (tableOfId.get(value.index).value.isEmpty()) {
                failRunTime(3,
                        tableOfId.get(value.index).name, tableOfId.get(value.index).type);
            }
            return tableOfId.get(value.index).type;
        } else {
            return value.type;
        }
    }

    private String valueBool(Value value)
            throws InterpreterErrorException {
        if (value.ident) {
            if (tableOfId.get(value.index).value.isEmpty()) {
                failRunTime(3,
                        tableOfId.get(value.index).name, tableOfId.get(value.index).type);
            }
            return tableOfId.get(value.index).value;
        } else {
            return value.value;
        }
    }

    private String typeBool() {
        return "bool";
    }

    private void getValueSignChange(String val, String type,
                                    String lexeme)
            throws InterpreterErrorException {

        String value = "";
        if (lexeme.equals("NEG")) {
            if (type.equals("int")){
                value = String.valueOf(-Integer.parseInt(val));
            } else if (type.equals("real")){
                value = String.valueOf(-Float.parseFloat(val));
            } else {
                failRunTime(500, "getValueSignChange" + val + " " + type + " " +  lexeme);
            }
        } else {
            failRunTime(510);
        }

        if (!tableOfLit.contains(value)) {
            tableOfLit.add(value);
        }
        stack.push(new Value(tableOfLit.indexOf(value), value, type));
    }

    private void getValueBoolChange(String val, String type,
                                    String lexeme)
            throws InterpreterErrorException {

        if (lexeme.equals("!")) {
            String value = String.valueOf(!Boolean.parseBoolean(val));
            stack.push(new Value(-1, value, type));
        } else {
            failRunTime(510);
        }
    }

    private void getValueAddMultPow(String valL, String tokL,
                          String lexeme,
                          String valR, String tokR)
            throws InterpreterErrorException {

        String value;

        if (tokL.equals("int") && tokR.equals("int")){
            value = String.valueOf((int) calculate(Integer.parseInt(valL), lexeme, Integer.parseInt(valR)));
        } else {
            if (lexeme.equals("%"))
                failRunTime(5, valL, tokL, lexeme, valR, tokR);
            value = String.valueOf((float) calculate(Float.parseFloat(valL), lexeme, Float.parseFloat(valR)));
        }

        if (!tableOfLit.contains(value)) {
            tableOfLit.add(value);
        }
        stack.push(new Value(tableOfLit.indexOf(value), value, tokL));
    }

    private void getValueBool(String valL, String tokL,
                                    String lexeme,
                                    String valR, String tokR)
            throws InterpreterErrorException {

        String value;
        if (tokL.equals("int") && tokR.equals("int")){
            value = String.valueOf(compare(Integer.parseInt(valL), lexeme, Integer.parseInt(valR)));
        } else if (tokL.equals("real") && tokR.equals("real")){
            value = String.valueOf(compare(Float.parseFloat(valL), lexeme, Float.parseFloat(valR)));
        } else if (tokL.equals("real") && tokR.equals("int")){
            value = String.valueOf(compare(Float.parseFloat(valL), lexeme, Integer.parseInt(valR)));
        } else {
            value = String.valueOf(compare(Integer.parseInt(valL), lexeme, Float.parseFloat(valR)));
        }

        stack.push(new Value(-2, value, "bool"));
    }

    private void getValueLog(String valL,
                             String lexeme,
                             String valR)
            throws InterpreterErrorException {

        String value = "";
        boolean v1 = Boolean.parseBoolean(valL);
        boolean v2 = Boolean.parseBoolean(valR);

        switch (lexeme) {
            case "||":
                value = String.valueOf(v1 || v2);
                break;
            case "&&":
                value = String.valueOf(v1 && v2);
                break;
            default:
                failRunTime(510);
        }

        stack.push(new Value(-3, value, "bool"));
    }

    private double calculate(double v1, String lexeme, double v2) throws InterpreterErrorException {
        switch (lexeme) {
            case "+":
                return v1 + v2;
            case "-":
                return v1 - v2;
            case "*":
                return v1 * v2;
            case "/":
                if (v2 == 0) failRunTime(6);
                return v1 / v2;
            case "%":
                return v1 % v2;
            case "^":
                return Math.pow(v1, v2);
            default:
                failRunTime(510);
        }
        failRunTime(510);
        return 0.0;
    }

    private boolean compare(double v1, String lexeme, double v2) throws InterpreterErrorException {
        switch (lexeme) {
            case ">":
                return v1 > v2;
            case "<":
                return v1 < v2;
            case "<=":
                return v1 <= v2;
            case ">=":
                return v1 >= v2;
            case "==":
                return v1 == v2;
            case "!=":
                return v1 != v2;
            default:
                failRunTime(510);
        }
        failRunTime(510);
        return false;
    }

    private void failRunTime(int errorCode, String... tuple) throws InterpreterErrorException {
        switch (errorCode) {
            case 1:
                throw new InterpreterErrorException(
                        "RunTime ERROR: невідповідність типів під час асигнування значення \n" +
                                tuple[0] + " тип: '" + tuple[1] + "'" +
                                " " + tuple[2] + " " +
                                tuple[3] + " тип: '" + tuple[4] + "'"
                        ,
                        errorCode);
            case 2:
                throw new InterpreterErrorException(
                        "RunTime ERROR: невідповідність типів\n" +
                                tuple[0] + " тип: '" + tuple[1] + "'" +
                                " " + tuple[2] + " " +
                                tuple[3] + " тип: '" + tuple[4] + "'"
                        ,
                        errorCode);
            case 3:
                throw new InterpreterErrorException(
                        "RunTime ERROR: неініціалізована змінна\n" +
                                tuple[0] + " тип: '" + tuple[1] + "'"
                        ,
                        errorCode);
            case 4:
                throw new InterpreterErrorException(
                        "RunTime ERROR: данна операція не підтримує цей тип операнда\n" +
                                tuple[0] + " тип: '" + tuple[1] + "'," +
                                " оператор: " + tuple[2]
                        ,
                        errorCode);
            case 5:
                throw new InterpreterErrorException(
                        "RunTime ERROR: данна операція не підтримує цей тип операндiв\n" +
                                tuple[0] + " тип: '" + tuple[1] + "'" +
                                " " + tuple[2] + " " +
                                tuple[3] + " тип: '" + tuple[4] + "'"
                        ,
                        errorCode);
            case 6:
                throw new InterpreterErrorException(
                        "RunTime ERROR: ділення на нуль"
                        ,
                        errorCode);
            case 7:
                throw new InterpreterErrorException(
                        "RunTime ERROR: неоголошена змінна: " + tuple[0]
                        ,
                        errorCode);
            case 8:
                throw new InterpreterErrorException(
                        "RunTime ERROR: неоголошена мітка: " + tuple[0]
                        ,
                        errorCode);
            case 9:
                throw new InterpreterErrorException(
                        "RunTime ERROR: read not ident"
                        ,
                        errorCode);
            case 10:
                throw new InterpreterErrorException(
                        "RunTime ERROR: тип input невірний: " + tuple[0]
                        ,
                        errorCode);
            case 11:
                throw new InterpreterErrorException(
                        "RunTime ERROR: тип input невірний, повинен бути: " + tuple[0]
                        ,
                        errorCode);
            case 500:
                throw new InterpreterErrorException(
                        "RunTime ERROR: " + tuple[0]
                        ,
                        errorCode);
            default:
                throw new InterpreterErrorException(
                        ""
                        ,
                        errorCode);
        }
    }
}
