package com.simplecompiler.utils;

/**
 * @author sad
 */
public class StringBuilderWithPadding {

    private int level = 0;
    private StringBuilder sb = new StringBuilder();
    private boolean newLine = true;
    private final String tabString;
    private int maxLevel = 0;

    public StringBuilderWithPadding() {
        this.tabString = "  ";
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public StringBuilderWithPadding(String tabString) {
        this.tabString = tabString;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public StringBuilderWithPadding incLevel() {
        level++;
        if (maxLevel < level) {
            maxLevel = level;
        }
        return this;
    }

    public StringBuilderWithPadding decLevel() {
        if (level > 0) {
            level--;
        }

        return this;
    }

    public int getLevel() {
        return level;
    }

    private void printOffset() {
        for (int i = 0; i < level; i++) {
            sb.append(tabString);
        }
    }

    private void checkNewLine() {
        if (newLine) {
            newLine = false;
            printOffset();
        }
    }

    public StringBuilderWithPadding append(long value) {
        return print(String.valueOf(value));
    }

    public StringBuilderWithPadding append(String value) {
        return print(value);
    }

    public StringBuilderWithPadding print(String value) {
        String[] values = value.split("\n", -1);
        int lastIndex = values.length - 1;
        for (int i = 0; i < values.length; i++) {
            String val = values[i];
            if (i == lastIndex && val.isEmpty()) {
                break;
            }
            checkNewLine();
            sb.append(val);
            if (i != lastIndex) {
                newLine = true;
                sb.append("\n");
            }
        }

        return this;
    }

    public StringBuilderWithPadding println() {
        return println("");
    }

    public StringBuilderWithPadding printlnWithoutOffset(String value) {
        sb.append(value);
        sb.append("\n");
        newLine = true;
        return this;
    }

    public StringBuilderWithPadding printWithoutOffset(String value) {
        sb.append(value);
        return this;
    }

    public StringBuilderWithPadding println(String value) {
        return print(value + "\n");
    }

    @Override
    public String toString() {
        return sb.toString();
    }

}
