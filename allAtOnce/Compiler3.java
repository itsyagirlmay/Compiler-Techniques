package allAtOnce;

import java.util.*;
import java.util.regex.*;

public class Compiler3 {
    // Token types
    enum TokenType {
        KEYWORD, IDENTIFIER, OPERATOR, SYMBOL, NUMBER, INVALID
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

    // Valid keywords
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "BEGIN", "INTEGER", "LET", "INPUT", "WRITE", "END"));

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
    private static final Map<String, String> tpToBinary = new HashMap<>();
    static {
        tpToBinary.put("A", "01000001");
        tpToBinary.put("B", "01000010");
        tpToBinary.put("C", "01000011");
        tpToBinary.put("D", "01000100");
        tpToBinary.put("E", "01000101");
        tpToBinary.put("F", "01000110");
        tpToBinary.put("G", "01000111");
        tpToBinary.put("H", "01001000");
        tpToBinary.put("I", "01001001");
        tpToBinary.put("J", "01001010");
        tpToBinary.put("K", "01001011");
        tpToBinary.put("L", "01001100");
        tpToBinary.put("M", "01001101");
        tpToBinary.put("N", "01001110");
        tpToBinary.put("O", "01001111");
        tpToBinary.put("P", "01010000");
        tpToBinary.put("Q", "01010001");
        tpToBinary.put("R", "01010010");
        tpToBinary.put("S", "01010011");
        tpToBinary.put("T", "01010100");
        tpToBinary.put("U", "01010101");
        tpToBinary.put("V", "01010110");
        tpToBinary.put("W", "01010111");
        tpToBinary.put("X", "01011000");
        tpToBinary.put("Y", "01011001");
        tpToBinary.put("Z", "01011010");
        tpToBinary.put("a", "01100001");
        tpToBinary.put("b", "01100010");
        tpToBinary.put("c", "01100011");
        tpToBinary.put("d", "01100100");
        tpToBinary.put("e", "01100101");
        tpToBinary.put("f", "01100110");
        tpToBinary.put("g", "01100111");
        tpToBinary.put("h", "01101000");
        tpToBinary.put("i", "01101001");
        tpToBinary.put("j", "01101010");
        tpToBinary.put("k", "01101011");
        tpToBinary.put("l", "01101100");
        tpToBinary.put("m", "01101101");
        tpToBinary.put("n", "01101110");
        tpToBinary.put("o", "01101111");
        tpToBinary.put("p", "01110000");
        tpToBinary.put("q", "01110001");
        tpToBinary.put("r", "01110010");
        tpToBinary.put("s", "01110011");
        tpToBinary.put("t", "01110100");
        tpToBinary.put("u", "01110101");
        tpToBinary.put("v", "01110110");
        tpToBinary.put("w", "01110111");
        tpToBinary.put("x", "01111000");
        tpToBinary.put("y", "01111001");
        tpToBinary.put("z", "01111010");
    }

    // Declared identifiers
    private static Set<String> declaredIds = new HashSet<>();

    // Lexical Analysis
    public static List<Token> tokenize(String line) {
        List<Token> tokens = new ArrayList<>();
        Pattern tokenPattern = Pattern.compile(
                "\\b(BEGIN|INTEGER|LET|INPUT|WRITE|END)\\b|" +
                        "\\b([a-zA-Z]+)\\b|" +
                        "([+\\-*/])|" +
                        "([=,)])|" +
                        "(\\d+)|" +
                        "([;%$&<>])|" +
                        "(\\s+)|" +
                        "([^\\s])");
        Matcher matcher = tokenPattern.matcher(line);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(new Token(TokenType.KEYWORD, matcher.group(1)));
            } else if (matcher.group(2) != null) {
                tokens.add(new Token(TokenType.IDENTIFIER, matcher.group(2)));
            } else if (matcher.group(3) != null) {
                tokens.add(new Token(TokenType.OPERATOR, matcher.group(3)));
            } else if (matcher.group(4) != null) {
                tokens.add(new Token(TokenType.SYMBOL, matcher.group(4)));
            } else if (matcher.group(5) != null) {
                tokens.add(new Token(TokenType.NUMBER, matcher.group(5)));
            } else if (matcher.group(6) != null) {
                tokens.add(new Token(TokenType.INVALID, matcher.group(6)));
            } else if (matcher.group(8) != null) {
                tokens.add(new Token(TokenType.INVALID, matcher.group(8)));
            }
        }
        return tokens;
    }

    // Check for lexical errors
    public static String checkLexicalErrors(List<Token> tokens) {
        for (Token token : tokens) {
            if (token.type == TokenType.KEYWORD && !KEYWORDS.contains(token.value)) {
                return "Lexical error: Misspelled keyword '" + token.value + "'";
            }
        }
        return null;
    }

    // Syntax Analysis
    public static String checkSyntaxErrors(List<Token> tokens) {
        // Check for numbers
        for (Token token : tokens) {
            if (token.type == TokenType.NUMBER) {
                return "Syntax error: Numbers not allowed ('" + token.value + "')";
            }
        }

        // Check for invalid characters
        for (Token token : tokens) {
            if (token.type == TokenType.INVALID && !token.value.equals(";")) {
                return "Syntax error: Invalid character '" + token.value + "'";
            }
        }

        // Check for combined operators
        for (int i = 0; i < tokens.size() - 1; i++) {
            if (tokens.get(i).type == TokenType.OPERATOR && tokens.get(i + 1).type == TokenType.OPERATOR) {
                return "Syntax error: Combined operators '" + tokens.get(i).value + tokens.get(i + 1).value + "'";
            }
        }

        // Check for semicolon at line end
        if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).value.equals(";")) {
            return "Syntax error: Semicolon not allowed at line end";
        }

        // Line-specific syntax checks
        if (tokens.isEmpty()) {
            return "Syntax error: Empty line";
        }

        // BEGIN, END
        if (tokens.size() == 1 && tokens.get(0).type == TokenType.KEYWORD &&
                (tokens.get(0).value.equals("BEGIN") || tokens.get(0).value.equals("END"))) {
            return null;
        }

        // INTEGER A, B, C
        if (tokens.get(0).type == TokenType.KEYWORD && tokens.get(0).value.equals("INTEGER")) {
            for (int i = 1; i < tokens.size(); i += 2) {
                if (tokens.get(i).type != TokenType.IDENTIFIER) {
                    return "Syntax error: Expected identifier after INTEGER";
                }
                if (i + 1 < tokens.size() && tokens.get(i + 1).type != TokenType.SYMBOL) {
                    return "Syntax error: Expected ',' or end after identifier";
                }
            }
            return null;
        }

        // INPUT A, B, C
        if (tokens.get(0).type == TokenType.KEYWORD && tokens.get(0).value.equals("INPUT")) {
            for (int i = 1; i < tokens.size(); i += 2) {
                if (tokens.get(i).type != TokenType.IDENTIFIER) {
                    return "Syntax error: Expected identifier after INPUT";
                }
                if (i + 1 < tokens.size() && tokens.get(i + 1).type != TokenType.SYMBOL) {
                    return "Syntax error: Expected ',' or end after identifier";
                }
            }
            return null;
        }

        // WRITE ID
        if (tokens.get(0).type == TokenType.KEYWORD && tokens.get(0).value.equals("WRITE")) {
            if (tokens.size() != 2 || tokens.get(1).type != TokenType.IDENTIFIER) {
                return "Syntax error: WRITE expects one identifier";
            }
            return null;
        }

        // LET ID = E or ID = E
        if ((tokens.get(0).type == TokenType.KEYWORD && tokens.get(0).value.equals("LET") &&
                tokens.get(1).type == TokenType.IDENTIFIER && tokens.get(2).type == TokenType.SYMBOL &&
                tokens.get(2).value.equals("=")) ||
                (tokens.get(0).type == TokenType.IDENTIFIER && tokens.get(1).type == TokenType.SYMBOL &&
                        tokens.get(1).value.equals("="))) {
            int exprStart = tokens.get(0).value.equals("LET") ? 3 : 2;
            if (exprStart >= tokens.size()) {
                return "Syntax error: Expected expression after '='";
            }
            // Validate expression: ID (OP ID)*
            for (int i = exprStart; i < tokens.size(); i++) {
                if (i % 2 == exprStart % 2) {
                    if (tokens.get(i).type != TokenType.IDENTIFIER) {
                        return "Syntax error: Expected identifier in expression";
                    }
                } else {
                    if (tokens.get(i).type != TokenType.OPERATOR) {
                        return "Syntax error: Expected operator in expression";
                    }
                }
            }
            return null;
        }

        return "Syntax error: Invalid line structure";
    }

    // Semantic Analysis
    public static String checkSemanticErrors(List<Token> tokens) {
        // Check for invalid symbols
        for (Token token : tokens) {
            if (token.value.matches("[%$&<>]")) {
                return "Semantic error: Invalid symbol '" + token.value + "'";
            }
        }

        // Check identifiers for INPUT, WRITE, and expressions
        if (tokens.get(0).type == TokenType.KEYWORD && tokens.get(0).value.equals("INPUT")) {
            for (int i = 1; i < tokens.size(); i += 2) {
                if (!declaredIds.contains(tokens.get(i).value)) {
                    return "Semantic error: Undeclared identifier '" + tokens.get(i).value + "'";
                }
            }
        } else if (tokens.get(0).type == TokenType.KEYWORD && tokens.get(0).value.equals("WRITE")) {
            if (!declaredIds.contains(tokens.get(1).value)) {
                return "Semantic error: Undeclared identifier '" + tokens.get(1).value + "'";
            }
        } else if ((tokens.get(0).type == TokenType.KEYWORD && tokens.get(0).value.equals("LET")) ||
                tokens.get(0).type == TokenType.IDENTIFIER) {
            String targetId = tokens.get(0).value.equals("LET") ? tokens.get(1).value : tokens.get(0).value;
            if (!declaredIds.contains(targetId)) {
                return "Semantic error: Undeclared identifier '" + targetId + "'";
            }
            int exprStart = tokens.get(0).value.equals("LET") ? 3 : 2;
            for (int i = exprStart; i < tokens.size(); i += 2) {
                if (tokens.get(i).type == TokenType.IDENTIFIER && !declaredIds.contains(tokens.get(i).value)) {
                    return "Semantic error: Undeclared identifier '" + tokens.get(i).value + "'";
                }
            }
        }

        return null;
    }

    // To Postfix
    public static List<String> toPostfix(List<Token> tokens, int exprStart) {
        List<String> output = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();
        for (int i = exprStart; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.IDENTIFIER) {
                output.add(token.value);
            } else if (token.type == TokenType.OPERATOR) {
                while (!operatorStack.isEmpty()
                        && precedence.getOrDefault(operatorStack.peek(), 0) >= precedence.get(token.value)) {
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

    // Generate ICR
    public static List<String> generateICR(List<String> postfix) {
        List<String> icr = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        int tempCount = 1;
        for (String token : postfix) {
            if (token.matches("[a-zA-Z]")) {
                stack.push(token);
            } else {
                String op2 = stack.pop();
                String op1 = stack.pop();
                String temp = "t" + tempCount;
                icr.add(temp + " = " + op1 + " " + token + " " + op2);
                stack.push(temp);
                tempCount++;
            }
        }
        return icr;
    }

    // Generate Assembly
    public static List<String> generateAssembly(List<String> icr, String target) {
        List<String> assembly = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\w+)\\s*([+\\-*/])\\s*(\\w+)");
        for (int i = 0; i < icr.size(); i++) {
            String instruction = icr.get(i);
            String[] parts = instruction.split("=");
            if (parts.length != 2)
                continue;
            String temp = parts[0].trim();
            String expression = parts[1].trim();
            Matcher matcher = pattern.matcher(expression);
            if (matcher.find()) {
                String op1 = matcher.group(1);
                String operator = matcher.group(2);
                String op2 = matcher.group(3);
                String storeDest = (i == icr.size() - 1) ? target : temp; // new
                switch (operator) {
                    case "+":
                        assembly.add("LDA " + op1);
                        assembly.add("ADD " + op2);
                        assembly.add("STR " + storeDest); // temp to storeDest
                        break;
                    case "-":
                        assembly.add("LDA " + op1);
                        assembly.add("SUB " + op2);
                        assembly.add("STR " + storeDest); // temp to storeDest
                        break;
                    case "*":
                        assembly.add("LDA " + op1);
                        assembly.add("MUL " + op2);
                        assembly.add("STR " + storeDest); // temp to storeDest
                        break;
                    case "/":
                        assembly.add("LDA " + op1);
                        assembly.add("DIV " + op2);
                        assembly.add("STR " + storeDest); // temp to storeDest
                        break;
                }
            }
        }
        return assembly;
    }

    // Optimize Assembly
    public static List<String> optimizeAssembly(List<String> assembly) {
        List<String> optimized = new ArrayList<>();
        for (int i = 0; i < assembly.size(); i += 3) {
            if (i + 2 >= assembly.size())
                break;
            String instr1 = assembly.get(i);
            String instr2 = assembly.get(i + 1);
            String instr3 = assembly.get(i + 2);
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

    // Generate TMC
    public static List<String> generateTMC(List<String> optimized) {
        List<String> tmc = new ArrayList<>();
        for (String instruction : optimized) {
            String[] parts = instruction.split("[ ,]+");
            if (parts.length != 4)
                continue;
            String operation = parts[0];
            String dest = parts[1];
            String op1 = parts[2];
            String op2 = parts[3];

            String opBinary = opToBinary.getOrDefault(operation, "00000000");
            String destBinary = tpToBinary.getOrDefault(dest, "01110100");
            String op1Binary = tpToBinary.getOrDefault(op1, "01110100");
            String op2Binary = tpToBinary.getOrDefault(op2, "01110100");

            String line = opBinary + "  " + destBinary + "  " + op1Binary + "  " + op2Binary;
            tmc.add(line);
        }
        return tmc;
    }

    public static void main(String[] args) {
        String[] program = {
                "BEGIN",
                "INTEGER A, B, C, E, M, N, G, H, I, a, c",
                "INPUT A, B, C",
                "LET B = A * / M",
                "LET G = a + c",
                "temp = <s %* * h - j / w + d + * $&;",
                "M = A / B + C",
                "N = G / H - I + a * B / c",
                "WRITE M",
                "WRITEE F;",
                "END"
        };

        System.out.println("V Compiler all at once ");
        System.out.println("------------------------");

        for (int lineNum = 1; lineNum <= program.length; lineNum++) {
            String line = program[lineNum - 1];
            System.out.printf("\nLine %d: %s\n", lineNum, line);

            // Lexical Analysis
            List<Token> tokens = tokenize(line);
            String lexicalError = checkLexicalErrors(tokens);
            if (lexicalError != null) {
                System.out.println("  " + lexicalError);
                continue;
            }

            // Display tokens concisely
            StringBuilder tokenStr = new StringBuilder();
            for (int i = 0; i < tokens.size(); i++) {
                tokenStr.append(String.format("%s (%s)", tokens.get(i).value, tokens.get(i).type));
                if (i < tokens.size() - 1)
                    tokenStr.append(", ");
            }
            System.out.println("  Tokens: " + tokenStr);

            // Syntax Analysis
            String syntaxError = checkSyntaxErrors(tokens);
            if (syntaxError != null) {
                System.out.println("  " + syntaxError);
                continue;
            }

            // Update declared identifiers
            if (tokens.get(0).type == TokenType.KEYWORD && tokens.get(0).value.equals("INTEGER")) {
                for (int i = 1; i < tokens.size(); i += 2) {
                    declaredIds.add(tokens.get(i).value);
                }
            }

            // Semantic Analysis
            String semanticError = checkSemanticErrors(tokens);
            if (semanticError != null) {
                System.out.println("  " + semanticError);
                continue;
            }

            System.out.println("  Status: Valid");

            // Check if it's an expression line and generate code
            if ((tokens.get(0).type == TokenType.KEYWORD && tokens.get(0).value.equals("LET")) ||
                    (tokens.get(0).type == TokenType.IDENTIFIER && tokens.get(1).type == TokenType.SYMBOL && tokens.get(1).value.equals("="))) {
                String target = tokens.get(0).value.equals("LET") ? tokens.get(1).value : tokens.get(0).value;
                int exprStart = tokens.get(0).value.equals("LET") ? 3 : 2;
                List<String> postfix = toPostfix(tokens, exprStart);
                List<String> icr = generateICR(postfix);
                System.out.println("  Postfix: " + postfix);
                System.out.println("  ICR:");
                for (String instr : icr) {
                    System.out.println("    " + instr);
                }

                // Code Generation
                List<String> assembly = generateAssembly(icr, target);
                System.out.println("  Assembly:");
                for (String instr : assembly) {
                    System.out.println("    " + instr);
                }

                // Code Optimization
                List<String> optimized = optimizeAssembly(assembly);
                System.out.println("  Optimized Assembly:");
                for (String instr : optimized) {
                    System.out.println("    " + instr);
                }

                // Target Machine Code
                List<String> tmc = generateTMC(optimized);
                System.out.println("  TMC:");
                for (String code : tmc) {
                    System.out.println("    " + code);
                }
            }
        }

        System.out.println("\nCompilation Complete");
    }
}