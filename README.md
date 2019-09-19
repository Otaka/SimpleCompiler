# SimpleCompiler
My first simple programming language written in java
This language supports only integer type, and nothing else, that is why it is pretty limited. After I finished this project, I have started new project https://github.com/Otaka/ScriptLang, that has much more capabilities.

Syntax is similar to LISP, just to make parsing more simple:
Here is example:
```
(fun nfibs (n) 
    (if (= n 0) 
        0 
        (if (< n 2) 
            1 
            (+ (nfibs (- n 1)) (nfibs (- n 2))))))

(fun main() 
    (print (nfibs 3))
 )
```

Compiler compiles the code to internal bytecode, that works on very simple stack based virtual machine.
