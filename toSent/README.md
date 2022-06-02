SDI1800056 Καλαμάκης Αντώνης


This project is a followup from the second project. Created a new visitor (same with all others) to write llvm code into a .ll file.

The logic behind is exactly the same with before. Only diffrence now is that i also use the third visitor with the data provided from the symbol table of the first visitor(Since the second visitor was only to type check and throw exceptions if needed we dont really need it for this part.)

For creating unique registers i have a global counter that is set to 0 at the begining of each method.
For unique labels i have a global counter that is initialized at the initialization of the third visitor only(since all labels must be unique).

For Arrays i have n+1 length, with the first item set to the length of the array. 
For that reason i used, for both boolean and integer arrays, i32 type to make it a bit easier to work with. 