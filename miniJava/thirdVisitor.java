import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import syntaxtree.*;
import visitor.GJDepthFirst;


public class thirdVisitor extends GJDepthFirst<String, String> {
    firstVisitor firstV;
    PrintWriter out;
    public thirdVisitor(String fileName, firstVisitor firVis){
        super();
        try{
            out = new PrintWriter(fileName.substring(0,fileName.lastIndexOf(".java"))+".ll", "UTF-8");
        }catch (IOException e) {
            System.err.println(fileName+": error file creation");
            e.printStackTrace();
        }

        firstV = firVis;
    }


    public String getDeclaration(String id, String className){
        String ret = "";
        String retType = firstV.lookUp(id, className+"::"+id, 1);

        if(retType.equals("int")){
            ret +="i32 ";
        }else if(retType.equals("bool")){
            ret += "i1 ";
        }else if(retType.equals("intArray")){
            ret +="i32* ";
        }else if(retType.equals("boolArray")){
            ret +="i1* ";
        }else{
            ret +="i8* ";
        }
        
        ret += "(";

        ArrayList<String> params = firstV.functions.get(id).get(className);

        ret += "i8*";
        if(!params.isEmpty()){
            for(int i=0;i<params.size();i++){
                if(params.get(i).equals("int")){
                    ret += ",i32";
                }else if(params.get(i).equals("bool")){
                    ret +=",i1";
                }else if(retType.equals("intArray")){
                    ret +="i32*";
                }else if(retType.equals("boolArray")){
                    ret +="i1*";
                }else{
                    ret +=",i8*";
                }
            }
        }
        ret += ")* @"+className+"."+id+" to i8*";
        return ret;
    }


    /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
   public String visit(Goal n, String argu) throws Exception {
    String _ret=null;
    String vTables="";
    for(Map.Entry<String, pair>ite : firstV.offsets.entrySet()){
        vTables += "@."+ite.getKey()+"_vtable = global ["+ite.getValue().functions.size()+" x i8*] [";
        // System.out.println(ite.getKey());
        for(int i=0;i<ite.getValue().functions.size();i++){
            vTables +="i8* bitcast ("+getDeclaration(ite.getValue().functions.get(i).id, ite.getKey())+")";
            if(i+1<ite.getValue().functions.size()){
                vTables +=", ";
            }
        }
        vTables +="]\n";
    }
    vTables +="\n\n";
    // System.out.println(vTables);
    out.write(vTables);


    String initStr = 
    "declare i8* @calloc(i32, i32) \n" +
    "declare i32 @printf(i8*, ...)\n"+
    "declare void @exit(i32)\n"+
    "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n"+
    "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n"+
    "define void @print_int(i32 %i) {\n"+
    "%_str = bitcast [4 x i8]* @_cint to i8*\n"+
    "call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n"+
    "ret void\n"+
    "}\n"+
    "\n"+
    "define void @throw_oob() {\n"+
    "%_str = bitcast [15 x i8]* @_cOOB to i8*\n"+
    "call i32 (i8*, ...) @printf(i8* %_str)\n"+
    "call void @exit(i32 1)\n"+
    "ret void\n"+
    "}\n";
    out.write(initStr);
    n.f0.accept(this, argu);
    n.f1.accept(this, argu);
    n.f2.accept(this, argu);
    out.close();
    return _ret;
 }

    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
    public String visit(MainClass n, String argu) throws Exception {
        String _ret=null;
        String cName = n.f1.accept(this, argu);
        String str = 
        "define i32 @main() {\n";
        out.write(str);
        n.f14.accept(this, argu);
        str = "ret i32 0\n"+
        "}\n";
        out.write(str);
        n.f11.accept(this, argu);
        n.f15.accept(this, argu);
        return _ret;
    }

