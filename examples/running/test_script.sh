#!/bin/bash
for i in *.ll; do
    [ -f "$i" ] || break
    echo $i
    clang -o ${i%%.*}-my $i
    touch ${i%%.*}-my.txt
    ./${i%%.*}-my > ${i%%.*}-my.txt
    touch ${i%%.*}-java.txt
    javac ${i%%.*}.java
    java ${i%%.*} > ${i%%.*}-java.txt
    diff ${i%%.*}-my.txt ${i%%.*}-java.txt
done