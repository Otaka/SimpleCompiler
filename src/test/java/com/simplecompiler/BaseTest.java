package com.simplecompiler;

import com.simplecompiler.frontend.FrontendCompiler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

/**
 * @author sad
 */
public class BaseTest {

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
