package com.simplecompiler.frontend;

/**
 * @author sad
 */
public class GlobalVar {

    private String globalVariableName;
    private String byteCode;

    public String getByteCode() {
        return byteCode;
    }

    public String getGlobalVariableName() {
        return globalVariableName;
    }

    public GlobalVar(String globalVariableName, String byteCode) {
        this.globalVariableName = globalVariableName;
        this.byteCode = byteCode;
    }

}
