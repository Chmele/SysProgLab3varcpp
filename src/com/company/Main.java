package com.company;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
		System.out.println("Enter file path:");
		Scanner input = new Scanner("C:\\Users\\admin\\IdeaProjects\\SysProgLab3varcpp\\src\\input.txt");//(System.in);
		String filePath = input.nextLine();
		try {
			FileReader fr = new FileReader(filePath);
			int i;
			StringBuilder sb = new StringBuilder();
			while ((i=fr.read()) != -1)
				sb.append((char) i);
			String text = sb.toString();
			String[] lines = text.split("\\r?\\n|\\t");
			var l = new Automaton();
			l.initAsCppLexer();
			for (var line:lines){
				for(var lexem:l.recognize(line)) {
					System.out.println(lexem);
				}
			}
//			l.recognize(lines[0]);
		}
		catch (FileNotFoundException e){
			System.out.println("No such file");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
