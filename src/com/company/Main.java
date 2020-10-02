package com.company;

public class Main {

    public static void main(String[] args) {
	    var l = new Automaton();
	    l.initAsCppLexer();
	    l.recognize("//das");
    }
}
