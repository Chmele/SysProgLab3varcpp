package com.company;

import java.util.ArrayList;
import java.util.List;

public class Automaton {
    private State start;
    private State current;
    private List<String> keyWords = new ArrayList<String>(List.of("auto", "class", "continue", "delete",
            "do", "double", "enum", "for", "if", "int", "long", "new", "or", "return"));

    List<String> digits10 = new ArrayList<String>(List.of("0123456789".split("")));
    List<String> digits8 = new ArrayList<String>(List.of("01234567".split("")));
    List<String> digits16 = new ArrayList<String>(List.of("0123456789abcdefABCDEF".split("")));
    List<String> letters = new ArrayList<String>(List.of("qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM".split("")));
    List<String> any = new ArrayList<String>();
    List<String> anyWithSpace = new ArrayList<String>();


    public Automaton() {
        any.addAll(letters);
        any.addAll(digits10);
        anyWithSpace = new ArrayList<>(any);
        anyWithSpace.add(" ");
        start = new State("Start parsing...", false, LexemType.NOT_RECOGNIZED);
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
            ret.add(lexem);
//            System.out.println(lexem.getType());
            i += lexem.getLetters().length();
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
        q1.appendRule(any, q1);
    }

    private void initNumericStates(){
        initIntegerStates();
        var q0 = new State("zero after start", true, LexemType.INT10);
        initOctoStates(q0);
        initHexStates(q0);
    }

    private void initIntegerStates(){
        var q3 = new State("got number 1-9 from start, it`s float or int10", true, LexemType.INT10);
        var oneToNine = new ArrayList<String>(List.of("123456789".split("")));
        start.appendRule(oneToNine, q3);
        q3.appendRule(digits10,q3);
        initULStates(q3);
        var q4 = new State("got . after int", true, LexemType.FLOAT);
        q3.appendRule(".",q4);
        q4.appendRule(digits10, q4);
    }

    private void initOctoStates(State s){
        var q1 = new State("got 0, then 1-7", true, LexemType.INT8);
        var oneToSeven = new ArrayList<String>(List.of("1234567".split("")));
        s.appendRule(oneToSeven, q1);
        q1.appendRule(digits8 ,q1);
        initULStates(q1);
    }

    private void initHexStates(State s){
        var q0 = new State("got x after 0", false, LexemType.NOT_RECOGNIZED);
        s.appendRule(List.of("xX".split("")), q0);
        var q1 = new State("got 1-f", true, LexemType.INT16);
        var oneToSixteen = new ArrayList<String>(List.of("123456789abcdef".split("")));
        s.appendRule(oneToSixteen, q1);
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
        q0.appendRule(any, q0);
        var q1 = new State("String finished", false, LexemType.STRING);
        q0.appendRule("\"", q1);

        q0  = new State("Char started", false, LexemType.NOT_RECOGNIZED);
        start.appendRule("'", q0);
        q1 = new State("Got char", false, LexemType.NOT_RECOGNIZED);
        q0.appendRule(any, q1);
        var q2 = new State("Char finished", true, LexemType.CHARACTER);
        q1.appendRule("'", q2);
    }

    private void initCommentStates(){
        var q0 = new State("/ input", false, LexemType.NOT_RECOGNIZED);
        start.appendRule("/", q0);
        var q1 = new State("comment input", true, LexemType.COMMENT);
        q0.appendRule("/", q1);
        q1.appendRule(anyWithSpace, q1);
    }

    private void initPunctuationOperatorStates(){
        var q0 = new State("Punctuation", true, LexemType.PUNCTUATION);
        start.appendRule(List.of(",;:()[]".split("")), q0);

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
        start.appendRule("+", q0);
        q1 = new State(">> or >=", true, LexemType.OPERATOR);
        q0.appendRule(List.of(">=".split("")), q1);

        q0 = new State("= input", true, LexemType.OPERATOR);
        start.appendRule("=", q0);
        q1 = new State("==", true, LexemType.OPERATOR);
        q0.appendRule("=", q1);
    }
}
