package com.simplecompiler.interpreter;

import com.simplecompiler.interpreter.NativeFunctionManager.Function;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry
 */
public class Interpreter {

    private NativeFunctionManager nativeFunctionManager = new NativeFunctionManager();

    public void execute(String bytecodeString) {
        Map<String, Integer> labelIndexes = new HashMap<>();
        String[] bytecode = bytecodeString.split("\n", -1);
        List<String> directives = new ArrayList<>();
        fillLabelsExtractDirectivesAndTrim(bytecode, labelIndexes, directives);
        ExecutionState executionState = new ExecutionState();
        processDirectives(directives, executionState);
        if (!labelIndexes.containsKey("main")) {
            throw new IllegalArgumentException("Bytecode does not contain 'main' function");
        }

        if (labelIndexes.containsKey("INIT_BLOCK")) {
            executionState.pushToStack(-1);//return address
            executionState.ip = labelIndexes.get("INIT_BLOCK");
            executeBytecode(executionState, bytecode, labelIndexes);
        }

        executionState.pushToStack(-1);//return address
        executionState.ip = labelIndexes.get("main");
        executeBytecode(executionState, bytecode, labelIndexes);
    }

    private void processDirectives(List<String> directives, ExecutionState executionState) {
        for (String directive : directives) {
            if (directive.startsWith("$global-var-count")) {
                int globalVarCount = extractIntArgumentFromString("$global-var-count", directive);
                executionState.reserveOnStack(globalVarCount);
            }
        }
    }

    private void executeBytecode(ExecutionState executionState, String[] bytecode, Map<String, Integer> labelIndexes) {
        OUTER:
        while (true) {
            String line = bytecode[executionState.ip];
            executionState.ip++;
            if (line.isEmpty()) {
                continue;
            }
            String command;
            int spaceIndex = line.indexOf(' ');
            if (spaceIndex == -1) {
                command = line;
            } else {
                command = line.substring(0, spaceIndex);
            }
            switch (command) {
                case "save":
                    executionState.pushToStack(executionState.accumulator);
                    break;
                case "load-long":
                    int value = Integer.parseInt(line.substring("load-long".length()).trim());
                    executionState.accumulator = value;
                    break;
                case "call":
                    String[] parts = line.split(" ", -1);
                    String functionName = parts[1];
                    int argsCount = Integer.parseInt(parts[2]);
                    if (labelIndexes.containsKey(functionName)) {
                        //executionState.stackIndex -= argsCount;
                        executionState.pushToStack(executionState.ip);
                        //executionState.reserveOnStack(argsCount);
                        executionState.ip = labelIndexes.get(functionName);
                    } else {
                        executeInternalFunction(executionState, functionName, argsCount);
                    }
                    break;
                case "store-local-var": {
                    int varIndex = extractIntArgumentFromString("store-local-var", line);
                    int variableStackOffset = executionState.frameIndex + 1 + varIndex;
                    executionState.stack[variableStackOffset] = executionState.accumulator;
                    break;
                }
                case "load-local-var": {
                    int varIndex = extractIntArgumentFromString("load-local-var", line);
                    int variableStackOffset = executionState.frameIndex + 1 + varIndex;
                    executionState.accumulator = executionState.stack[variableStackOffset];
                    break;
                }
                case "load-global-var": {
                    int varIndex = extractIntArgumentFromString("load-global-var", line);
                    executionState.accumulator = executionState.stack[varIndex];
                    break;
                }
                case "store-global-var": {
                    int varIndex = extractIntArgumentFromString("store-global-var", line);
                    executionState.stack[varIndex] = executionState.accumulator;
                    break;
                }
                case "load-arg": {
                    int argIndex = extractIntArgumentFromString("load-arg", line);
                    int variableStackOffset = executionState.frameIndex - 2 - argIndex;
                    executionState.accumulator = executionState.stack[variableStackOffset];
                    break;
                }
                case "add":
                    executionState.accumulator += executionState.popFromStack();
                    break;
                case "sub":
                    executionState.accumulator -= executionState.popFromStack();
                    break;
                case "mul":
                    executionState.accumulator *= executionState.popFromStack();
                    break;
                case "div":
                    executionState.accumulator /= executionState.popFromStack();
                    break;
                case "less":
                    executionState.accumulator = executionState.accumulator < executionState.popFromStack() ? 1 : 0;
                    break;
                case "not":
                    executionState.accumulator = executionState.accumulator == 0 ? 1 : 0;
                    break;
                case "eq":
                    executionState.accumulator = executionState.accumulator == executionState.popFromStack() ? 1 : 0;
                    break;
                case "neq":
                    executionState.accumulator = executionState.accumulator != executionState.popFromStack() ? 1 : 0;
                    break;
                case "more": {
                    int operand = executionState.popFromStack();
                    executionState.accumulator = executionState.accumulator > operand ? 1 : 0;
                    break;
                }
                case "bitand":
                    executionState.accumulator = executionState.accumulator & executionState.popFromStack();
                    break;
                case "bitor":
                    executionState.accumulator = executionState.accumulator | executionState.popFromStack();
                    break;
                case "bitxor":
                    executionState.accumulator = executionState.accumulator ^ executionState.popFromStack();
                    break;
                case "lshift":
                    executionState.accumulator = executionState.accumulator << executionState.popFromStack();
                    break;
                case "rshift":
                    executionState.accumulator = executionState.accumulator >> executionState.popFromStack();
                    break;
                case "urshift":
                    executionState.accumulator = executionState.accumulator >>> executionState.popFromStack();
                    break;
                case "and": {
                    boolean operand = executionState.popFromStack() != 0;
                    executionState.accumulator = executionState.accumulator != 0 && operand ? 1 : 0;
                    break;
                }
                case "or": {
                    boolean operand = executionState.popFromStack() != 0;
                    executionState.accumulator = executionState.accumulator != 0 || operand ? 1 : 0;
                    break;
                }
                case "branch-false":
                    if (executionState.accumulator == 0) {
                        String label = line.substring("branch-false".length()).trim();
                        if (!labelIndexes.containsKey(label)) {
                            throw new IllegalArgumentException("Cannot find label [" + label + "]");
                        }
                        int destIndex = labelIndexes.get(label);
                        executionState.ip = destIndex;
                    }
                    break;
                case "branch":
                    String label = line.substring("branch".length()).trim();
                    if (!labelIndexes.containsKey(label)) {
                        throw new IllegalArgumentException("Cannot find label [" + label + "]");
                    }
                    int destIndex = labelIndexes.get(label);
                    executionState.ip = destIndex;
                    break;
                case "enter": {
                    //save frame
                    executionState.pushToStack(executionState.frameIndex);
                    executionState.frameIndex = executionState.stackIndex;
                    //reserve space for local variables
                    int localVarsCount = extractIntArgumentFromString("enter", line);
                    executionState.reserveOnStack(localVarsCount);
                    break;
                }
                case "leave": {
                    int localVarsCount = extractIntArgumentFromString("leave", line);
                    int argumentsCount = extractLastIntArgumentFromString("leave", line);
                    executionState.freeFromStack(localVarsCount);
                    executionState.frameIndex = executionState.popFromStack();
                    executionState.ip = executionState.popFromStack();
                    if (executionState.ip == -1) {
                        break OUTER;
                    }
                    executionState.freeFromStack(argumentsCount);
                    break;
                }
                case "label":
                    break;
                default:
                    throw new IllegalStateException("Unknown bytecode [" + line + "]");
                    
            }
        }
    }

