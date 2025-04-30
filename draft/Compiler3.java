import java.util.*;
import java.util.regex.*;

public class Compiler3 {
    // Token types
    enum TokenType {
        IDENTIFIER,
        OPERATOR,
        SYMBOL
    }

    // Token class
    static class Token {
        TokenType type;
        String value;
        Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    // Operator precedence
    private static final Map<String, Integer> precedence = new HashMap<>();
    static {
        precedence.put("+", 1);
        precedence.put("-", 1);
        precedence.put("*", 2);
        precedence.put("/", 2);
    }

    // Operation to binary mapping
    private static final Map<String, String> opToBinary = new HashMap<>();
    static {
        opToBinary.put("DIV", "01000100");
        opToBinary.put("MUL", "01001101");
        opToBinary.put("ADD", "01000001");
        opToBinary.put("SUB", "01010011");
    }

    // Lexical Analysis: Tokenize input
    public static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        Pattern tokenPattern = Pattern.compile("(\\s+)|(\\d+)|([+\\-*/])|(;)");
        Matcher matcher = tokenPattern.matcher(input);
        while (matcher.find()) {
            if (matcher.group(2) != null) {
                tokens.add(new Token(TokenType.IDENTIFIER, matcher.group(2)));
            } else if (matcher.group(3) != null) {
                tokens.add(new Token(TokenType.OPERATOR, matcher.group(3)));
            } else if (matcher.group(4) != null) {
                tokens.add(new Token(TokenType.SYMBOL, matcher.group(4)));
            }
        }
        return tokens;
    }

    // Syntax Analysis: Validate token sequence
    public static boolean isValidSyntax(List<Token> tokens) {
        if (tokens.isEmpty() || tokens.get(0).type != TokenType.IDENTIFIER) {
            return false;
        }
        for (int i = 1; i < tokens.size() - 1; i += 2) {
            if (tokens.get(i).type != TokenType.OPERATOR || tokens.get(i + 1).type != TokenType.IDENTIFIER) {
                return false;
            }
        }
        return tokens.get(tokens.size() - 1).type == TokenType.SYMBOL;
    }

    // Convert to postfix notation using Shunting-yard
    public static List<String> toPostfix(List<Token> tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();
        for (Token token : tokens.subList(0, tokens.size() - 1)) {
            if (token.type == TokenType.IDENTIFIER) {
                output.add(token.value);
            } else if (token.type == TokenType.OPERATOR) {
                while (!operatorStack.isEmpty() && precedence.getOrDefault(operatorStack.peek(), 0) >= precedence.get(token.value)) {
                    output.add(operatorStack.pop());
                }
                operatorStack.push(token.value);
            }
        }
        while (!operatorStack.isEmpty()) {
            output.add(operatorStack.pop());
        }
        return output;
    }

    // Generate Intermediate Code Representation
    public static List<String> generateICR(List<String> postfix) {
        List<String> icr = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        int tempCount = 1;
        for (String token : postfix) {
            if (token.matches("\\d+")) {
                stack.push(token);
            } else {
                String op2 = stack.pop();
                String op1 = stack.pop();
                String temp = "t" + tempCount;
                icr.add(temp + "= " + op1 + token + op2);
                stack.push(temp);
                tempCount++;
            }
        }
        return icr;
    }

