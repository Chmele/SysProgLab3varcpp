package com.company;

import java.util.ArrayList;
import java.util.List;

public class Automaton {
    private final State start;
    private State current;
    private State incorrect = new State("incorrect", true, LexemType.NOT_RECOGNIZED);
    private final List<String> keyWords = new ArrayList<>(List.of("auto", "class", "continue", "delete",
            "do", "double", "enum", "for", "if", "int", "long", "new", "or", "return"));

    List<String> digits10 = new ArrayList<>(List.of("0123456789".split("")));
    List<String> digits8 = new ArrayList<>(List.of("01234567".split("")));
    List<String> digits16 = new ArrayList<>(List.of("0123456789abcdefABCDEF".split("")));
    List<String> letters = new ArrayList<>(List.of("qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM".split("")));
    List<String> any = new ArrayList<>();
    List<String> anyWithSpace;
    List<String> anyWithSpaceAndNewline;


    public Automaton() {
        any.addAll(letters);
        any.addAll(digits10);
        any.addAll(List.of("+/-*()!@#$%^&{}[]?<>|\\,:".split("")));
        anyWithSpace = new ArrayList<>(any);
        anyWithSpace.add(" ");
        anyWithSpace.add(".");
        anyWithSpace.add("\t");
        anyWithSpaceAndNewline = new ArrayList<>(anyWithSpace);
        anyWithSpaceAndNewline.add("\r");
        anyWithSpaceAndNewline.add("\n");
        start = new State("Start parsing...", false, LexemType.NOT_RECOGNIZED);
        incorrect.appendRule(any, incorrect);
    }


    public List<Lexem> recognize (String s){
        var ret = new ArrayList<Lexem>();
        int i = 0;
        while (i<s.length()){
            if(s.charAt(i) == ' ') i++;
            var lexem = this.parseWord(s, i);
            if (keyWords.contains(lexem.getLetters())){
                lexem.setType(LexemType.KEYWORD);
            }
            if (lexem.getLetters().length() == 0){
                try {
                    ret.add(new Lexem(LexemType.NOT_RECOGNIZED, String.valueOf(s.charAt(i))));
                    i++;
                }
                catch (StringIndexOutOfBoundsException e){
                }
            }
            else {
                ret.add(lexem);
//            System.out.println(lexem.getType());
                i += lexem.getLetters().length();
            }
        }
        return ret;
    }

    private Lexem parseWord(String s, Integer from){
        this.current = start;
        var ret = new Lexem();
        int i = from;
        while(i < s.length()){
            var type = this.putSymbol(String.valueOf(s.charAt(i)));
            if (type != null) {
                ret.setType(type);
                ret.append(String.valueOf(s.charAt(i)));
            }
            else return ret;
            i++;
        }
        return ret;
    }

    private LexemType putSymbol(String s){
        try {
//            System.out.printf("char %s ", s);
            var next = this.current.map(s);
            if (next == null) return null;
            else current = next;
//            System.out.println(this.current.getName());
            return current.getType();
        }
        catch (NullPointerException e){
            return LexemType.NOT_RECOGNIZED;
        }
    }

    public void initAsCppLexer(){
        initIdentifierStates();
        initNumericStates();
        initStringCharStates();
        initCommentStates();
        initPunctuationOperatorStates();
    }

    private void initIdentifierStates(){
        var q1 = new State("got literal", true, LexemType.IDENTIFIER);
        start.appendRule(letters, q1);
        q1.appendRule(letters, q1);
        q1.appendRule(digits10, q1);
    }

    private void initNumericStates(){
        initIntegerStates();
        var q0 = new State("zero after start", true, LexemType.INT10);
        start.appendRule("0", q0);
        initOctoStates(q0);
        initHexStates(q0);
    }

    private void initIntegerStates(){
        var q3 = new State("got number 1-9 from start, it`s float or int10", true, LexemType.INT10);
        var oneToNine = new ArrayList<>(List.of("123456789".split("")));
        start.appendRule(oneToNine, q3);
        q3.appendRule(any, incorrect);
        q3.appendRule(digits10, q3);
        initULStates(q3);
        var q4 = new State("got . after int", true, LexemType.FLOAT);
        q3.appendRule(".",q4);
        q4.appendRule(digits10, q4);
        var q1 = new State("got e", false, LexemType.NOT_RECOGNIZED);
        var q2 = new State("got numbers after e", true, LexemType.FLOAT);
        q3.appendRule("e", q1);
        q4.appendRule("e", q1);
        q1.appendRule(any, incorrect);
        q1.appendRule("-", q1);
        q1.appendRule(digits10, q2);
        q2.appendRule(any, incorrect);
        q2.appendRule(digits10, q2);
    }

    private void initOctoStates(State s){
        var q1 = new State("got 0, then 1-7", true, LexemType.INT8);
        var oneToSeven = new ArrayList<>(List.of("1234567".split("")));
        s.appendRule(oneToSeven, q1);
        q1.appendRule(any, incorrect);
        q1.appendRule(digits8 ,q1);
        initULStates(q1);
    }

