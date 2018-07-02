package com.simplecompiler.interpreter;

import com.simplecompiler.BaseTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Dmitry
 */
public class InterpreterTest extends BaseTest {

    @Test
    @Ignore
    public void testRecursionFibonacchi() throws IOException {
        System.out.println("Fibonnachi call. Performance test. Can run for a very long time.");
        JOptionPane.showMessageDialog(null, "Started fibonacchi test");
        long startTime = System.currentTimeMillis();
        List result = executeScript("/com/simplecompiler/testsources/fibCalc.scs");
        long endTime = System.currentTimeMillis();
        String message = "Total time = " + (endTime - startTime) + "ms. Result = " + result;
        System.out.println(message);
        JOptionPane.showMessageDialog(null, message);
    }

    @Test
    public void testNumberBasis() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/numberBasis.scs"), Arrays.asList(26, 3));
    }

    @Test
    public void testNativeFunctionInvocation() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/printTwoValues.scs"), Arrays.asList(1, 2, 3, 4));
    }

    @Test
    public void testArithmExpression() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/printMathResultOfTwoValues.scs"), Arrays.asList(19, 18, 36, 5));
    }

    @Test
    public void testIf() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/testIf.scs"), Arrays.asList(54, 75, 8, 9));
    }

    @Test
    public void testIfMultiline() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/testIfMultiline.scs"), Arrays.asList(1, 2, 3, 4, 5));
    }

    @Test
    public void testFunctionDefinitionAndCall() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/defineFunctionAndCallIt.scs"), Arrays.asList(2, 3));
    }

    @Test
    public void testBitwiseOperations() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/testBitwiseOperations.scs"), Arrays.asList(36, 183, 147));
    }

    @Test
    public void testShiftOperations() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/testShiftOperations.scs"), Arrays.asList(32, 22, 22));
    }

    @Test
    public void testLogicAndEquality() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/testLogicAndEquality.scs"),
                Arrays.asList(
                        // =
                        1, 0,
                        // !=
                        1, 0,
                        // not
                        0, 1,
                        // and
                        1, 0, 0, 0,
                        // or
                        1, 1, 1, 0
                )
        );
    }

    @Test
    public void testFunctionChain() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/functionChain.scs"), Arrays.asList(15, 105, 1005, 1005, 105, 15));
    }

    @Test
    public void testRecursion() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/recursion.scs"), Arrays.asList(1, 0, 56));
    }

    @Test
    public void testSingleVariable() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/simpleVariable.scs"), Arrays.asList(2));
    }

    @Test
    public void testVariablesInFunctions() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/testVariablesInFunctions.scs"), Arrays.asList(8, 4, 2, 8, 8, 4));
    }

    @Test
    public void testGlobalVariables() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/testGlobalVariables.scs"), Arrays.asList(55, 60, 75, 43, 49, 30, 50, 55, 60, 75, 55, 60, 75, 43, 49));
    }

    @Test
    public void testForLoop() throws IOException {
        checkEqual(executeScript("/com/simplecompiler/testsources/testForLoop.scs"), Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
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
