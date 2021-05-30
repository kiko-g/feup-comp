**GROUP: 1A 

TODO: Definir autoavaliação e contribuição
Names, numbers, self assessment, and contribution of the members of the group to the project:
NAME1: Francisco Gonçalves, NR1: 201704790, GRADE1: ?, CONTRIBUTION1: ?
NAME2: Luís Ramos, NR2: up201706253, GRADE2: ?, CONTRIBUTION2: ?
NAME3: Martim Silva, NR3: up201705205, GRADE3: ?, CONTRIBUTION3: ?

GLOBAL Grade of the project: 18



**SUMMARY: (Describe what your tool does and its main features.)
- The tool compiles .jmm files written in Java-- which is a simpler version of Java. 
- The tool receives the .jmm files and goes through 4 main steps before generating the final output:
    1. The program parses the file by performing a syntatic analysis and generates a JSON file with a tree that represents the Java-- class
    2. With the parser result the compiler performs a semantic analysis
    3. The result of the previous operation is now going to be converted into a Low Level Intermediate Representation
    4. After that and using the LLIR result the compiler generates files with JVM instructions accepted by jasmin
- Finally, Jasmin translates the last output into Java bytecodes (.class files).




**DEALING WITH SYNTACTIC ERRORS: (Describe how the syntactic error recovery of your tool works. Does it exit after the first error?)
TODO: Descrever fase sintática e geração do JSON
 


**SEMANTIC ANALYSIS: (Refer the semantic rules implemented by your tool.)
1. Build Analysis Table (symbol table)
2. Type Analysis
3. Initialization Analysis

Scenarios covered with reports:
- Duplicated imports
- Redeclarations of variables
- Redeclarations of methods
- Redeclarations of function parameters
- Missing imports
- Invalid Types used
- Accessing length of a non array
- A method could not be found
- Invalid parameters to method
- Types don't match
- Array assignment to a non array variable
- Array initialization with a Type different than int
- Variable not initialized
- Variables were not initialized



**CODE GENERATION: (describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)
Code Generation Features
- Class
- Fields
- Methods
- Instructions
- Conditionals (`if` and `if-else`)
- Loops (`while`)
- Arrays
  - Array initialization (`newarray int`)
  - Array Store (`astore`)
  - Array Access (`aload`)
  - Array Position Store (`iastore`)
  - Array Position Access (`iaload`)
- Limits (`.limit stack` and `.limit locals`)

Code Generation Instruction Selection
- `iconst_`, `bipush`, `sipush`, `ldc`, for pushing integer to the stack with the lowest cost.
- `iinc` for incrementing/decrementing local variables by a constant value.
- `ishl`, `ishr` for using shifts with multiplications/division with a power of 2 number.
- `ineg` for subtracting a variable to 0.
- `iflt`, `iflt`, `ifge`, `ifgt`, `ifeq`, `ifneq` for if statements comparing with 0

Extra features
- [ ] Declaration of objects with constructor and parameters
- [ ] Use do-while template when possible
- [x] Variables with keyword names: array, i32, ret, bool, field, method and void
- [x] Variables starting with $
- [x] Checks if a variable is initialized
- [x] Functions overload
- [x] Pop instructions to avoid the accumulation of stack size

Custom Tests
- Person (**Test1**)
- Factorial (**Test2**)
- Calendar (**Test3**)
- Shapes (**Test4**)
- ShapesExtra (**Test5**)

 


**TASK DISTRIBUTION: (Identify the set of tasks done by each member of the project. You can divide this by checkpoint it if helps)
TODO: Distribuir tasks
Francisco
- 

Luís Ramos
- 

Martim Silva
- 

 


**PROS: (Identify the most positive aspects of your tool)
- Function overloading
- Meaningful error/warning reports at the semantic level
- Optimizations implemented (-o)
- Register allocation (graph coloring)
- Organized implementation


**CONS:

