package com.simplecompiler.frontend;

import java.io.IOException;
import java.util.List;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * @author Dmitry
 */
public class FrontendCompiler {

    public String compile(String source, String fileName) throws IOException {
        Grammar grammar = Parboiled.createParser(Grammar.class);
        BasicParseRunner parseRunner = new BasicParseRunner(grammar.start());
        ParsingResult result = parseRunner.run(source);
        if (!result.matched) {
            throw new IllegalArgumentException("Error while parsing source file [" + fileName + "]");
        }

        List resultValue = (List) result.resultValue;
        ParsingContext parsingContext = new ParsingContext();
        parsingContext.setFileName(fileName);
        parsingContext.setSource(source);
        StringBuilder processedSource = new StringBuilder();
        for (Object ast : resultValue) {
            String compiledAst = processAst(ast, parsingContext);
            processedSource.append(compiledAst).append("\n");
        }

        return processedSource.toString();
    }

    private String processAst(Object ast, ParsingContext parsingContext) {
        if (ast instanceof String) {
            return processAtom((String) ast, parsingContext);
        } else if (ast instanceof List) {
            return processList((List) ast, parsingContext);
        } else {
            throw new IllegalArgumentException("Unknow object type of ast [" + ast.getClass().getSimpleName() + "]");
        }
    }

    private String processAtom(String atom, ParsingContext parsingContext) {
        if (isLong(atom)) {
            return "load-long " + atom + "\n";
        } else if (isArg(atom, parsingContext)) {
            for (int i = 0; i < parsingContext.getCurrentFunctionArgNames().size(); i++) {
                String parameterName = parsingContext.getCurrentFunctionArgNames().get(i);
                if (atom.equals(parameterName)) {
                    return "load-arg " + i + "\n";
                }
            }
            throw new IllegalArgumentException("Cannot find argument [" + atom + "]");
        } else {
            return "load-var " + atom + "\n";
        }
    }

    private String processList(List list, ParsingContext parsingContext) {
        if (!(list.get(0) instanceof String)) {
            throw new IllegalArgumentException("Expression should start from operator atom but is started from other list. [" + astToString(list) + "]");
        }

        String operator = (String) list.get(0);
        if (operator.equals("fun")) {
            return processFun(list, parsingContext);
        } else if (operator.equals("var")) {
            return processDefineExpression(list, parsingContext);
        } else if (operator.equals("if")) {
            return processIf(list, parsingContext);
        } else if (operator.equals("+") || operator.equals("-") || operator.equals("/") || operator.equals("*") || operator.equals("=") || operator.equals("<") || operator.equals(">")) {
            return processMathAndLogicExpression(operator, list, parsingContext);
        } else {
            return processFunctionCall(list, parsingContext);
        }
    }

    private String processFunctionCall(List args, ParsingContext parsingContext) {
        String functionName = (String) args.get(0);
        int argsCount = args.size() - 1;
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < args.size(); i++) {
            result.append(processAst(args.get(i), parsingContext)).append("save\n");
        }

        //result.append(processAst(functionName, parsingContext));
        result.append("call ").append(functionName).append(" ").append(argsCount).append("\n");
        return result.toString();
    }

    private String processIf(List args, ParsingContext parsingContext) {
        if (args.size() != 4) {
            throw new IllegalArgumentException("[IF] expression should be in following format [IF CONDITION TRUE_EXPRESSION FALSE_EXPRESSION], but found [" + astToString(args) + "]");
        }

        String conditionExpressionCompiled = processAst(args.get(1), parsingContext);
        String trueExpressionCompiled = processAst(args.get(2), parsingContext);
        String falseExpressionCompiled = processAst(args.get(3), parsingContext);
        String labelA = parsingContext.getNextLabel("condition_");
        String labelB = parsingContext.getNextLabel("condition_");
        String result = conditionExpressionCompiled + "branch-false " + labelA + "\n";
        result += trueExpressionCompiled + "branch " + labelB + "\n" + "label " + labelA + "\n" + falseExpressionCompiled + "\n" + "label " + labelB + "\n";
        return result;
    }

    private String processFun(List args, ParsingContext parsingContext) {
        parsingContext.clearArgNames();
        String functionLabel = (String) args.get(1);
        List paramsList = (List) args.get(2);
        for (Object param : paramsList) {
            if (!(param instanceof String)) {
                throw new IllegalArgumentException("Parameter name of function should be string, but found [" + astToString(param) + "]");
            }

            registerArgName((String) param, parsingContext);
        }

        StringBuilder bodyExpression = new StringBuilder();
        for (int i = 3; i < args.size(); i++) {
            String compiledAst = processAst(args.get(i), parsingContext);
            bodyExpression.append(compiledAst);
        }

        String result = "label " + functionLabel + "\n";
        result += ("enter\n" + bodyExpression + "leave\n");
        return result;
    }

    private String processDefineExpression(List args, ParsingContext parsingContext) {
        String varName = (String) args.get(1);
        Object initExpression = args.get(2);
        String initExpressionCompiled = processAst(initExpression, parsingContext);
        return "long " + varName + "\n" + initExpressionCompiled + "store-var " + varName + "\n";
    }

    private String processMathAndLogicExpression(String command, List args, ParsingContext parsingContext) {
        Object arg1 = args.get(1);
        Object arg2 = args.get(2);
        String compiledArg1 = processAst(arg1, parsingContext);
        String compiledArg2 = processAst(arg2, parsingContext);
        String result = compiledArg2 + "save\n" + compiledArg1;
        switch (command) {
            case "+":
                result += "add";
                break;
            case "-":
                result += "sub";
                break;
            case "*":
                result += "mul";
                break;
            case "/":
                result += "div";
                break;
            case "<":
                result += "less";
                break;
            case ">":
                result += "more";
                break;
            case "=":
                result += "eq";
                break;
            default:
                break;
        }
        result += "\n";
        return result;
    }

    private boolean isArg(String varName, ParsingContext parsingContext) {
        for (String argName : parsingContext.getCurrentFunctionArgNames()) {
            if (varName.equals(argName)) {
                return true;
            }
        }

        return false;
    }

    private void registerArgName(String varName, ParsingContext parsingContext) {
        if (isArg(varName, parsingContext)) {
            throw new IllegalArgumentException("Argument [" + varName + "] already exists");
        }

        parsingContext.getCurrentFunctionArgNames().add(varName);
    }

    private boolean isLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String astToString(Object ast) {
        if (ast instanceof List) {
            StringBuilder sb = new StringBuilder();
            for (Object obj : (List) ast) {
                if (obj instanceof List) {
                    sb.append("EXPRS ");
                } else {
                    sb.append(obj).append(" ");
                }
            }

            return sb.toString().trim();
        } else {
            return ast.toString();
        }
    }
}
