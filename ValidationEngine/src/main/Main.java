/*
 * 
 */
package main;

import validation.ValidationEngine;



public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ValidationEngine validation = new ValidationEngine(args, false);
		
		validation.run();
		
	}
}