package edu.ucla.pic20a.examples.calculator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CalculatorApp {
	public static void main(String[] args) {
		new CalculatorApp();
	}

	// Initialize the app object, which builds the UI.
	private CalculatorApp() {
		JFrame calcFrame = new JFrame("Calculator");
		calcFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		calcFrame.setSize(240, 320);
		calcFrame.setVisible(true);

		// Add a label on top, to show the user's entered expression.
		calcLabel = new JLabel(" ");
		calcLabel.setHorizontalAlignment(JLabel.CENTER);
		calcFrame.getContentPane().add(calcLabel, BorderLayout.NORTH);

		// Most of the buttons are in an array in the middle.
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(6, 3));
		addMainButtons(panel);
		calcFrame.getContentPane().add(panel, BorderLayout.CENTER);

		// The equals button goes below the other buttons.
		JButton equalsButton = new JButton("=");
		equalsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				String exp = calcLabel.getText();
				double val = eval(exp);
				calcLabel.setText(Double.toString(val));
			}
		});
		calcFrame.getContentPane().add(equalsButton,
				BorderLayout.SOUTH);

		// Without this code, the buttons didn't show until I resized
		// the window. That could just be my computer, of course.
		// Source: https://stackoverflow.com/a/12295235
		calcFrame.getContentPane().revalidate();
		calcFrame.getContentPane().repaint();
	}

	// Add the main buttons: digits, '+', '*', etc.
	private void addMainButtons(JPanel panel) {
		// Top row
		panel.add(makeTextAdderButton("7"));
		panel.add(makeTextAdderButton("8"));
		panel.add(makeTextAdderButton("9"));
		// Second row
		panel.add(makeTextAdderButton("4"));
		panel.add(makeTextAdderButton("5"));
		panel.add(makeTextAdderButton("6"));
		// Third row
		panel.add(makeTextAdderButton("1"));
		panel.add(makeTextAdderButton("2"));
		panel.add(makeTextAdderButton("3"));
		// Fourth row
		panel.add(makeTextAdderButton("."));
		panel.add(makeTextAdderButton("0"));
		JButton backspaceButton = new JButton("←");
		backspaceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				String s = calcLabel.getText();
				if (s.length() > 0)
					s = s.substring(0, s.length() - 1);
				calcLabel.setText(s);
			}
		});
		panel.add(backspaceButton);
		// Fifth row
		panel.add(makeTextAdderButton("+"));
		panel.add(makeTextAdderButton("-"));
		panel.add(makeTextAdderButton("*"));
		// Sixth row
		panel.add(makeTextAdderButton("/"));
		panel.add(makeTextAdderButton("("));
		panel.add(makeTextAdderButton(")"));
	}

	// Create a single button, which when clicked, appends textToAdd to
	// calcLabel.
	private JButton makeTextAdderButton(String textToAdd) {
		JButton button = new JButton(textToAdd);
		button.addActionListener(new TextAdder(textToAdd));
		return button;
	}

	// Action listener for the text-adder buttons.
	private class TextAdder implements ActionListener {
		private TextAdder(String textToAdd) {
			this.textToAdd = textToAdd;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			calcLabel.setText(calcLabel.getText() + textToAdd);
		}

		private final String textToAdd;
	}

	// Member variables.
	private final JLabel calcLabel;

	// A function to evaluate an arithmetic expression, entered as a string.
	//
	// Source: https://stackoverflow.com/a/26227947 (credit to user Boann;
	// code released to the public domain).
	//
	// Comments marked with [AK] added by me (Andrew Krieger). This is the
	// last function in the CalculatorApp class, so don't worry about
	// missing anything else relevant to the UI.
	public static double eval(final String str) {
		// [AK]: The code `new Object() { ... }` defines an anonymous
		// class. This is an unnamed class extending Object, and with
		// the members and methods defined below.
		return new Object() {
			int pos = -1, ch;

			void nextChar() {
				ch = (++pos < str.length()) ? str.charAt(pos) : -1;
			}

			boolean eat(int charToEat) {
				while (ch == ' ') nextChar();
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			double parse() {
				nextChar();
				double x = parseExpression();
				if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
				return x;
			}

			// Grammar:
			// expression = term | expression `+` term | expression `-` term
			// term = factor | term `*` factor | term `/` factor
			// factor = `+` factor | `-` factor | `(` expression `)`
			//        | number | functionName factor | factor `^` factor

			double parseExpression() {
				double x = parseTerm();
				for (;;) {
					if      (eat('+')) x += parseTerm(); // addition
					else if (eat('-')) x -= parseTerm(); // subtraction
					else return x;
				}
			}

			double parseTerm() {
				double x = parseFactor();
				for (;;) {
					if      (eat('*')) x *= parseFactor(); // multiplication
					else if (eat('/')) x /= parseFactor(); // division
					else return x;
				}
			}

			double parseFactor() {
				if (eat('+')) return parseFactor(); // unary plus
				if (eat('-')) return -parseFactor(); // unary minus

				double x;
				int startPos = this.pos;
				if (eat('(')) { // parentheses
					x = parseExpression();
					eat(')');
				} else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
					while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
					x = Double.parseDouble(str.substring(startPos, this.pos));
				} else if (ch >= 'a' && ch <= 'z') { // functions
					while (ch >= 'a' && ch <= 'z') nextChar();
					String func = str.substring(startPos, this.pos);
					x = parseFactor();
					if (func.equals("sqrt")) x = Math.sqrt(x);
					else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
					else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
					else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
					else throw new RuntimeException("Unknown function: " + func);
				} else {
					throw new RuntimeException("Unexpected: " + (char)ch);
				}

				if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

				return x;
			}
		}.parse();
	}
}