    /**
        * f0 -> "class"
        * f1 -> Identifier()
        * f2 -> "{"
        * f3 -> ( VarDeclaration() )*
        * f4 -> ( MethodDeclaration() )*
        * f5 -> "}"
    */
    public String visit(ClassDeclaration n, String argu) throws Exception {
        String _ret=null;
        String id = n.f1.accept(this, argu);
        // n.f3.accept(this, argu);
        n.f4.accept(this, id);
        return _ret;
    }

    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        String _ret=null;
        String id = n.f1.accept(this, argu);
        n.f3.accept(this, argu);
        // n.f5.accept(this, argu);
        n.f6.accept(this, id);
        return _ret;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    public String visit(VarDeclaration n, String argu) throws Exception {
        String _ret=null;
        String type = n.f0.accept(this, argu);
        String id = n.f1.accept(this, argu);

        String fin= "%"+id+" = alloca "+type+"\n";
        out.write(fin);

        return _ret;
    }

    /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
    public String visit(MethodDeclaration n, String argu) throws Exception {
        String _ret=null;
        String type = n.f1.accept(this, argu);
        String id = n.f2.accept(this, argu);
        String params = n.f4.accept(this, argu);
        n.f8.accept(this, argu);
        n.f10.accept(this, argu);
        
        String fin = 
        "define "+ type + " @"+argu+"."+id+"(i8* %this";
        if(params != null){
            if(params.indexOf("//") != -1){
                params = params.substring(0, params.indexOf("//"))+") {\n"+params.substring(params.indexOf("//")+2);
            }
            fin += params;
        }else{
            fin += ") {\n";
        }
        out.write(fin);

        n.f7.accept(this, argu);
        fin = "}\n";
        out.write(fin);
        return _ret;
    }

    /**
        * f0 -> FormalParameter()
        * f1 -> FormalParameterTail()
    */
    public String visit(FormalParameterList n, String argu) throws Exception {
        String _ret=null;
        String temp = n.f0.accept(this, argu);
        String temp2 = n.f1.accept(this, temp);
        if(temp2 != null){
            return temp2;
        }
        return temp;
    }

    /**
        * f0 -> Type()
        * f1 -> Identifier()
    */
    public String visit(FormalParameter n, String argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String id = n.f1.accept(this, argu);
        if(argu.contains("//")){
            String temp = argu;
            int index = temp.indexOf("//");
            temp = temp.substring(0, index)+", "+type+" %."+id+temp.substring(index);
            temp += "%"+id+" = alloca "+type+"\n"+
            "store "+type+" %."+id+", "+type+"* %"+id+"\n";
            return temp;
        }else{
            String temp = ", "+type+" %."+id+
            "//"+
            "%"+id+" = alloca "+type+"\n"+
            "store "+type+" %."+id+", "+type+"* %"+id+"\n";
            return temp;
        }
    }

    /**
     * f0 -> ( FormalParameterTerm() )*
    */
    public String visit(FormalParameterTail n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> ","
    * f1 -> FormalParameter()
    */
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }


    /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    public String visit(Type n, String argu) throws Exception {
        return n.f0.accept(this, "noid");
    }

   /**
    * f0 -> BooleanArrayType()
    *       | IntegerArrayType()
    */
    public String visit(ArrayType n, String argu) throws Exception {
        return n.f0.accept(this, argu);
     }
  
     /**
      * f0 -> "boolean"
      * f1 -> "["
      * f2 -> "]"
      */
     public String visit(BooleanArrayType n, String argu) throws Exception {
        return "i1*";
     }
  
     /**
      * f0 -> "int"
      * f1 -> "["
      * f2 -> "]"
    */
    public String visit(IntegerArrayType n, String argu) throws Exception {
        return "i32*";
    }

    /**
     * f0 -> "boolean"
    */
    public String visit(BooleanType n, String argu) throws Exception {
        return "i1";
    }

    /**
     * f0 -> "int"
    */
    public String visit(IntegerType n, String argu) throws Exception {
        return "i32";
    }

    /**
    * f0 -> <IDENTIFIER>
    */
    public String visit(Identifier n, String argu) throws Exception {
        if(argu != null && argu.equals("noid")){
            return "i8*";
        }
        return n.f0.tokenImage;
    }
}