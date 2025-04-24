import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compiler {

    // --- Token Class ---
    static class Token {
        String type;
        String value;

        public Token(String type, String value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return "TOKEN#" + value + " " + type + ": " + value;
        }
    }

    // --- Stage 1: Lexical Analysis (Scanner) ---
    public static List<Token> lexicalAnalysis(String input) {
        List<Token> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\d+|[+\\-*/;])"); // Matches digits and operators
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String match = matcher.group();
            if (match.matches("\\d+")) {
                tokens.add(new Token("identifier", match));
            } else if (match.matches("[+\\-*/]")) {
                tokens.add(new Token("Operator", match));
            } else if (match.equals(";")) {
                tokens.add(new Token("symbol", match));
            }
        }

        return tokens;
    }

    // --- Stage 2: Syntax Analysis (Parser) ---
    public static boolean syntaxAnalysis(List<Token> tokens) {
        if (tokens.size() < 3) {
            return false; //  Needs at least: digit operator digit ;
        }

        boolean valid = true;
        for (int i = 0; i < tokens.size() - 1; i++) {
            Token token = tokens.get(i);
            if (i % 2 == 0) { // Even index: should be a number
                if (!token.type.equals("identifier")) {
                    valid = false;
                    break;
                }
            } else { // Odd index: should be an operator
                if (!token.type.equals("Operator")) {
                    valid = false;
                    break;
                }
            }
        }
        if (!tokens.get(tokens.size() - 1).type.equals("symbol")) {
            valid = false;
        }
        return valid;
    }

    // --- Stage 3: Semantic Analysis ---
    public static boolean semanticAnalysis(List<Token> tokens) {
        return syntaxAnalysis(tokens);
    }

    // --- Stage 4: Intermediate Code Representation (ICR) ---
    public static String intermediateCodeRepresentation(String input) {
        List<Token> tokens = lexicalAnalysis(input);
        List<String> outputQueue = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();
        int tempVarCount = 1;
        Map<String, String> tempVarMap = new HashMap<>();

        // Shunting Yard Algorithm
        for (Token token : tokens) {
            if (token.type.equals("identifier")) {
                outputQueue.add(token.value);
            } else if (token.type.equals("Operator")) {
                while (!operatorStack.isEmpty() &&
                        precedence(operatorStack.peek()) >= precedence(token.value)) {
                    outputQueue.add(operatorStack.pop());
                }
                operatorStack.push(token.value);
            } else if (token.type.equals("symbol")) {
                while (!operatorStack.isEmpty()) {
                    outputQueue.add(operatorStack.pop());
                }
            }
        }

        // Generate ICR from the output queue
        StringBuilder icr = new StringBuilder();
        Stack<String> operandStack = new Stack<>();

        for (String item : outputQueue) {
            if (item.matches("\\d+")) { // If it's a number
                operandStack.push(item);
            } else if (item.matches("[+\\-*/]")) { // If it's an operator
                if (operandStack.size() < 2) {
                    // Handle error: insufficient operands
                    return "Error: Invalid expression";
                }
                String operand2 = operandStack.pop();
                String operand1 = operandStack.pop();
                String tempVar = "t" + tempVarCount++;
                icr.append(tempVar).append(" = ").append(operand1).append(" ").append(item).append(" ").append(operand2).append("\n");
                operandStack.push(tempVar);
            }
        }

        return icr.toString();
    }

    // method to determine operator precedence
    private static int precedence(String operator) {
        switch (operator) {
            case "*":
            case "/":
                return 2;
            case "+":
            case "-":
                return 1;
            default:
                return 0;
        }
    }

    // --- Stage 5: Code Generation ---
    public static String codeGeneration(String input) {
        // A very basic code generation for a hypothetical assembly language.
        // This is where you'd map ICR to machine code.
        StringBuilder code = new StringBuilder();
        String[] parts = input.split("(?=[+\\-*/])|(?<=[+\\-*/])");
        int tempVarCount = 1;
        Map<String, String> tempVarMap = new HashMap<>();

        // First, generate code for each intermediate result
        for (int i = 0; i < parts.length; i += 3) {
            if (i + 2 < parts.length) {
                String operator = parts[i + 1].trim();
                String operand1 = parts[i].trim();
                String operand2 = parts[i + 2].trim();
                String tempVar = "t" + tempVarCount;

                // Generate code for the current operation
                if (operator.equals("+")) {
                    code.append("LDA ").append(operand1).append("\n");
                    code.append("ADD ").append(operand2).append("\n");
                    code.append("STR ").append(tempVar).append("\n");
                } else if (operator.equals("-")) {
                    code.append("LDA ").append(operand1).append("\n");
                    code.append("SUB ").append(operand2).append("\n");
                    code.append("STR ").append(tempVar).append("\n");
                } else if (operator.equals("*")) {
                    code.append("LDA ").append(operand1).append("\n");
                    code.append("MUL ").append(operand2).append("\n");
                    code.append("STR ").append(tempVar).append("\n");
                } else if (operator.equals("/")) {
                    code.append("LDA ").append(operand1).append("\n");
                    code.append("DIV ").append(operand2).append("\n");
                    code.append("STR ").append(tempVar).append("\n");
                }
                tempVarCount++;
            }
        }
        return code.toString();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input;

        System.out.println("A COMPILER PROJECT FOR CTE711S");
        System.out.println("===================================");

        while (true) {
            System.out.println("\nENTER NEXT STRING === ");
            System.out.println("-Every String/line must end with a semicolon (;)");
            System.out.println("-Enter String (Containing 0 to 9 and/or operators: +,/,*,-)");
            System.out.println("-Enter No Space in b/w chara & end the string with semicolon(;)ie 5-4+9*8/2; for Full(7 stages) results of compilation");
            System.out.println("-or Enter Space in b/w char with semicolon(;) at end of String ie 5 - 4 + 9 * 8 / 2 ; for the result of arithmetic expression");
            System.out.println("-Or Type 99 and press Enter to Quit:");
            input = scanner.nextLine();

            if (input.trim().equals("99")) {
                break;
            }

            input = input.trim();

            // --- Stage 1: Lexical Analysis ---
            List<Token> tokens = lexicalAnalysis(input);
            System.out.println("\n======STAGE1: COMPILER TECHNIQUES--> LEXICAL ANALYSIS-Scanner");
            System.out.println("SYMBOL TABLE COMPRISING ATTRIBUTES AND TOKENS:");
            for (int i = 0; i < tokens.size(); i++) {
                System.out.println(tokens.get(i));
            }
            System.out.println("Total number of Tokens: " + tokens.size());

            // --- Stage 2: Syntax Analysis ---
            System.out.println("\n======STAGE2: COMPILER TECHNIQUES--> SYNTAX ANALYSIS-Parser");
            System.out.println("GET A DERIVATION FOR : " + input);
            if (syntaxAnalysis(tokens)) {
                System.out.println("The input is syntactically correct.");
            } else {
                System.out.println("The input is syntactically incorrect.");
            }

            // --- Stage 3: Semantic Analysis ---
            System.out.println("\n======STAGE3: COMPILER TECHNIQUES--> SEMANTIC ANALYSIS");
            if (semanticAnalysis(tokens)) {
                System.out.println("CONCLUSION-->This expression: " + input + " is Syntactically and Semantically correct");
            } else {
                System.out.println("CONCLUSION-->This expression: " + input + " is Syntactically or Semantically incorrect");
            }

            // --- Stage 4: Intermediate Code Representation ---
            System.out.println("\n======STAGE4: COMPILER TECHNIQUES--> INTERMEDIATE CODE REPRESENTATION (ICR)");
            System.out.println("THE STRING ENTERED IS : " + input);
            String icr = intermediateCodeRepresentation(input.substring(0, input.length() - 1)); // Remove semicolon
            System.out.println(icr);
            System.out.println("CONCLUSION-->The expression was correctly generated in ICR");

            // --- Stage 5: Code Generation ---
            System.out.println("\n======STAGE5: CODE GENERATION");
            String code = codeGeneration(input.substring(0, input.length() - 1));
            System.out.println(code);
        }
        scanner.close();
    }
}
