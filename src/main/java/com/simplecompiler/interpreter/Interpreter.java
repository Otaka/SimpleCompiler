package com.simplecompiler.interpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry
 */
public class Interpreter {

    public void execute(String bytecodeString) {
        Map<String, Integer> labelIndexes = new HashMap<>();
        String[] bytecode = bytecodeString.split("\n", -1);
        fillLabelsAndTrim(bytecode, labelIndexes);
        ExecutionState executionState = new ExecutionState();
        if (!labelIndexes.containsKey("main")) {
            throw new IllegalArgumentException("Bytecode does not contain 'main' function");
        }

        executionState.ip = labelIndexes.get("main");
        while (true) {
            String line = bytecode[executionState.ip];
            executionState.ip++;
            if (line.startsWith("label")) {
                continue;
            }

            if (line.startsWith("load-long")) {
                int value = Integer.parseInt(line.substring("load-long".length()).trim());
                executionState.accumulator = value;
                continue;
            }
            if (line.startsWith("long")) {
                String varName = line.substring("long".length()).trim();
                executionState.variables.put(varName, 0);
                continue;
            }
            if (line.startsWith("load-var")) {
                String varName = line.substring("load-var".length()).trim();
                if (!executionState.variables.containsKey(varName)) {
                    throw new IllegalArgumentException("Cannot find variable [" + varName + "]");
                }

                executionState.accumulator = executionState.variables.get(varName);
                continue;
            }
            if (line.startsWith("store-var")) {
                String varName = line.substring("store-var".length()).trim();
                if (!executionState.variables.containsKey(varName)) {
                    throw new IllegalArgumentException("Cannot find variable [" + varName + "]");
                }

                executionState.variables.put(varName, executionState.accumulator);
                continue;
            }
            if (line.startsWith("load-arg")) {
                int argIndex = Integer.parseInt(line.substring("load-arg".length()).trim());
                executionState.accumulator = executionState.argsStack.get(executionState.argsStack.size()-1)[argIndex];
                continue;
            }
            if (line.startsWith("save")) {
                
                executionState.stack[executionState.stackIndex] = executionState.accumulator;
                executionState.stackIndex++;
                continue;
            }
            if (line.startsWith("add")) {
                executionState.accumulator += executionState.stack[executionState.stackIndex];
                executionState.stackIndex--;
                continue;
            }
            if (line.startsWith("sub")) {
                executionState.accumulator -= executionState.stack[executionState.stackIndex];
                executionState.stackIndex--;
                continue;
            }
            if (line.startsWith("less")) {
                executionState.accumulator = executionState.accumulator < executionState.stack[executionState.stackIndex] ? 1 : 0;
                executionState.stackIndex--;
                continue;
            }
            if (line.startsWith("eq")) {
                executionState.accumulator = executionState.accumulator == executionState.stack[executionState.stackIndex] ? 1 : 0;
                executionState.stackIndex--;
                continue;
            }
            if (line.startsWith("more")) {
                executionState.accumulator = executionState.accumulator > executionState.stack[executionState.stackIndex] ? 1 : 0;
                executionState.stackIndex--;
                continue;
            }
            if (line.startsWith("branch")) {
                String label = line.substring("branch".length()).trim();
                if (!labelIndexes.containsKey(label)) {
                    throw new IllegalArgumentException("Cannot find label [" + label + "]");
                }
                int destIndex = labelIndexes.get(label);
                executionState.ip = destIndex;
                continue;
            }
            if (line.startsWith("branch-false")) {
                if (executionState.accumulator == 0) {
                    String label = line.substring("branch-false".length()).trim();
                    if (!labelIndexes.containsKey(label)) {
                        throw new IllegalArgumentException("Cannot find label [" + label + "]");
                    }
                    int destIndex = labelIndexes.get(label);
                    executionState.ip = destIndex;
                }
                continue;
            }
            if (line.startsWith("leave")) {
                executionState.ip = executionState.returnAddresses.remove(executionState.returnAddresses.size() - 1);
                executionState.argsStack.remove(executionState.argsStack.size() - 1);
                continue;
            }
            if (line.startsWith("call")) {
                String[] parts = line.split(" ", -1);
                String functionName = parts[1];
                int argsCount = Integer.parseInt(parts[2]);
                if (labelIndexes.containsKey(functionName)) {
                    int[] args = new int[argsCount];
                    for (int i = 0; i < argsCount; i++) {
                        args[i] = executionState.stack[executionState.stackIndex - i-1];
                    }

                    executionState.stackIndex -= argsCount;
                    executionState.argsStack.add(args);
                    executionState.returnAddresses.add(executionState.ip);
                    executionState.ip=labelIndexes.get(functionName);
                } else {
                    executeInternalFunction(executionState, functionName, argsCount);
                }
                continue;
            }
        }
    }

    private void executeInternalFunction(ExecutionState executionState, String functionName, int argsCount) {
        if (functionName.equals("print")) {
            for (int i = 0; i < argsCount; i++) {
                if (i > 0) {
                    System.out.print(" ");
                }

                System.out.print(executionState.stack[executionState.stackIndex - i]);
            }
            executionState.stackIndex -= argsCount;
            return;
        }
        
        throw new IllegalArgumentException("Cannot find function [" + functionName + "]");
    }

    private void fillLabelsAndTrim(String[] bytecode, Map<String, Integer> labelIndexes) {
        for (int i = 0; i < bytecode.length; i++) {
            String line = bytecode[i].trim();
            bytecode[i] = line;
            if (line.startsWith("label")) {
                String label = line.substring("label".length()).trim();
                if (labelIndexes.containsKey(label)) {
                    throw new IllegalArgumentException("duplicate label [" + label + "] on line [" + i + "] and also on line [" + labelIndexes.get(label) + "]");
                }

                labelIndexes.put(label, i + 1);
            }
        }
    }
}