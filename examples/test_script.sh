#!/bin/bash
for filepath in output_correct/*
do
    filename=$(basename $filepath)
    # echo $filename
    diff output_correct/$filename output_my/$filename
    
done