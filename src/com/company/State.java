package com.company;

import java.util.HashMap;
import java.util.List;

public class State {
    private String name;
    private HashMap<Object, State> rules;
    private boolean isFinal;
    private LexemType type;
    public State(String s, boolean isFinal, LexemType type){
        name = s;
        this.type = type;
        this.isFinal = isFinal;
        rules = new HashMap<>();
    }

    public LexemType getType(){
        return type;
    }

    public String getName(){
        return name;
    }

    public void appendRule(List<String> o, State s){
        for (var i:o){
            this.appendRule(i, s);
        }
    }

    public void appendRule(String o, State s){
        rules.put(o, s);
    }

    public State map(Object o){
        return rules.get(o);
    }
}