    private int extractIntArgumentFromString(String command, String fullString) {
        String arg = fullString.substring(command.length()).trim();
        if (arg.contains(" ")) {
            arg = arg.substring(0, arg.indexOf(' '));
        }
        return Integer.parseInt(arg);
    }

    private int extractLastIntArgumentFromString(String command, String fullString) {
        String arg = fullString.substring(fullString.lastIndexOf(" ") + 1).trim();
        return Integer.parseInt(arg);
    }

    private Object[] extractArguments(ExecutionState executionState, int argsCount) {
        Object[] args = new Object[argsCount];
        for (int i = 0; i < argsCount; i++) {
            int valueIndex = executionState.stackIndex - i;
            args[argsCount - i - 1] = executionState.stack[valueIndex];
        }
        executionState.stackIndex -= argsCount;
        return args;
    }

    private void executeInternalFunction(ExecutionState executionState, String functionName, int argsCount) {
        Function function = nativeFunctionManager.findFunction(functionName);
        if (function == null) {
            throw new IllegalArgumentException("Cannot find function [" + functionName + "]");
        }
        Object[] extractedArguments = extractArguments(executionState, argsCount);
        Class returnType = function.method.getReturnType();
        Object result;
        try {
            result = function.method.invoke(function.ownerObject, new Object[]{extractedArguments});
        } catch (Exception ex) {
            throw new RuntimeException("Error while execute function [" + functionName + "]", ex);
        }

        if (returnType != void.class) {
            executionState.accumulator = (Integer) result;
        }
    }

    private void fillLabelsExtractDirectivesAndTrim(String[] bytecode, Map<String, Integer> labelIndexes, List<String> directivesToCollect) {
        for (int i = 0; i < bytecode.length; i++) {
            String line = bytecode[i].trim();
            bytecode[i] = line;
            if (line.startsWith("$")) {
                directivesToCollect.add(line);
            } else if (line.startsWith("label")) {
                String label = line.substring("label".length()).trim();
                if (labelIndexes.containsKey(label)) {
                    throw new IllegalArgumentException("duplicate label [" + label + "] on line [" + i + "] and also on line [" + labelIndexes.get(label) + "]");
                }

                labelIndexes.put(label, i + 1);
            }
        }
    }

    public NativeFunctionManager getNativeFunctionManager() {
        return nativeFunctionManager;
    }
}