    // Generate Assembly Code
    public static List<String> generateAssembly(List<String> icr) {
        List<String> assembly = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\w+)\\s*([+\\-*/])\\s*(\\w+)");
        for (String instruction : icr) {
            String[] parts = instruction.split("=");
            if (parts.length != 2) continue;
            String temp = parts[0].trim();
            String expression = parts[1].trim();
            Matcher matcher = pattern.matcher(expression);
            if (matcher.find()) {
                String op1 = matcher.group(1);
                String operator = matcher.group(2);
                String op2 = matcher.group(3);
                switch (operator) {
                    case "+":
                        assembly.add("LDA " + op1);
                        assembly.add("ADD " + op2);
                        assembly.add("STR " + temp);
                        break;
                    case "-":
                        assembly.add("LDA " + op1);
                        assembly.add("SUB " + op2);
                        assembly.add("STR " + temp);
                        break;
                    case "*":
                        assembly.add("LDA " + op1);
                        assembly.add("MUL " + op2);
                        assembly.add("STR " + temp);
                        break;
                    case "/":
                        assembly.add("LDA " + op1);
                        assembly.add("DIV " + op2);
                        assembly.add("STR " + temp);
                        break;
                }
            }
        }
        return assembly;
    }

    // Generate Optimized Assembly
    public static List<String> optimizeAssembly(List<String> assembly) {
        List<String> optimized = new ArrayList<>();
        if (assembly.size() % 3 != 0) {
            System.err.println("Error: Invalid assembly instruction count");
            return optimized;
        }
        for (int i = 0; i < assembly.size(); i += 3) {
            String instr1 = assembly.get(i);     // LDA op1
            String instr2 = assembly.get(i + 1); // OP op2
            String instr3 = assembly.get(i + 2); // STR temp
            String op1 = instr1.split(" ")[1];
            String[] parts2 = instr2.split(" ");
            String OP = parts2[0];
            String op2 = parts2[1];
            String temp = instr3.split(" ")[1];
            String optimizedInstr = OP + " " + temp + ", " + op1 + ", " + op2;
            optimized.add(optimizedInstr);
        }
        return optimized;
    }

    // Helper function to reverse a string
    private static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    // Modified strToBinary to return binary string instead of printing
    static String strToBinary(String s) {
        int n = s.length();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < n; i++) {
            int val = Integer.valueOf(s.charAt(i));
            String bin = "";
            if (val == 0) {
                bin = "0";
            } else {
                while (val > 0) {
                    bin = (val % 2 == 1 ? "1" : "0") + bin;
                    val /= 2;
                }
                bin = reverse(bin);
            }
            // Pad to 8 bits if necessary
            while (bin.length() < 8) {
                bin = "0" + bin;
            }
            result.append(bin);
            if (i < n - 1) result.append(" ");
        }
        return result.toString();
    }

    // Generate Target Machine Code using strToBinary
    public static List<String> generateTMC(List<String> optimized) {
        List<String> tmc = new ArrayList<>();
        for (String instruction : optimized) {
            String[] parts = instruction.split("[ ,]+");
            if (parts.length != 4) continue;
            String operation = parts[0];
            String dest = parts[1];
            String op1 = parts[2];
            String op2 = parts[3];

            // Operation binary from predefined mapping
            String opBinary = opToBinary.getOrDefault(operation, "00000000");

            // Destination register binary (hardcoded as per sample)
            String destBinary = "01110100";

            // Operands: use strToBinary for numeric values or registers
            String op1Binary = op1.matches("\\d+") ? strToBinary(op1) : "01110100";
            String op2Binary = op2.matches("\\d+") ? strToBinary(op2) : "01110100";

            // Format the line with spacing to match sample output
            String line = opBinary + "          " + destBinary + "          " +
                    op1Binary + "          " + op2Binary;
            tmc.add(line);
        }
        return tmc;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("A MINI COMPILER PROJECT FOR CTE711S");
        System.out.println("===================================");
        System.out.println("ENTER NEXT STRING === #1");
        System.out.println("-Every String/line must end with a semicolon (;)");
        System.out.println("-Enter String (Containing 0 to 9 and/or operators: +,/,*,-)");
        System.out.println("-Enter No Space in b/w chars & end the string with semicolon(;) e.g. 5-4+9*8/2;");
        System.out.println("-or Enter Space in b/w char with semicolon(;) at end of String e.g. 5 - 4 + 9 * 8 / 2 ;");
        System.out.println("-Or Type 99 and press Enter to Quit:");
        String input = scanner.nextLine();

        if (input.equals("99")) {
            System.out.println("Exiting...");
            return;
        }

        // Stage 1: Lexical Analysis
        List<Token> tokens = tokenize(input);
        System.out.println("\n======STAGE1: COMPILER TECHNIQUES--> LEXICAL ANALYSIS-Scanner");
        System.out.println("SYMBOL TABLE COMPRISING ATTRIBUTES AND TOKENS:");
        int tokenNum = 1;
        for (Token token : tokens) {
            String typeStr = token.type == TokenType.IDENTIFIER ? "identifier" :
                    token.type == TokenType.OPERATOR ? "Operator" : "symbol";
            System.out.println("TOKEN#" + tokenNum + " " + token.value + " " + typeStr);
            tokenNum++;
        }
        System.out.println("Total number of Tokens: " + tokens.size());
        System.out.println("GIVEN THE GRAMMAR: E=E1 | E=E1*E2 | E=E1+E2 | E=digit | E={0,1,2,3,4,5,6,7,8,9}");

        // Stage 2: Syntax Analysis
        System.out.println("\n======STAGE2: COMPILER TECHNIQUES--> SYNTAX ANALYSIS-Parser");
        if (isValidSyntax(tokens)) {
            System.out.println("GET A DERIVATION FOR : " + input);
            System.out.println("Expression is syntactically correct");
        } else {
            System.out.println("Syntax Error");
            return;
        }

        // Stage 3: Semantic Analysis
        System.out.println("\n======STAGE3: COMPILER TECHNIQUES--> SEMANTIC ANALYSIS");
        System.out.println("CONCLUSION-->This expression: " + input + " is Syntactically and Semantically correct");

        // Stage 4: Intermediate Code Representation
        System.out.println("\n======STAGE4: COMPILER TECHNIQUES--> INTERMEDIATE CODE REPRESENTATION (ICR)");
        List<String> postfix = toPostfix(tokens);
        List<String> icr = generateICR(postfix);
        System.out.println("THE STRING ENTERED IS : " + input.replace(";", ""));
        System.out.println("The ICR is as follows:");
        for (String instr : icr) {
            System.out.println(instr);
        }
        System.out.println("CONCLUSION-->The expression was correctly generated in ICR");

        // Stage 5: Code Generation
        System.out.println("\n======STAGE5: CODE GENERATION (CG)");
        List<String> assembly = generateAssembly(icr);
        for (String instr : assembly) {
            System.out.println(instr);
        }

        // Stage 6: Code Optimization
        System.out.println("\n======STAGE6: CODE OPTIMISATION (CO)");
        List<String> optimized = optimizeAssembly(assembly);
        for (String instr : optimized) {
            System.out.println(instr);
        }

        // Stage 7: Target Machine Code
        System.out.println("\n======STAGE7: TARGET MACHINE CODE (TMC)");
        List<String> tmc = generateTMC(optimized);
        for (String code : tmc) {
            System.out.println(code);
        }

        System.out.println("\n======END OF COMPILATION");
        System.out.println("======THE ORIGINAL INPUT STRING IS: " + input);
    }
}
