package com.example.accounting.domain.model.auto_journal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("PMD.AvoidThrowStatement")
public final class AmountFormulaEvaluator {

    private static final Pattern SIMPLE_VAR = Pattern.compile("^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*$");
    private static final Pattern VAR_OP_NUM = Pattern.compile(
            "^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*([+\\-*/])\\s*([0-9]+(\\.[0-9]+)?)\\s*$");

    private AmountFormulaEvaluator() {
    }

    public static BigDecimal evaluate(String formula, Map<String, BigDecimal> params) {
        Objects.requireNonNull(formula, "数式は必須です");
        Objects.requireNonNull(params, "パラメータは必須です");

        Matcher simpleMatcher = SIMPLE_VAR.matcher(formula);
        if (simpleMatcher.matches()) {
            return resolveVariable(simpleMatcher.group(1), params);
        }

        Matcher opMatcher = VAR_OP_NUM.matcher(formula);
        if (opMatcher.matches()) {
            return evaluateOperation(opMatcher, params);
        }

        throw new IllegalArgumentException("サポートされていない数式フォーマット: " + formula);
    }

    private static BigDecimal resolveVariable(String varName, Map<String, BigDecimal> params) {
        BigDecimal value = params.get(varName);
        if (value == null) {
            throw new IllegalArgumentException("パラメータ '" + varName + "' が見つかりません");
        }
        return value;
    }

    private static BigDecimal evaluateOperation(Matcher opMatcher, Map<String, BigDecimal> params) {
        String varName = opMatcher.group(1);
        String operator = opMatcher.group(2);
        BigDecimal constant = new BigDecimal(opMatcher.group(3));
        BigDecimal varValue = resolveVariable(varName, params);

        return applyOperator(varValue, operator, constant);
    }

    private static BigDecimal applyOperator(BigDecimal varValue, String operator, BigDecimal constant) {
        return switch (operator) {
            case "+" -> varValue.add(constant);
            case "-" -> varValue.subtract(constant);
            case "*" -> varValue.multiply(constant).setScale(0, RoundingMode.HALF_UP);
            case "/" -> divideValues(varValue, constant);
            default -> throw new IllegalArgumentException("サポートされていない演算子: " + operator);
        };
    }

    private static BigDecimal divideValues(BigDecimal varValue, BigDecimal constant) {
        if (constant.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("ゼロ除算は許可されていません");
        }
        return varValue.divide(constant, 0, RoundingMode.HALF_UP);
    }
}
