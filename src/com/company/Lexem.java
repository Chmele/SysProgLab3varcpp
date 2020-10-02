package com.company;

public class Lexem {
    private String letters;
    private LexemType type;

    public Lexem(){
        this.letters = new String();
    }

    public void append(String s){
        letters = letters.concat(s);
    }

    public String toString(){
        return letters;
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
