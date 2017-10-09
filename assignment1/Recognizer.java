// Gage Langdon
// CSc 135 Assignment 1 - Professor Radimsky
// NOTE: based on example parser from Dr. Scott Gordon

// HOW TO COMPILE IN LINUX (ATHENA):
// Compile this in linux by typing: javac Recognizer.java
// Then run the piece by typing: java Recognizer

// HOW TO COMPILE IN ECLIPSE (WINDOWS):
// Open the file in your workspace.
// Right click and click run as java application

// 	--EBNF--
//  piece ::= {stmnt ; } [lststmnt ; ]
//  block ::= piece
//  stmnt ::= assignst | whilst | ifst | forst
//  assignst ::= varlist = explst
//  whilst ::= W expr D block E
//  ifst ::= I expr T block [S block] E
//  forst ::= P varname = expr , expr [ , expr] D block E
//  lststmnt ::= R [explst] | K
//  varlist ::= varname { , varname}
//  explst ::= expr , {expr ,}
//  expr ::= term [ binop expr ] | unop expr
//  term ::= N | F | V | num | varname | ( expr )
//  binop ::= + | - | * | / | < | > | A | O
//  unop ::= - | & | #
//  varname ::= letter { letter | digit }
//  num ::= digit { digit }
//  letter ::= U | X | Y
//  digit ::= 0 | 1 | 2 | 3 | 4 | 5

// validTests: U=N,; IN+NTU=N,;E; WN+123DU=N,;E; IN+UXY05TU=N,;SU=N,;E; U123,X123,Y123=N+123,N*F,123/-1,; PU01X234Y5=#V,N+N,N*NDU=N,;E;RN+123,F+UX23,;  
// U=N,;IN+NTU=N,;E;WN+123DU=N,;E;IN+UXY05TU=N,;SU=N,;E;U123,X123,Y123=N+123,N*F,123/-1,;PU01X234Y5=#V,N+N,N*NDU=N,;E;RN+123,F+UX23,; 

import java.io.*;
import java.util.Scanner;

public class Recognizer {
	static String inputString;
	static int index = 0;
	static int errorflag = 0;

	private char token() {
		return (inputString.charAt(index));
	}

	private void advancePtr() {
		if (index < (inputString.length() - 1))
			index++;
	}

	private void match(char T) {
		System.out.println(T + " == " + token());
		if (T == token()) {
			advancePtr();
		} else {
			error();
		}
	}

	private void error() {
		System.out.println("error at position: " + index + " Token: " + token());
		errorflag = 1;
		advancePtr();
	}

	// ---------- Utilities
	private Boolean isLetter() {
		return (token() == 'U' || token() == 'X' || token() == 'Y');
	}

	private Boolean isDigit() {
		return (token() == '0' || token() == '1' || token() == '2' || token() == '3' || token() == '4'
				|| token() == '5');
	}

	private Boolean isUnop() {
		return (token() == '-' || token() == '&' || token() == '#');
	}

	private Boolean isBinop() {
		return (token() == '+' || token() == '-' || token() == '*' || token() == '/' || token() == '<' || token() == '>'
				|| token() == 'A' || token() == 'O');
	}

	private Boolean isTerm() {
		return (token() == 'N' || token() == 'F' || token() == 'V' || isDigit() || isLetter() || token() == '(');
	}

	private Boolean isExpr() {
		return (isTerm() || isUnop());
	}

	private Boolean isStmnt() {
		return (isLetter() || token() == 'W' || token() == 'I' || token() == 'P');
	}

	private Boolean isLstStmnt() {
		return (token() == 'K' || token() == 'R');
	}

	// ---------- Grammar
	private void piece() {
		if (!isStmnt()) {
			error();
			return;
		}

		while (isStmnt()) {
			stmnt();
			match(';');
		}
		if (isLstStmnt()) {
			lststmnt();
			match(';');
		}
	}

	private void block() {
		piece();
	}

	private void stmnt() {
		if (isLetter())
			assignst();
		else if (token() == 'W')
			whilst();
		else if (token() == 'I')
			ifst();
		else if (token() == 'P')
			forst();
		else
			error();
	}

	private void assignst() {
		varlist();
		match('=');
		explst();
	}

	private void whilst() {
		match('W');
		expr();
		match('D');
		block();
		match('E');
	}

	private void ifst() {
		match('I');
		expr();
		match('T');
		block();
		if (token() == 'S') {
			match('S');
			block();
		}
		match('E');
	}

	private void forst() {
		match('P');
		varname();
		match('=');
		expr();
		match(',');
		expr();
		if (token() == ',') {
			match(',');
			expr();
		}
		match('D');
		block();
		match('E');
	}

	private void lststmnt() {
		if (token() == 'K')
			match('K');
		else if (token() == 'R')
			match('R');
		if (isExpr())
			explst();
		else
			error();
	}

	private void varlist() {
		if (isLetter()) {
			varname();
			while (token() == ',') {
				match(',');
				varname();
			}
		} else
			error();
	}

	private void explst() {
		expr();
		match(',');
		if (isExpr())
			do {
				expr();
				match(',');
			} while (isExpr());
	}

	private void expr() {
		if (isTerm()) {
			term();
			if (isBinop()) {
				binop();
				expr();
			}
		} else if (isUnop()) {
			unop();
			expr();
		} else
			error();
	}

	private void term() {
		if (token() == 'N' || token() == 'F' || token() == 'V')
			match(token());
		else if (isDigit())
			num();
		else if (isLetter())
			varname();
		else if (token() == '(') {
			match('(');
			expr();
			match(')');
		} else {
			error();
		}
	}

	private void binop() {
		if (isBinop())
			match(token());
		else
			error();
	}

	private void unop() {
		if (isUnop())
			match(token());
		else
			error();
	}

	private void varname() {
		if (isLetter())
			do {
				if (isDigit())
					digit();
				else if (isLetter())
					letter();
			} while (isLetter() || isDigit());
	}

	private void num() {
		do
			digit();
		while (isDigit());
	}

	private void letter() {
		if (isLetter())
			match(token());
		else
			error();
	}

	private void digit() {
		if (isDigit())
			match(token());
		else
			error();
	}

	// ---------- Start
	private void start() {
		piece();
		if (token() == '$')
			match('$');
		if (errorflag == 0)
			System.out.println("legal." + "\n");
		else
			System.out.println("errors found." + "\n");
	}

	// ---------- Main
	public static void main(String[] args) throws IOException {
		Recognizer rec = new Recognizer();
		Scanner input = new Scanner(System.in);
		System.out.print("\n" + "enter an expression: ");
		inputString = input.nextLine();
		rec.start();
	}
}