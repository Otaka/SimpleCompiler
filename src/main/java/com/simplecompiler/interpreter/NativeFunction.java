package com.simplecompiler.interpreter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author sad
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeFunction {

    public String name();
}
