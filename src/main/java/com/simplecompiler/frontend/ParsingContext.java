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
    private List<String> currentFunctionLocalVariables = new ArrayList<>();
    private Map<String, String> functions = new HashMap<String, String>();
    private List<GlobalVar> globalVariables = new ArrayList<>();
    private int currentIndexToGenerateIds;
    private String currentFunctionName;

    public List<GlobalVar> getGlobalVariables() {
        return globalVariables;
    }

    public void setCurrentFunctionName(String currentFunctionName) {
        this.currentFunctionName = currentFunctionName;
    }

    public String getCurrentFunctionName() {
        return currentFunctionName;
    }

    public Map<String, String> getFunctions() {
        return functions;
    }

    public String getNextLabel(String typePrefix) {
        return typePrefix + fileName.replace(".", "_") + "_" + (currentIndexToGenerateIds++) + "";
    }

    public StringBuilderWithPadding getByteCodeSource() {
        return byteCodeSource;
    }

    public List<String> getCurrentFunctionArgNames() {
        return currentFunctionArgNames;
    }

    public List<String> getCurrentFunctionLocalVariables() {
        return currentFunctionLocalVariables;
    }

    public void exitFromFunction() {
        currentFunctionArgNames.clear();
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
