package com.simplecompiler.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry
 */
public class ExecutionState {
    public int accumulator;
    public int[]stack=new int[10000];
    public int stackIndex=0;
    public int ip=0;
    public Map<String,Integer>variables=new HashMap<>();
    public List<int[]>argsStack=new ArrayList<>();
    public List<Integer>returnAddresses=new ArrayList<>();
    
    public ExecutionState() {
    }
    
}
