package com.company;

public class Lexem {
    private String letters;
    private LexemType type;

    public Lexem(){
        this.letters = "";
    }

    public Lexem(LexemType t, String s){
        this.letters = s;
        this.type = t;
    }

    public void append(String s){
        letters = letters.concat(s);
    }

    public String toString(){
        return String.format("<%s> - <%s>", type, letters);
    }

    public void setType(LexemType t){
        type = t;
    }

    public LexemType getType(){
        return type;
    }

    public String getLetters(){
        return letters;
    }
}
