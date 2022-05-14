SDI1800056 Καλαμάκης Αντώνης


This project was initialized by the minijava example provided as material in the tutorial lectures.

For Main.java, I added the logic to accept multiple files and execute the visitors one by one, printing the offsets and
a success message. In case of a file with a semantic error simply catch the exception, print a failure message and
continue to the next file.

For this implementation the logic is based on 2 visitors extending the GJDepthFirst visitor. First visitor fills 
the symbol table and the second uses it.

For the implementation of the symbol table, we have 3 HashMaps. 1 map for the fields, 1 for the functions and one for the classes.

For the fields, basically here we have a map pointing into a hashmap. The field identifier points into a hashmap that receives the scope
and finally returns the type of the identifier. In this is stored every field for every class variable, function and its return type and
every variable inside a function. For the purpose of this project since a variable can be named the same with a function this hashmap returns a list of strings.
This list is always size of two. Index 0 represents the variables and index 1 represents the functions.

For the functions hashmap, we basically have an identifier pointing into a hashmap that receives as key a scope and returns the types of the arguments 
of a function as an ArrayList of Strings.

Finally, the classes hashmap receives a class identifier and returns a string with all the parent classes if they exist.
(For this implementation we represent the scope like this:

for a variable i inside class's A, foo function: A::foo::i, so if a class C extends class B that extends A, we store it like this in the classes hashmap,
"A::B")

We use 5 functions to "communicate" with the symbol table for inserting a field, lookup, ge the parent class, find the parent class or check if the field we are checking belongs to the parent class.

In this visitor we also use a linked hash map to store the fields and their offsets. At every symbol table insertion we also store extra information in the offsets map.
Based on the class's name we push a offsetItem object into the pair and initialize its members with the identifier, the field's current offset and the size of that field.

2 functions were created for the offsets hashmap, one for inserting a new item and one for printing the map with a specific format.

For the both first and second visitor the argu argument for each visit function is mostly used as a scope in case a later visit method needs the current scope of the item it visits.

The second visitor also extends the GjDepthFirst visitor and when initialized, gets the first visitor as a parameter.

Since we now have a symbol table we only need to semantic check our input. Simply the visit methods, when needed, are designed
to finally return the type of the item they are referring to. Also at this step its important to note that the program checks if a used identifier exists,
since its not the first's visitor job to check. 

In this visitor most visit functions return a type so the callers can compare and see if its the desired type or not. 

We use functions like isSubClass or hasSameParametersWithSuper to determine if the extended class is correctly extending the parent class and for some functions just for convenience like type Exists to check if a
class identifier exists or not(and also checks if the type is simple types like int or an array of booleans etc).

Since for the scope of this project there is no need to print all the error messages, in case of using a non-existant identifier the program throws a null exception and stops the semantic check.

In case of some other kind of error the program prints the appropriate message as a thrown exception message error and throws the exception.

As said before it is designed to work with multiple files and continue to the next one in case one file throws any kind of exception.