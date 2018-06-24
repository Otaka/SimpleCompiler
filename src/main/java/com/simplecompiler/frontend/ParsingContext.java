package com.simplecompiler.frontend;

import com.simplecompiler.utils.StringBuilderWithPadding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry
 */
public class ParsingContext {

    private String source;
    private String fileName;
    private StringBuilderWithPadding byteCodeSource = new StringBuilderWithPadding("  ");
    private List<String> currentFunctionArgNames = new ArrayList<>();
    private int currentIndex;
    private Map<String, String> functions = new HashMap<String, String>();

    public Map<String, String> getFunctions() {
        return functions;
    }

    public String getNextLabel(String typePrefix) {
        return typePrefix + fileName.replace(".", "_") + "_" + (currentIndex++) + "";
    }

    public StringBuilderWithPadding getByteCodeSource() {
        return byteCodeSource;
    }

    public List<String> getCurrentFunctionArgNames() {
        return currentFunctionArgNames;
    }

    public void clearArgNames() {
        currentFunctionArgNames.clear();
    }

    public String getFileName() {
        return fileName;
    }

    public String getSource() {
        return source;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setSource(String source) {
        this.source = source;
    }

}
