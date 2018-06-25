package com.simplecompiler.interpreter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sad
 */
public class TestPrintFunction {

    private List<Object> printedValues = new ArrayList<Object>();
    private boolean doActualPrint;

    public TestPrintFunction() {
        doActualPrint = false;
    }

    public TestPrintFunction(boolean doActualPrint) {
        this.doActualPrint = doActualPrint;
    }

    @NativeFunction(name = "print")
    public void printAndSave(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            if (doActualPrint) {
                if (i > 0) {
                    System.out.print(" ");
                }

                System.out.print(args[i]);
            }

            printedValues.add(args[i]);
        }

        if (doActualPrint) {
            System.out.println();
        }
    }

    public List getPrintedValues() {
        return printedValues;
    }

    public void clear() {
        printedValues.clear();
    }
}
