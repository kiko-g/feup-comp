# JMM Compiler

## Project Purpose
To build a compiler for programs written in the [Mini-Java language](https://cs.fit.edu/~ryan/cse4251/mini_java_grammar.html). 

## Project Requirements
* [javacc](https://git.fe.up.pt/compilers2021/comp2021-1a/-/tree/javacc)
* [gradle](https://gradle.org/install/)

## Compile
To compile the program, run ``gradle build``. This will compile your classes to ``classes/main/java`` and copy the JAR file to the root directory. The JAR file will have the same name as the repository folder.

### Run

To run you have two options: Run the ``.class`` files or run the JAR.

### Run ``.class``

To run the ``.class`` files, do the following:

```cmd
java -cp "./build/classes/java/main/" <class_name> <arguments>
```

Where ``<class_name>`` is the name of the class you want to run and ``<arguments>`` are the arguments to be passed to ``main()``.

### Run ``.jar``

To run the JAR, do the following command:

```cmd
java -jar <jar filename> <arguments>
```

Where ``<jar filename>`` is the name of the JAR file that has been copied to the root folder, and ``<arguments>`` are the arguments to be passed to ``main()``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).
You can also see a test report by opening ``build/reports/tests/test/index.html``.

# Checkpoints
1. [Checkpoint 1](https://git.fe.up.pt/compilers2021/comp2021-1a/-/wikis/Checkpoint-1)
2. [Checkpoint 2](#)
3. [Checkpoint 3](#)
