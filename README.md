# JMM Compiler

## Group 1A

| Name                | Student Number | Grade | Contribution |
| ------------------- | -------------- | ----- | ------------ |
| Francisco Gonçalves | up201704790    |  20   |     33.3 %   |
| Luís Ramos          | up201706253    |  20   |     33.3 %   |
| Martim Silva        | up201705205    |  20   |     33.3 %   |

Global grade of the project: 20

## Project Requirements

- Java 15
- Javacc
- Gradle

## Compile

To compile the program, run `gradle build`. This will compile your classes to `classes/main/java` and copy the JAR file to the root directory. The JAR file will have the same name as the repository folder.

## Run
To run the JAR in Windows, do the following command:

```cmd
.\comp2021-1a.bat Main [-r=N] [-o] <input_file.jmm>
```

To run the JAR in Linux, do the following command:
```shell
bash comp2021-1a Main [-r=N] [-o] <input_file.jmm>
```

The possible flags that can be used are the following:
- -r=N | Activate the -r optimization, relative to the liveness analysis and register allocation for local variables. Must be a positive integer, equal or higher than 1 (representing the maximum number of registers that each function can use for local variables). In case it's not possible to allocate N registers to local variables, the compiler will create a report.
- -o | Activates the -o optimizations. This optimization performs constant propagation, constant folding and dead code removal.

## Test

To run all tests, enter the following command. All the tests are located in `test/JMMTest.java`

```cmd
gradle test
```

## Summary

- Development of a compiler for .jmm files written in [Java--](https://www.cs.purdue.edu/homes/hosking/502/project/grammar.html), a subset of the Java language.
- The compiler goes through 4 main steps:
  1. The program parses a Java-- class file by performing **Lexical and Syntatic Analysis** and generates a JSON file with its representation.
  2. Performs a **Semantic Analysis** to check any potential semantic errors.
  3. Converts the Java-- class into a **Low Level Intermediate Representation**.
  4. Performs **Code Generation** step using JVM instructions accepted by Jasmin, generating .class files.

## Syntatic Errors

- If the compiler finds a syntatic error inside a while statement it does not halt the execution and is able to recover from it, adding a Report with the error messages. 
- When detecting a syntatic error inside a while statement the compiler ignores every token until the next "{" or the next ")"
- The generated `.json` file (in `/generated/json`) saves the AST if the program doesn't have errors otherwise it will save the list of reports.
- All syntatic error messages include the line, column and expected token. One possible error message in the while statement is the following:
```cmd
ERROR@SYNTATIC, line 4, col 4: Error(1) detected during parsing process. Unexpected token ')' ....
    Error Details:
        Line: 4          Column: 24
    Was expecting:
        "true" | "false" | "this" | "new" | "(" | "!" | <IDENTIFIER> | <INTEGER_LITERAL>
```

## Semantic Analysis

### Main steps

1. Build Analysis Table (Symbol Table)
2. Type Analysis
3. Initialization Analysis

### Scenarios covered with reports

The compiler detects the following semantic errors:

- Duplicated imports
- Redeclarations of variables
- Redeclarations of methods
- Redeclarations of function parameters
- Missing imports
- Invalid Types used
- Accessing length of a non array
- A method not be found
- Invalid parameters to method
- Types don't match
- Array assignment to a non array variable
- Array initialization with a Type different than int
- Variable not initialized
- Variables were not initialized

## Intermediate Representation & Code Generation

### Intermediate Representation
""Liveness analysis, graph coloring and register allocation is done using the OLLIR code.""

We created classes that translate 

### Intermediate Representation  & Code Generation  Features
- [x] Class
- [x] Fields
- [x] Methods
- [x] Instructions
- [x] Conditionals (`if` and `if-else`)
- [x] Loops (`while`)
- [x] Arrays
  - [x] Array initialization (`newarray int`)
  - [x] Array Store (`astore`)
  - [x] Array Access (`aload`)
  - [x] Array Position Store (`iastore`)
  - [x] Array Position Access (`iaload`)
- [x] Limits (`.limit stack` and `.limit locals`)

#### Code Generation Instruction Selection (default)

- [x] `iconst_`, `bipush`, `sipush`, `ldc`, for pushing integer to the stack with the lowest cost.
- [x] `iinc` for incrementing/decrementing local variables by a constant value.
- [x] `ishl`, `ishr` for using shifts with multiplications/division with a power of 2 number.
- [x] `ineg` for subtracting a variable to 0.
- [x] `iflt`, `iflt`, `ifge`, `ifgt`, `ifeq`, `ifneq` for if statements comparing with 0

### Optimizations
All the optimizations are done at the OLLIR level either after the Semantic Analysis or after the generation of the Intermediate Representation.
#### Optimizations (-o)
- [x] Constant propagation
- [x] Constant folding
- [x] Dead code removal (with unnused assignments)

#### Optimizations (default)
- [x] While conditions using do while template

### Extra features
All the optimizations are done at the OLLIR level either after the Semantic Analysis or after the generation of the Intermediate Representation.
- [x] Functions overload
- [x] Variables with keyword names: array, i32, ret, bool, field, method and void
- [x] Variables starting with $
- [x] Checks if a variable is initialized
- [x] Pop instructions to avoid the accumulation of stack size

## Task Distribution
The development of the project was done in a collaborative manner using platforms such as Discord and VSCode live share. There was constant interchanging in tasks and the code many times was implemented in a pair-programming environment and constant discussions about algorithms efficiency, data structures where all members participated.

## Pros
- [x] Function overloading
- [x] Efficient instructions in Jasmin
- [x] Checks if variable is initialized
- [x] Pop instructions to avoid the accumulation of stack size
- [x] Do-while template optimization
- [x] Constant folding optimization
- [x] Constant propagation optimization
- [x] Dead code removal (in certain cases)
- [x] Meaningful error/warning reports
- [x] Register allocation (graph coloring)
- [x] Code structure
- [x] Robustness of the compiler
- [x] Compreenshive tests in [JmmTest class](https://git.fe.up.pt/compilers2021/comp2021-1a/-/blob/master/test/JMMTest.java)  
- [x] Storage of all steps
    - Saves .json file (`/generated/json`) while doing syntatic analysis.
    - Saves .ollir file (`/ollir`) while doing intermediate representation step.
    - Saves .j file in (`/jasmin`) while doing code generation step.

## Cons
- [x] Grammar uses one local lookahead of 2
