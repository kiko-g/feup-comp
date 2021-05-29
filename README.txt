**GROUP: 1A

Names, numbers, self assessment, and contribution of the members of the group to the project:
NAME1: Francisco Gonçalves, NR1: 201704790, GRADE1: ?, CONTRIBUTION1: ?
NAME2: Luís Ramos, NR2: up201706253, GRADE2: ?, CONTRIBUTION2: ?
NAME3: Martim Silva, NR3: up201705205, GRADE3: ?, CONTRIBUTION3: ?
TODO

 
GLOBAL Grade of the project: TODO 18



** SUMMARY: (Describe what your tool does and its main features.)

 



**DEALING WITH SYNTACTIC ERRORS: (Describe how the syntactic error recovery of your tool works. Does it exit after the first error?)
TODO
 


**SEMANTIC ANALYSIS: (Refer the semantic rules implemented by your tool.)
1. Build Analysis Table (symbol table)
2. Type Analysis
3. Initialization Analysis

### Build Analysis Table
Reports are added when there are:
- Duplicated imports
- Redeclarations of variables
- Redeclarations of methods
- Redeclarations of function parameters

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

### Initialization Analysis
Reports are added when:
- Variables were not initialized


**CODE GENERATION: (describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)

 


**TASK DISTRIBUTION: (Identify the set of tasks done by each member of the project. You can divide this by checkpoint it if helps)
TODO
Francisco
- 
Luís Ramos
- 
-

Martim Silva
- 

 


**PROS: (Identify the most positive aspects of your tool)
- Organized implementation
- Function overloading
- Meaningful error/warning reports at the semantic level
- Optimizations implemented (-o)
- Register allocation (graph coloring)



**CONS:

