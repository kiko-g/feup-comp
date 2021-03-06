jjtree .\%1.jjt && ^
javacc .\%1.jj && ^
javac *.java && ^
java -cp ./ %1