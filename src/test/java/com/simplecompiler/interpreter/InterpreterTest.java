package com.simplecompiler.interpreter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Dmitry
 */
public class InterpreterTest {
    

    @Test
    public void testExecute() throws IOException {
        Interpreter interpreter=new Interpreter();
        interpreter.execute( IOUtils.resourceToString("/com/simplecompiler/testsources/defineFunctionAndCallIt_bytecode.txt", StandardCharsets.UTF_8));
    }
    
}
