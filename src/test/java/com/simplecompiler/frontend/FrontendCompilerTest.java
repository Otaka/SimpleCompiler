package com.simplecompiler.frontend;

import com.simplecompiler.BaseTest;
import org.junit.Assert;
import org.junit.Test;

public class FrontendCompilerTest extends BaseTest {

    @Test
    public void testCompile() throws Exception {
        String result = compileFileFromResource("/com/simplecompiler/testsources/defineFunctionAndCallIt.scs");
        String expectedBytecodeString = readFileFromResource("/com/simplecompiler/testsources/defineFunctionAndCallIt_bytecode.txt");
        Assert.assertEquals(
                expectedBytecodeString,
                result
        );
    }
    
    @Test
    public void testVariablesInFunctionParsing() throws Exception {
        String result = compileFileFromResource("/com/simplecompiler/testsources/testParsingOfSimpleLocalVar.scs");
        String expectedBytecodeString = readFileFromResource("/com/simplecompiler/testsources/testParsingOfSimpleLocalVar_bytecode.txt");
        Assert.assertEquals(
                expectedBytecodeString,
                result
        );
    }
}