    private void initHexStates(State s){
        var q0 = new State("got x after 0", false, LexemType.NOT_RECOGNIZED);
        s.appendRule(List.of("xX".split("")), q0);
        var q1 = new State("got 1-f", true, LexemType.INT16);
        var oneToSixteen = new ArrayList<>(List.of("123456789abcdef".split("")));
        q0.appendRule(any, incorrect);
        q0.appendRule(oneToSixteen, q1);
        q1.appendRule(any, incorrect);
        q1.appendRule(digits16 ,q1);
        initULStates(q1);
    }

    private void initULStates(State s){
        var sU = new State(s.getName().concat(", then U"), true, s.getType());
        var sUL = new State(sU.getName().concat(", then L"), true, s.getType());
        var sL = new State(s.getName().concat(", then L"), true, s.getType());
        var sLU = new State(sL.getName().concat(", then U"), true, s.getType());
        s.appendRule(List.of("Uu".split("")), sU);
        sU.appendRule(List.of("Ll".split("")), sUL);
        s.appendRule(List.of("Ll".split("")), sL);
        sL.appendRule(List.of("Uu".split("")), sLU);
    }

    private void initStringCharStates(){
        var q0  = new State("String started", false, LexemType.NOT_RECOGNIZED);
        start.appendRule("\"", q0);
        q0.appendRule(anyWithSpace, q0);
        var q1 = new State("String finished", false, LexemType.STRING);
        q0.appendRule("\"", q1);

        q0  = new State("Char started", false, LexemType.NOT_RECOGNIZED);
        start.appendRule("'", q0);
        q1 = new State("Got char", false, LexemType.NOT_RECOGNIZED);
        q0.appendRule(anyWithSpaceAndNewline, q1);
        var q2 = new State("Char finished", true, LexemType.CHARACTER);
        q1.appendRule("'", q2);
    }

    private void initCommentStates(){
        var q0 = new State("/ input", false, LexemType.NOT_RECOGNIZED);
        start.appendRule("/", q0);
        var q1 = new State("line comment input", true, LexemType.COMMENT);
        q0.appendRule("/", q1);
        q1.appendRule(anyWithSpace, q1);
        var q2 = new State("multiline comment start", false, LexemType.NOT_RECOGNIZED);
        q0.appendRule("*", q2);
        q2.appendRule(anyWithSpaceAndNewline, q2);
        var q3 = new State("got * after multiline comment", false, LexemType.NOT_RECOGNIZED);
        q2.appendRule("*", q3);
        q3.appendRule(anyWithSpaceAndNewline, q2);
        var q4 = new State("finished multiline comment", true, LexemType.COMMENT);
        q3.appendRule("/", q4);
        var q5 = new State("newline or tab input", true, LexemType.COMMENT);
        start.appendRule("\t", q5);
        start.appendRule("\n", q5);
        start.appendRule("\r", q5);
        start.appendRule(" ", q5);
    }

    private void initPunctuationOperatorStates(){
        var q0 = new State("Punctuation", true, LexemType.PUNCTUATION);
        start.appendRule(List.of(",;:()[]{}".split("")), q0);

        q0 = new State("+ input", true, LexemType.OPERATOR);
        start.appendRule("+", q0);
        var q1 = new State("++ or +=", true, LexemType.OPERATOR);
        q0.appendRule(List.of("+=".split("")), q1);

        q0 = new State("- input", true, LexemType.OPERATOR);
        start.appendRule("-", q0);
        q1 = new State("- or -=", true, LexemType.OPERATOR);
        q0.appendRule(List.of("-=".split("")), q1);

        q0 = new State("! input", true, LexemType.OPERATOR);
        start.appendRule("!", q0);
        q1 = new State("!=", true, LexemType.OPERATOR);
        q0.appendRule("=", q1);

        q0 = new State("* input", true, LexemType.OPERATOR);
        start.appendRule("*", q0);
        q1 = new State("*=", true, LexemType.OPERATOR);
        q0.appendRule("=", q1);

        q0 = start.map("/");
        q1 = new State("/=", true, LexemType.OPERATOR);
        q0.appendRule("=", q1);

        q0 = new State("> input", true, LexemType.OPERATOR);
        start.appendRule(">", q0);
        q1 = new State(">>", true, LexemType.OPERATOR);
        q0.appendRule(">", q1);
        var q2 = new State(">=", true, LexemType.OPERATOR);
        q0.appendRule("=", q2);
        var q3 = new State(">>=", true, LexemType.OPERATOR);
        q1.appendRule("=", q3);

        q0 = new State("< input", true, LexemType.OPERATOR);
        start.appendRule("<", q0);
        q1 = new State("<<", true, LexemType.OPERATOR);
        q0.appendRule("<", q1);
        q2 = new State("<=", true, LexemType.OPERATOR);
        q0.appendRule("=", q2);
        q3 = new State("<<=", true, LexemType.OPERATOR);
        q1.appendRule("=", q3);

        q0 = new State("= input", true, LexemType.OPERATOR);
        start.appendRule("=", q0);
        q1 = new State("==", true, LexemType.OPERATOR);
        q0.appendRule("=", q1);
    }
}
