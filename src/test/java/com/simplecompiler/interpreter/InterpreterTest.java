package com.simplecompiler.interpreter;

import com.simplecompiler.BaseTest;
import java.io.IOException;
import java.util.List;
import org.hamcrest.CoreMatchers;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Dmitry
 */
public class InterpreterTest extends BaseTest {

    @Test
    public void testNativeFunctionInvocation() throws IOException {
        assertThat(executeScript("/com/simplecompiler/testsources/printTwoValues.scs"), CoreMatchers.hasItems(1, 2, 3, 4));
    }

    @Test
    public void testArithmExpression() throws IOException {
        assertThat(executeScript("/com/simplecompiler/testsources/printMathResultOfTwoValues.scs"), CoreMatchers.hasItems(19, 18, 36, 5));
    }

    @Test
    public void testIf() throws IOException {
        assertThat(executeScript("/com/simplecompiler/testsources/testIf.scs"), CoreMatchers.hasItems(54, 75,8,9));
    }

    @Test
    public void testFunctionDefinitionAndCall() throws IOException {
        assertThat(executeScript("/com/simplecompiler/testsources/defineFunctionAndCallIt.scs"), CoreMatchers.hasItems(48, 3));
    }

    private List<Integer> executeScript(String path) {
        String bytecode = compileFileFromResource(path);
        Interpreter interpreter = new Interpreter();
        TestPrintFunction functionHolder = new TestPrintFunction();
        interpreter.getNativeFunctionManager().registerFunctions(functionHolder);
        interpreter.execute(bytecode);
        return functionHolder.getPrintedValues();
    }

}
