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
    "@_cint = constant [4 x i8] c"+"%d\0a\00"+"\n"+
    "@_cOOB = constant [15 x i8] c"+"Out of bounds\0a\00"+"\n"+
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
        "@."+cName+"_vtable = global [0 x i8*] [] \n"+
        "define i32 @main() {\n"+
        "ret i32 0\n"+
        "}\n";
        n.f11.accept(this, argu);
        n.f14.accept(this, argu);
        n.f15.accept(this, argu);
        return _ret;
     }


    /**
    * f0 -> <IDENTIFIER>
    */
   public String visit(Identifier n, String argu) throws Exception {
    return n.f0.tokenImage;
 }
}