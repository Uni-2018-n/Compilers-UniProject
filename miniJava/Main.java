import syntaxtree.*;
import visitor.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 1){
            System.err.println("Usage: java Main <inputFile>");
            System.exit(1);
        }



        FileInputStream fis = null;
        try{
            fis = new FileInputStream(args[0]);
            MiniJavaParser parser = new MiniJavaParser(fis);

            Goal root = parser.Goal();

            System.err.println("Program parsed successfully.");

            firstVisitor eval = new firstVisitor();
            root.accept(eval, null);
            for (String i : eval.fields.keySet()) {
                System.out.print(i+"->{");
                for(String j : eval.fields.get(i).keySet()){
                    System.out.print(j+"("+eval.fields.get(i).get(j)+"), ");
                }
                System.out.println("}");
            }
            System.out.println("Functions:");
            for (String i : eval.functions.keySet()) {
                System.out.print(i+"->{");
                for(String j : eval.functions.get(i).keySet()){
                    System.out.print(j+"("+eval.functions.get(i).get(j)+"), ");
                }
                System.out.println("}");
            }
            System.out.println("First part DONE:");


            secondVisitor secVis = new secondVisitor(eval);
            root.accept(secVis, null);
        }
        catch(ParseException ex){
            System.out.println(ex.getMessage());
        }
        catch(FileNotFoundException ex){
            System.err.println(ex.getMessage());
        }
        catch(Exception ex){
            System.err.println(ex.getMessage());
            System.err.println("failed for "+args[0]);
        }
        finally{
            try{
                if(fis != null) fis.close();
            }
            catch(IOException ex){
                System.err.println(ex.getMessage());
            }
        }
    }
}
