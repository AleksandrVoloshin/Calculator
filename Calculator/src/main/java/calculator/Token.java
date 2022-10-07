package calculator;

/**
* Класс Token - задает специальные точки/отметки для определения последовательности
* алгоритма синтаксического анализа.
*/

public class Token {

    private TokenType type;
    private Double numberValue;

    public Token(TokenType type) {
        this.type = type;
        this.numberValue = null;
    }

    public Token(TokenType type, Double numberValue) {
        this.type = type;
        this.numberValue = numberValue;
    }

    public Token(TokenType type, String numberValue) {
        this.type = type;
        this.numberValue = Double.valueOf(numberValue);
    }

    public static Token valueOf(String str) {
        System.out.println("Parsing token '" + str + "'");
        switch (str) {
            case "(":
                return new Token(TokenType.LEFT_BRACKET);
            case ")":
                return new Token(TokenType.RIGHT_BRACKET);
            case "+":
                return new Token(TokenType.ADDITION);
            case "-":
                return new Token(TokenType.SUBTRACTION);
            case "/":
                return new Token(TokenType.DIVISION);
            case "*":
                return new Token(TokenType.MULTIPLICATION);
            default:
                return new Token(TokenType.NUMBER, str);
        }
    }

    public boolean isPrio() {
        return type == TokenType.MULTIPLICATION || type == TokenType.DIVISION;
    }

    boolean isBracket() {
        return type == TokenType.LEFT_BRACKET || type == TokenType.RIGHT_BRACKET;
    }

    boolean isOperator() {
        return type == TokenType.ADDITION || type == TokenType.SUBTRACTION || type == TokenType.DIVISION ||
                type == TokenType.MULTIPLICATION;
    }

    @Override
    public String toString() {
        if (type == TokenType.NUMBER) {
            return numberValue.toString();
        } else {
            return type.toString();
        }
    }

    public Double getNumberValue() {
        return numberValue;
    }

    public TokenType getType() {
        return type;
    }
}