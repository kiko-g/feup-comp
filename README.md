# JMM Compiler

## Group 1A

| Name                | Student Number |
| ------------------- | -------------- |
| Francisco Gonçalves | up201704790    |
| Luís Ramos          | up201706253    |
| Martim Silva        | up201705205    |

## Purpose

To build a compiler for programs written in the [Mini-Java language](https://cs.fit.edu/~ryan/cse4251/mini_java_grammar.html).

---

## Project Requirements

- [javacc](https://git.fe.up.pt/compilers2021/comp2021-1a/-/tree/javacc)
- [gradle](https://gradle.org/install/)

## Compile

To compile the program, run `gradle build`. This will compile your classes to `classes/main/java` and copy the JAR file to the root directory. The JAR file will have the same name as the repository folder.

### Run

To run you have two options: Run the `.class` files or run the JAR.

### Run `.class`

To run the `.class` files, do the following:

```cmd
java -cp "./build/classes/java/main/" <class_name> <arguments>
```

Where `<class_name>` is the name of the class you want to run and `<arguments>` are the arguments to be passed to `main()`.

### Run `.jar`

To run the JAR, do the following command:

```cmd
java -jar <jar filename> <arguments>
```

Where `<jar filename>` is the name of the JAR file that has been copied to the root folder, and `<arguments>` are the arguments to be passed to `main()`.

Program arguments:

- `-r=N` to limit the number of registers (N) available **(optional)**
- `-o` to use the optimizations implemented **(optional)**
- `<filepath>` to test a specific file

## Test

To test the program, run `gradle test`. This will execute the build, and run the JUnit tests in the `test` folder. If you want to see output printed during the tests, use the flag `-i` (i.e., `gradle test -i`).
You can also see a test report by opening `build/reports/tests/test/index.html`.

## Checkpoints

1. [Checkpoint 1](https://git.fe.up.pt/compilers2021/comp2021-1a/-/wikis/Checkpoint-1)
2. [Checkpoint 2](https://git.fe.up.pt/compilers2021/comp2021-1a/-/wikis/Checkpoint-2)
3. [Checkpoint 3](https://git.fe.up.pt/compilers2021/comp2021-1a/-/wikis/Checkpoint-3)

---

## Syntatic Analysis (checkpoint 1)

If the compiler detects an error in a while condition it will stop at the end of the condidtion

TODO

### Checklist

- [x] Global lookahead of 1
- [x] Local lookahead of 2 only used once

---

## Semantic Analysis (checkpoint 2)

### Symbol Table

TODO

### Type Analysis

Reports are added when:

- Import is missing
- Invalid Type is found
- Accessing the length of a non array
- A method could not be found
- Invalid parameters to method
- Types don't match
- Array assignment to a non array variable
- Array initialization with a Type different than int
- Variable not initialized

### OLLIR

TODO

---

## Code generation (checkpoint 3)

### Code Generation Features

- [x] **Class**
- [x] **Fields**
- [x] **Methods**
- [x] **Instructions**
- [x] **Conditionals** (`if` and `if-else`)
- [x] **Loops** (`while`)
- [x] **Arrays**
  - [x] Array initialization (`newarray int`)
  - [x] Array Store (`astore`)
  - [x] Array Access (`aload`)
  - [x] Array Position Store (`iastore`)
  - [x] Array Position Access (`iaload`)
- [x] **Limits** (`.limit stack` and `.limit locals`)

### Code Generation Instruction Selection

- [x] `iconst_`, `bipush`, `sipush`, `ldc`, for pushing integer to the stack with the lowest cost.
- [x] `iinc` for incrementing/decrementing local variables by a constant value.
- [x] `ishl`, `ishr` for using shifts with multiplications/division with a power of 2 number.
- [x] `ineg` for subtracting a variable to 0.
- [x] `iflt`, `iflt`, `ifge`, `ifgt`, `ifeq`, `ifneq` for if statements comparing with 0

### Extra features

- [ ] Declaration of objects with constructor and parameters
- [ ] Use do-while template when possible
- [x] Variables with keyword names: array, i32, ret, bool, field, method and void
- [x] Variables starting with $
- [x] Checks if a variable is initialized
- [x] Functions overload
- [x] Pop instructions to avoid the accumulation of stack size

### Custom Tests

- Person (Test1)
- Factorial (Test2)
- Calendar (Test3)
- Shapes (Test4)
- ShapesExtra (Test5)

---

## Final Delivery

### Optimizations

- Constant Propagation
- Transform while into only one jump

### Register allocation

InterferenceGraphMaker creates the graph and calculates the lifetimes. After that GraphPainter is called and attempts to color the graph with N registers (-r=N). This is done byt using Maps with a Node associated to Sets of Nodes, a stack for the registers and a set for the used colors.

## Pros

- Function overloading
- Meaningful error reports at the syntatic and semantic level
- Optimizations implemented (-o)
- Register allocation (graph coloring)

## Cons

- None because we're perfect TODO

## TODO

- [x] Liveness analysis
- [x] Graph coloring
- [ ] Dead code removal
- [ ] Constant folding
