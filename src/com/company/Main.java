package com.company;

import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
		System.out.println("Enter file path:");
//		Scanner input = new Scanner("C:\\Users\\admin\\IdeaProjects\\SysProgLab3varcpp\\src\\input.txt");//(System.in);
		Scanner input = new Scanner(System.in);
		String filePath = input.nextLine();
		try {
			FileReader fr = new FileReader(filePath);
			int i;
			StringBuilder sb = new StringBuilder();
			while ((i=fr.read()) != -1)
				sb.append((char) i);
			String text = sb.toString();
			var l = new Automaton();
			l.initAsCppLexer();
			String str = "";
			for(var lexem:l.recognize(text)) {
				if(lexem.getType() != LexemType.COMMENT) {
					str = str.concat(lexem.toString());
					str = str.concat("\n");
				}
			}
			System.out.print(str);
			BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\admin\\IdeaProjects\\SysProgLab3varcpp\\src\\output.txt"));
			writer.write(str);
			writer.close();
		}
		catch (FileNotFoundException e){
			System.out.println("No such file");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
