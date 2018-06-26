package com.simplecompiler;

import com.simplecompiler.frontend.FrontendCompiler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.IOUtils;

/**
 * @author sad
 */
public class BaseTest {

    protected void checkEqual(List actual, List expected) {
        if (actual.size() != expected.size()) {
            String listMessage = "";
            if (actual.size() < 20 && expected.size() < 20) {
                listMessage = "\nExpected list:" + expected + "\nActual list  :" + actual;
            }

            throw new AssertionError("Lists sizes are not equal. Expected size " + expected.size() + ", actual size " + actual.size() + listMessage);
        }
        for (int i = 0; i < actual.size(); i++) {
            if (!Objects.equals(actual.get(i), expected.get(i))) {
                String listMessage = "";
                if (actual.size() < 20 && expected.size() < 20) {
                    listMessage = "\nExpected list:" + expected + "\nActual list  :" + actual;
                }

                throw new AssertionError("Lists are not equal. Difference at index " + i + ". Expected [" + expected.get(i) + "], but actual [" + actual.get(i) + "]" + listMessage);
            }
        }
    }

    public String readFileFromResource(String fileName) {
        try {
            String fileContent = IOUtils.resourceToString(fileName, StandardCharsets.UTF_8).trim().replace("\r", "");
            return fileContent;
        } catch (IOException ex) {
            throw new RuntimeException("Error while reading file [" + fileName + "]", ex);
        }
    }

    public String compileFileFromResource(String fileName) {
        try {
            FrontendCompiler compiler = new FrontendCompiler();
            String fileContent = readFileFromResource(fileName);
            return compiler.compile(fileContent, fileName.substring(fileName.lastIndexOf("/") + 1)).trim();
        } catch (IOException ex) {
            throw new RuntimeException("Error while reading file [" + fileName + "]", ex);
        }
    }
}
