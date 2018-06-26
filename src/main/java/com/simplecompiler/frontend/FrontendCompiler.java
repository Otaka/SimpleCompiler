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
            if (!compiledAst.isEmpty()) {
                processedSource.append(compiledAst).append("\n");
            }
        }

        if (!parsingContext.getGlobalVariables().isEmpty()) {
            StringBuilder initBlock = new StringBuilder();
            initBlock.append("$global-var-count ").append(parsingContext.getGlobalVariables().size()).append("\n");
            initBlock.append("label INIT_BLOCK\n");
            initBlock.append("enter 0 0\n");
            for (GlobalVar globalVariable : parsingContext.getGlobalVariables()) {
                initBlock.append(globalVariable.getByteCode());
            }
            initBlock.append("leave 0 0").append("\n");
            initBlock.append("\n");
            processedSource.insert(0, initBlock);
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
                    int argumentIndex = parsingContext.getCurrentFunctionArgNames().size() - i - 1;//arguments should be in backward order
                    //int argumentIndex=i;//arguments should be in backward order
                    return "load-arg " + argumentIndex + "\n";
                }
            }
            throw new IllegalArgumentException("Cannot find argument [" + atom + "]");
        } else if (isLocalVar(atom, parsingContext)) {
            for (int i = 0; i < parsingContext.getCurrentFunctionLocalVariables().size(); i++) {
                String localVariable = parsingContext.getCurrentFunctionLocalVariables().get(i);
                if (atom.equals(localVariable)) {
                    return "load-local-var " + i + "\n";
                }
            }
            throw new IllegalArgumentException("Cannot find argument [" + atom + "]");
        } else if (isGlobalVar(atom, parsingContext)) {
            return "load-global-var " + getGlobalVariableIndex(atom, parsingContext) + "\n";
        } else {
            throw new IllegalStateException("Unknown symbol [" + atom + "]");
        }
    }

    private String processList(List list, ParsingContext parsingContext) {
        if (!(list.get(0) instanceof String)) {
            throw new IllegalArgumentException("Expression should start from operator atom but is started from other list. [" + astToString(list) + "]");
        }

        String operator = (String) list.get(0);
        switch (operator) {
            case "for":
                return processForLoop(list, parsingContext);
            case "fun":
                return processFun(list, parsingContext);
            case "var":
                return processVar(list, parsingContext);
            case "if":
                return processIf(list, parsingContext);
            case "@":
            case "progn":
                return processProgn(list, operator, parsingContext);
            case "+":
            case "-":
            case "/":
            case "*":
            case "=":
            case "!=":
            case "<":
            case ">":
            case "and":
            case "or":
            case "bitand":
            case "bitor":
            case "bitxor":
            case "<<":
            case ">>":
            case ">>>":
                return processMathAndLogicExpression(operator, list, parsingContext);
            case "not":
                return processNotExpression(list, parsingContext);
            default:
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
        result += trueExpressionCompiled + "branch " + labelB + "\n" + "label " + labelA + "\n" + falseExpressionCompiled + "label " + labelB + "\n";
        return result;
    }

    private String processForLoop(List args, ParsingContext parsingContext) {
        if (args.size() < 5) {
            throw new IllegalArgumentException("[FOR] expression should be in following format [FOR INIT CONDITION INC OTHER_EXPRESSIONS...], but found [" + astToString(args) + "]");
        }

        String initExpression = processAst(args.get(1), parsingContext);
        String conditionExpression = processAst(args.get(2), parsingContext);
        String incExpression = processAst(args.get(3), parsingContext);
        String labelEnd = parsingContext.getNextLabel("loop_end");
        String labelAgain = parsingContext.getNextLabel("loop_again_");

        StringBuilder bodyExpression = new StringBuilder();
        for (int i = 4; i < args.size(); i++) {
            bodyExpression.append(processAst(args.get(i), parsingContext));
        }

        StringBuilder result = new StringBuilder();
        result.append(initExpression);
        result.append("label ").append(labelAgain).append("\n");
        result.append(conditionExpression);
//        result.append("not\n");
        result.append("branch-false ").append(labelEnd).append("\n");
        result.append(bodyExpression);
        result.append(incExpression);
        result.append("branch ").append(labelAgain).append("\n");
        result.append("label ").append(labelEnd).append("\n");
        return result.toString();
    }

    private String processProgn(List args, String command, ParsingContext parsingContext) {
        if (args.size() < 2) {
            throw new IllegalArgumentException("[" + command + "] expression should be in following format [" + command + " OTHER_EXPRESSIONS...], but found [" + astToString(args) + "]");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.size(); i++) {
            sb.append(processAst(args.get(i), parsingContext));
        }
        return sb.toString();
    }

    private String processFun(List args, ParsingContext parsingContext) {
        parsingContext.exitFromFunction();
        String functionLabel = (String) args.get(1);
        parsingContext.setCurrentFunctionName(functionLabel);
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
        result += "enter " + parsingContext.getCurrentFunctionLocalVariables().size() + " " + parsingContext.getCurrentFunctionArgNames().size() + "\n";
        result += bodyExpression;
        result += "leave " + parsingContext.getCurrentFunctionLocalVariables().size() + " " + parsingContext.getCurrentFunctionArgNames().size() + "\n";
        parsingContext.setCurrentFunctionName(null);
        return result;
    }

    private String processVar(List args, ParsingContext parsingContext) {
        String varName = (String) args.get(1);
        Object initExpression = args.get(2);
        String initExpressionCompiled = processAst(initExpression, parsingContext);
        if (parsingContext.getCurrentFunctionName() != null && isArg(varName, parsingContext)) {
            throw new IllegalArgumentException("Cannot change argument [" + varName + "] in function [" + parsingContext.getCurrentFunctionName() + "]");
        }

        if (parsingContext.getCurrentFunctionName() == null) {
            //global var
            if (getGlobalVariableIndex(varName, parsingContext) != -1) {
                throw new IllegalArgumentException("Global variable with name [" + varName + "] already exists");
            }
            int globalVarIndex = parsingContext.getGlobalVariables().size();
            String globalVarBytecode = initExpressionCompiled + "store-global-var " + globalVarIndex + "\n";
            GlobalVar globalVariable = new GlobalVar(varName, globalVarBytecode);
            parsingContext.getGlobalVariables().add(globalVariable);
            return "";
        } else {
            //local var
            int varIndex = getLocalVarIndex(varName, parsingContext);
            if (varIndex == -1) {
                parsingContext.getCurrentFunctionLocalVariables().add(varName);
                varIndex = getLocalVarIndex(varName, parsingContext);
            }
            return initExpressionCompiled + "store-local-var " + varIndex + "\n";
        }
    }

    private int getGlobalVariableIndex(String variableName, ParsingContext parsingContext) {
        for (int i = 0; i < parsingContext.getGlobalVariables().size(); i++) {
            if (variableName.equals(parsingContext.getGlobalVariables().get(i).getGlobalVariableName())) {
                return i;
            }
        }
        return -1;
    }

    private String processNotExpression(List args, ParsingContext parsingContext) {
        Object arg1 = args.get(1);
        String compiledArg1 = processAst(arg1, parsingContext);
        return compiledArg1 + "not\n";
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
            case "!=":
                result += "neq";
                break;
            case "and":
                result += "and";
                break;
            case "or":
                result += "or";
                break;
            case "bitand":
                result += "bitand";
                break;
            case "bitor":
                result += "bitor";
                break;
            case "bitxor":
                result += "bitxor";
                break;
            case "<<":
                result += "lshift";
                break;
            case ">>":
                result += "rshift";
                break;
            case ">>>":
                result += "urshift";
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

    private boolean isGlobalVar(String varName, ParsingContext parsingContext) {
        return getGlobalVariableIndex(varName, parsingContext) != -1;
    }

    private boolean isLocalVar(String varName, ParsingContext parsingContext) {
        for (String localVariable : parsingContext.getCurrentFunctionLocalVariables()) {
            if (varName.equals(localVariable)) {
                return true;
            }
        }

        return false;
    }

    private int getLocalVarIndex(String varName, ParsingContext parsingContext) {
        for (int i = 0; i < parsingContext.getCurrentFunctionLocalVariables().size(); i++) {
            String localVariable = parsingContext.getCurrentFunctionLocalVariables().get(i);
            if (varName.equals(localVariable)) {
                return i;
            }
        }

        return -1;
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
