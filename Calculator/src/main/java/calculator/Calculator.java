package calculator;

import exception.DivisionByZeroRuntimeException;
import exception.WrongExpressionFormatException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * В классе Calculator есть метод evaluate, кторый входящий параметр String парсит в набор токенов.
 *
 * Затем идет проверка, валидны ли эти токены.
 *
 * Если токены валидны, идет вызов метода fmt (формат).
 *
 * Метод calcIteratively - цикл работает пока не найдется токен с левой скобкой, потом выход из цикла.
 * Если находится правая скобка, то считается что выделяется часть выражения в скобках
 * и дальше рекурсивно выражение обрабатывается (есть ли там еще скобки? вычисляю то, что внутри вложенных скобок).
 * И таким образом высчитывается количественное выражение - рекурсивно в глубь просматривает участки
 * выражений выделенные в скобки и считает их.
 *
 * Далее (строка 143) идет проверка наличия операций с более высоким приоритетом.
 * В итоге, передается лист токенов, а на выходе получается лист с одним токеном, это и есть финальное выражение
 * т.е. рекурсивно между собой токены складываются, вычитаются, обрабатывается то, что в скобках,
 * и на выходе остается лист с одним токеном.*/

public class Calculator {

    public String evaluate(String statement) {
        try {
            List<Token> tokens = parseTokens(statement);
            if (!areValid(tokens)) {
                return null;
            }
            return fmt(calcIteratively(tokens).get(0).getNumberValue());
        } catch (NumberFormatException nfe) {
            System.out.println("Wrong number format");
            return null;
        } catch (DivisionByZeroRuntimeException dbze) {
            System.out.println("Division by zero");
            return null;
        } catch (WrongExpressionFormatException wefe) {
            System.out.println("Wrong format");
            return null;
        } catch (Exception e) {
            System.out.println("Error");
            return null;
        }
    }
    static String fmt(Double value) {
        if (value == null) {
            return null;
        }
        double d = value;
        return d == (long) d ? String.format("%d", (long) d) : String.format("%s", d);
    }

    private static boolean areValid(List<Token> tokens) {

        if (tokens.get(0).isOperator() || tokens.get(tokens.size() - 1).isOperator()) {
            throw new WrongExpressionFormatException();
        }

        int counter = 0;
        Token prev = null;
        for (int idx = 0; idx < tokens.size(); idx++) {
            if (prev != null) {
                if (prev.isOperator() && tokens.get(idx).isOperator() || prev.getType() == TokenType.NUMBER && tokens.get(idx).getType() == TokenType.NUMBER) {
                    throw new WrongExpressionFormatException();
                }
            }
            if (tokens.get(idx).getType() == TokenType.LEFT_BRACKET) {
                counter++;
            } else if (tokens.get(idx).getType() == TokenType.RIGHT_BRACKET) {
                counter--;
            }
            if (counter < 0) {
                throw new WrongExpressionFormatException();
            }
            prev = tokens.get(idx);
        }

        if (counter != 0) {
            throw new WrongExpressionFormatException();
        }

        return true;
    }

    private static List<Token> parseTokens(String expression) {
        String[] strTokens = format(expression).split(" ");
        List<Token> tokens = Arrays.stream(strTokens).filter(e -> !e.equals("")).map(e -> Token.valueOf(e)).collect(Collectors.toList());
        return tokens;
    }

    private static String format(String expression) {
        return expression.replace("(", " ( ").replace(")", " ) ")
                .replace("+", " + ").replace("-", " - ")
                .replace("*", " * ").replace("/", " / ");
    }

    private static List<Token> calcIteratively(List<Token> tokens) {
        System.out.println("Calculating " + tokens);
        while (tokens.stream().anyMatch(e -> e.getType() == TokenType.LEFT_BRACKET)) {

            List<Token> bracketsContent = new ArrayList<>();
            boolean including = false;
            int startIdx = -1;
            int endIdx = -1;
            for (int idx = 0; idx < tokens.size(); idx++) {

                Token token = tokens.get(idx);
                if (!including && token.getType() == TokenType.LEFT_BRACKET) {
                    including = true;
                    startIdx = idx;
                } else if (including && token.getType() == TokenType.LEFT_BRACKET) {
                    bracketsContent.clear();
                    startIdx = idx;
                } else if (including && token.getType() == TokenType.RIGHT_BRACKET) {

                    including = false;
                    endIdx = idx;
                    System.out.println("Brackets content: " + bracketsContent);
                    List<Token> abbreviated = calcIteratively(bracketsContent);
                    System.out.println("Abbreviated: " + abbreviated);

                    final int removeIdx = startIdx;
                    IntStream.range(startIdx, endIdx + 1).forEach(e -> tokens.remove(removeIdx));

                    tokens.addAll(startIdx, abbreviated);
                    System.out.println("After abbreviation: " + tokens);
                } else if (including) {
                    bracketsContent.add(token);
                }
            }
        }

        while (tokens.size() > 2) {

            int leftOperandIdx = 0;
            for (int idx = 0; idx < tokens.size(); idx++) {
                if (tokens.get(idx).isPrio()) {
                    leftOperandIdx = idx - 1;
                    break;
                }
            }

            Double left = tokens.get(leftOperandIdx).getNumberValue();
            Double right = tokens.get(leftOperandIdx + 2).getNumberValue();
            Double res = null;
            switch (tokens.get(leftOperandIdx + 1).getType()) {
                case ADDITION:
                    res = left + right;
                    break;
                case SUBTRACTION:
                    res = left - right;
                    break;
                case DIVISION:
                    if (right == 0) {
                        throw new DivisionByZeroRuntimeException();
                    }
                    res = left / right;
                    break;
                case MULTIPLICATION:
                    res = left * right;
                    break;
            }

            tokens.remove(tokens.get(leftOperandIdx + 2));
            tokens.remove(tokens.get(leftOperandIdx + 1));
            tokens.remove(tokens.get(leftOperandIdx));
            tokens.add(leftOperandIdx, new Token(TokenType.NUMBER, res));
            System.out.println("Intermediate result: " + tokens);
        }

        return tokens;
    }
}