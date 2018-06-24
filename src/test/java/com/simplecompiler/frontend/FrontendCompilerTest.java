package com.simplecompiler.frontend;

import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class FrontendCompilerTest {

    @Test
    public void testCompile() throws Exception {
        FrontendCompiler frontEndCompiler = new FrontendCompiler();
        String scriptString = IOUtils.resourceToString("/com/simplecompiler/testsources/defineFunctionAndCallIt.scs", StandardCharsets.UTF_8);
        String result = frontEndCompiler.compile(scriptString, "defineFunctionAndCallIt.scs");
        String expectedBytecodeString = IOUtils.resourceToString("/com/simplecompiler/testsources/defineFunctionAndCallIt_bytecode.txt", StandardCharsets.UTF_8);
        Assert.assertEquals(
                expectedBytecodeString.trim(),
                result.trim()
        );
    }
}
