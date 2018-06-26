package com.simplecompiler.interpreter;

/**
 * @author Dmitry
 */
public class ExecutionState {

    public int accumulator;
    public int[] stack = new int[10000];
    public int stackIndex = -1;
    public int ip = 0;
    public int frameIndex = -1;

    public ExecutionState() {
    }

    public void reserveOnStack(int count) {
        stackIndex += count;
    }

    public void freeFromStack(int count) {
        stackIndex -= count;
    }

    public void pushToStack(int value) {
        stackIndex++;
        stack[stackIndex] = value;
    }

    public int popFromStack() {
        int value = stack[stackIndex];
        stackIndex--;
        return value;
    }

    public int peekFromStack() {
        return stack[stackIndex];
    }
}
