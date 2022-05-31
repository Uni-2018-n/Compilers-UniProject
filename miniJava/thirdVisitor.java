import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import syntaxtree.*;
import visitor.GJDepthFirst;


public class thirdVisitor extends GJDepthFirst<String, String> {
    int regC = 0;
    int labelC = 0;
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

    public Boolean lookUp(String id, String scope){//simple lookup, if the field exists in this scope ok else return null
        if(firstV.fields.containsKey(id)){
            if(firstV.fields.get(id).containsKey(scope)){
                return true;
            }else{
                return false;
            }
        }else{
            return null;
        }
    }

    public String doSmthWithInts(String t1, String t2, String argu, String action){
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        String fin = "";

        int t1c;
        int t1c2;
        if(pattern.matcher(t1).matches()){
            t1c= regC++;
            t1c2= regC++;
            fin +=
                    "%_"+t1c+" = alloca i32\n"+
                            "store i32 "+t1+", i32* %_"+t1c+"\n"+
                            "%_"+t1c2+" = load i32, i32* %_"+t1c+"\n";
        }else if(t1.contains("%")){
            t1c2= Integer.parseInt(t1.substring(t1.lastIndexOf("%_")+2));
        }else{
            t1c= regC++;
            int t1cc = regC++;
            t1c2= regC++;
            if(lookUp(t1, argu)){//check if variable is declared inside the function
                fin +=
                        "%_"+t1c2+" = load i32, i32* %"+t1+"\n";
            }else{
                String type = getDeclarationVar(t1, argu);
                int offset = firstV.getOffset(t1, argu, false);
                fin +=
                    "%_"+t1c+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                    "%_"+t1cc+" = bitcast i8* %_"+t1c+" to i32*\n"+
                    "%_"+t1c2+ " = load i32, i32* %_"+t1cc+"\n";
            }
        }

        int t2c = regC++;
        int t2c2 = regC++;

        if(pattern.matcher(t2).matches()){
            t2c= regC++;
            t2c2= regC++;
            fin +=
                    "%_"+t2c+" = alloca i32\n"+
                            "store i32 "+t2+", i32* %_"+t2c+"\n"+
                            "%_"+t2c2+" = load i32, i32* %_"+t2c+"\n";
        }else if(t2.contains("%")){
            t2c2= Integer.parseInt(t2.substring(t2.lastIndexOf("%_")+2));
        }else{
            t2c= regC++;
            int t2cc = regC++;
            t2c2= regC++;
            if(lookUp(t2, argu)){//check if variable is declared inside the function
                fin +=
                        "%_"+t2c2+" = load i32, i32* %"+t2+"\n";
            }else{
                String type = getDeclarationVar(t1, argu);
                int offset = firstV.getOffset(t1, argu, false);
                fin +=
                        "%_"+t2c+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                                "%_"+t2cc+" = bitcast i8* %_"+t2c+" to i32*\n"+
                                "%_"+t2c2+ " = load i32, i32* %_"+t2cc+"\n";
            }
        }

        int outC =regC++;

        if(action.equals("icmp")){
            fin +=
                    "%_"+outC+" = icmp slt i32 %_"+t1c2+", %_"+t2c2+"\n";
        }else if(action.equals("add")){
            fin +=
                    "%_"+outC+" = add i32 %_"+t1c2+", %_"+t2c2+"\n";
        }else if(action.equals("sub")){
            fin +=
                    "%_"+outC+" = sub i32 %_"+t1c2+", %_"+t2c2+"\n";
        }else if(action.equals("mul")){
            fin +=
                    "%_"+outC+" = mul i32 %_"+t1c2+", %_"+t2c2+"\n";
        }


        out.write(fin);
        return "%_"+String.valueOf(outC);
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

    public String getDeclarationVar(String id, String className){
        String retType = firstV.lookUp(id, className+"::"+id, 0);
        if(retType.equals("int")){
            return "i32";
        }else if(retType.equals("bool")){
            return "i1";
        }else if(retType.equals("intArray")){
            return "i32*";
        }else if(retType.equals("boolArray")){
            return "i1*";
        }else{
            return "i8*";
        }
    }

    public String getDeclarationFunc(String id, String className){
        while(true){
            String retType = firstV.lookUp(id, className, 1);
            if(retType == null){
                if(className.contains("::")){
                    className = className.substring(0, className.lastIndexOf("::"));
                }
            }else if(retType.equals("int")){
                return "i32";
            }else if(retType.equals("bool")){
                return "i1";
            }else if(retType.equals("intArray")){
                return "i32*";
            }else if(retType.equals("boolArray")){
                return "i1*";
            }else{
                return "i8*";
            }

        }
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

        for(int i=0;i<ite.getValue().functions.size();i++){
            vTables +="i8* bitcast ("+getDeclaration(ite.getValue().functions.get(i).id, ite.getKey())+")";
            if(i+1<ite.getValue().functions.size()){
                vTables +=", ";
            }
        }
        vTables +="]\n";
    }
    vTables +="\n\n";
    out.write(vTables);


    String initStr = 
    "declare i8* @calloc(i32, i32)\n" +
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
        regC = 0;
        labelC =0;
        String _ret=null;
        String cName = n.f1.accept(this, "");
        String str = 
        "define i32 @main() {\n";
        out.write(str);
        n.f14.accept(this, "");
        n.f15.accept(this, cName+"::main");
        str = "ret i32 0\n"+
        "}\n";
        out.write(str);
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
        // n.f3.accept(this, argu); //no need for var declaration
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
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {//TODO: extends nothing is done
        String _ret=null;
        String id = n.f1.accept(this, argu);
        n.f3.accept(this, argu);
        // n.f5.accept(this, argu); //no need for var declaration
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
        regC =0;
        String _ret=null;
        String type = n.f1.accept(this, argu);
        String id = n.f2.accept(this, argu);
        String params = n.f4.accept(this, argu);

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
        n.f8.accept(this, argu+"::"+id);
        String fullArg = firstV.classesLookup(argu)+"::"+id;
        String tempCout = n.f10.accept(this, fullArg);
        String ret =
                "ret "+type+" "+tempCout+"\n";
        out.write(ret);
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
        String temp ="";
        if(argu.contains("//")){
            temp = argu;
            int index = temp.indexOf("//");
            temp = temp.substring(0, index)+", "+type+" %."+id+temp.substring(index);
            temp += "%"+id+" = alloca "+type+"\n"+
            "store "+type+" %."+id+", "+type+"* %"+id+"\n";
            return temp;
        }else{
            temp = ", "+type+" %."+id+
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
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */
    public String visit(Statement n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, String argu) throws Exception {
        String id = n.f0.accept(this, argu);
        String fin ="";
        if(lookUp(id, argu)){
            String type = getDeclarationVar(id, argu);
            if(type.contains("i32")){
                String exprReg = n.f2.accept(this, argu);
                if(exprReg.contains("::/::")){
                    exprReg = exprReg.substring(exprReg.indexOf("::/::")+5);
                }
                if(type.contains("*")){
                    fin +=
                        "store i32* "+exprReg+", i32** %"+id+"\n";
                }else{
                    fin +=
                            "store i32 "+exprReg+", i32* %"+id+"\n";
                }
            }else if(type.contains("i1")){
                String exprReg = n.f2.accept(this, argu+"::/::bool");
                if(exprReg.contains("::/::")){
                    exprReg = exprReg.substring(exprReg.indexOf("::/::")+5);
                }
                if(type.contains("*")){
                    fin +=
                            "store i32* "+exprReg+", i32** %"+id+"\n";
                }else{
                    fin +=
                            "store i1 "+exprReg+", i1* %"+id+"\n";
                }
            }else if(type.contains("i8")){
                String exprReg = n.f2.accept(this, argu);
                if(exprReg.contains("::/::")){
                    exprReg = exprReg.substring(exprReg.indexOf("::/::")+5);
                }
                fin +=
                        "store i8* "+exprReg+", i8** %"+id+"\n";
            }
        }else{
            int offset = firstV.getOffset(id, argu, false);
            String type = getDeclarationVar(id, argu);
            int t1 = regC++;
            int t2 = regC++;
            if(type.contains("*")){
                if(type.contains("i8")){
                    String exprReg = n.f2.accept(this, argu);
                    if(exprReg.contains("::/::")){
                        exprReg = exprReg.substring(exprReg.indexOf("::/::")+5);
                    }
                    fin +=
                        "%_"+t1+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                        "%_"+t2+" = bitcast i8* %_"+t1+" to i8**\n"+
                        "store i8* "+exprReg+", i8** %_"+t2+"\n";
                }else{
                    String exprReg = n.f2.accept(this, argu);
                    if(exprReg.contains("::/::")){
                        exprReg = exprReg.substring(exprReg.indexOf("::/::")+5);
                    }
                    fin +=
                        "%_"+t1+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                        "%_"+t2+" = bitcast i8* %_"+t1+" to i32**\n"+
                        "store i32* "+exprReg+", i32** %_"+t2+"\n";
                }
            }else{
                if(type.contains("i32")){
                    String exprReg = n.f2.accept(this, argu);
                    if(exprReg.contains("::/::")){
                        exprReg = exprReg.substring(exprReg.indexOf("::/::")+5);
                    }
                    fin +=
                        "%_"+t1+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                        "%_"+t2+" = bitcast i8* %_"+t1+" to i32*\n"+
                        "store i32 "+exprReg+", i32* %_"+t2+"\n";
                }else if(type.contains("i1")){
                    String exprReg = n.f2.accept(this, argu+"::/::bool");
                    if(exprReg.contains("::/::")){
                        exprReg = exprReg.substring(exprReg.indexOf("::/::")+5);
                    }
                    fin +=
                        "%_"+t1+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                        "%_"+t2+" = bitcast i8* %_"+t1+" to i1*\n"+
                        "store i1 "+exprReg+", i1* %_"+t2+"\n";
                }
            }
        }
        out.write(fin);
        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {
        String _ret=null;
        String id = n.f0.accept(this, argu);
        String expr1 = n.f2.accept(this, argu);
        String expr2 = n.f5.accept(this, argu);
        String fin = "";
        int t1 = regC++;
        int t2 = regC++;
        int t3 = regC++;
        int t4 = regC++;
        int t5 = regC++;
        int l1 = labelC++;
        int l2 = labelC++;
        int l3 = labelC++;
        if(lookUp(id, argu)){
            String type = getDeclarationVar(id, argu);
            if(type.contains("i32")){
                fin +=
                    "%_"+t1+" = load i32*, i32** %"+id+"\n"+
                    "%_"+t2+" = load i32, i32* %_"+t1+"\n"+
                    "%_"+t3+" = icmp ult i32 "+expr1+", %_"+t2+"\n"+
                    "br i1 %_"+t3+", label %oob"+l1+", label %oob"+l2+"\n"+
                "oob"+l1+":\n"+
                    "%_"+t4+" = add i32 "+expr1+", 1\n"+
                    "%_"+t5+" = getelementptr i32, i32* %_"+t1+", i32 %_"+t4+"\n"+
                    "store i32 "+expr2+", i32* %_"+t5+"\n"+
                    "br label %oob"+l3+"\n"+
                "oob"+l2+":\n"+
                    "call void @throw_oob()\n"+
                    "br label %oob"+l3+"\n"+
                "oob"+l3+":\n";
            }else if(type.contains("i1")){
                int t6 = regC++;
                int t7 = regC++;
                int t8 = regC++;
                fin +=
                    "%_"+t1+" = load i32*, i32** %"+id+"\n"+
                    "%_"+t2+" = load i32, i32* %_"+t1+"\n"+
                    "%_"+t3+" = icmp ult i32 "+expr1+", %_"+t2+"\n"+
                    "br i1 %_"+t3+", label %oob"+l1+", label %oob"+l2+"\n"+
                "oob"+l1+":\n"+
                    "%_"+t4+" = add i32 "+expr1+", 1\n"+
                    "%_"+t5+" = getelementptr i32, i32* %_"+t1+", i32 %_"+t4+"\n"+
                    "%_"+t6+" = alloca i1\n"+
                    "store i1 "+expr2+", i1* %_"+t6+"\n"+
                    "%_"+t7+" = bitcast i1* %_"+t6+" to i32*\n"+
                    "%_"+t8+" = load i32, i32* %_"+t7+"\n"+
                    "store i32 %_"+t8+", i32* %_"+t5+"\n"+
                    "br label %oob"+l3+"\n"+
                "oob"+l2+":\n"+
                    "call void @throw_oob()\n"+
                    "br label %oob"+l3+"\n"+
                "oob"+l3+":\n";
            }
        }else{
            int offset = firstV.getOffset(id, argu, false);
            String type = getDeclarationVar(id, argu);
            int t6 = regC++;
            int t7 = regC++;
            if(type.contains("i32")){
                fin +=
                        "%_"+t1+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                        "%_"+t2+" = bitcast i8* %_"+t1+" to i32**\n"+
                        "%_"+t3+" = load i32*, i32** %_"+t2+"\n"+
                        "%_"+t4+" = load i32, i32* %_"+t3+"\n"+
                        "%_"+t5+" = icmp ult i32 "+expr1+", %_"+t4+"\n"+
                        "br i1 %_"+t5+", label %oob"+l1+", label %oob"+l2+"\n"+
                    "oob"+l1+":\n"+
                        "%_"+t6+" = add i32 "+expr1+", 1\n"+
                        "%_"+t7+" = getelementptr i32, i32* %_"+t3+", i32 %_"+t6+"\n"+
                        "store i32 "+expr2+", i32* %_"+t7+"\n"+
                        "br label %oob"+l3+"\n"+
                    "oob"+l2+":\n"+
                        "call void @throw_oob()\n"+
                        "br label %oob"+l3+"\n"+
                    "oob"+l3+":\n";

            }else if(type.contains("i1")){
                int t8 = regC++;
                int t9 = regC++;
                int t10 = regC++;
                fin +=""+
                        "%_"+t1+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                        "%_"+t2+" = bitcast i8* %_"+t1+" to i32**\n"+
                        "%_"+t3+" = load i32*, i32** %_"+t2+"\n"+
                        "%_"+t4+" = load i32, i32* %_"+t3+"\n"+
                        "%_"+t5+" = icmp ult i32 "+expr1+", %_"+t4+"\n"+
                        "br i1 %_"+t5+", label %oob"+l1+", label %oob"+l2+"\n"+
                    "oob"+l1+":\n"+
                        "%_"+t6+" = add i32 "+expr1+", 1\n"+
                        "%_"+t7+" = getelementptr i32, i32* %_"+t3+", i32 %_"+t6+"\n"+
                        "%_"+t8+" = alloca i1\n"+
                        "store i1 "+expr2+", i1* %_"+t8+"\n"+
                        "%_"+t9+" = bitcast i1* %_"+t8+" to i32*\n"+
                        "%_"+t10+" = load i32, i32* %_"+t9+"\n"+
                        "store i32 %_"+t10+", i32* %_"+t7+"\n"+
                        "br label %oob"+l3+"\n"+
                    "oob"+l2+":\n"+
                        "call void @throw_oob()\n"+
                        "br label %oob"+l3+"\n"+
                    "oob"+l3+":\n";
            }
        }
        out.write(fin);
        return _ret;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfStatement n, String argu) throws Exception {
        String expr = n.f2.accept(this, argu);

        int lbl1 = labelC++;
        int lbl2 = labelC++;
        int lbl3 = labelC++;
        String fin =
                "br i1 "+expr+", label %then"+lbl1+", label %else"+lbl2+"\n"+
            "then"+lbl1+":\n";
        out.write(fin);
                String stmtIf = n.f4.accept(this, argu);
        fin =
                "br label %fin"+lbl3+"\n"+
            "else"+lbl2+":\n";
        out.write(fin);
                String stmtElse = n.f6.accept(this, argu);
        fin =
                "br label %fin"+lbl3+"\n"+
                "fin"+lbl3+": \n";
        out.write(fin);
        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, String argu) throws Exception {

        int lbl1 = labelC++;
        int lbl2 = labelC++;
        int lbl3 = labelC++;

        String fin =
                "br label %expr"+lbl1+"\n"+
            "expr"+lbl1+":\n";
        out.write(fin);
                String expr = n.f2.accept(this, argu);
        fin =
                "br i1 "+expr+", label %stmt"+lbl2+", label %fin"+lbl3+"\n"+
            "stmt"+lbl2+":\n";
        out.write(fin);
                String stmt = n.f4.accept(this, argu);
        fin =
                "br label %expr"+lbl1+"\n"+
            "fin"+lbl3+":\n";
        out.write(fin);

        return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, String argu) throws Exception {
        String t = n.f2.accept(this, argu);
        String fin =
                "call void (i32) @print_int(i32 "+t+")\n";
        out.write(fin);
        return null;
    }


    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | Clause()
     */
    public String visit(Expression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, String argu) throws Exception {
        if(argu.contains("::/::"))
        argu = argu.substring(0, argu.lastIndexOf("::/::"));
        String t1 = n.f0.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        int l1 = labelC++;
        int l2 = labelC++;
        int l3 = labelC++;
        int regOut = regC++;
        String fin =
                "br i1 "+t1+", label %if"+l1+", label %else"+l2+"\n"+
            "if"+l1+":\n";
        fin +=
                "br label %fin"+l3+"\n"+
            "else"+l2+":\n"+
                "br label %fin"+l3+"\n"+
            "fin"+l3+":\n"+
                "%_"+regOut+" = phi i1 [ false, %if"+l1+"], ["+t2+", %else"+l2+"]\n";
        out.write(fin);
        return "%_"+regOut;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String argu) throws Exception {
        if(argu.contains("::/::"))
            argu = argu.substring(0, argu.lastIndexOf("::/::"));
        String t1 = n.f0.accept(this, argu);
        String t2 = n.f2.accept(this, argu);

        return doSmthWithInts(t1, t2, argu, "icmp");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception {
        if(argu.contains("::/::"))
            argu = argu.substring(0, argu.lastIndexOf("::/::"));
        String t1 = n.f0.accept(this, argu);
        String t2 = n.f2.accept(this, argu);

        return doSmthWithInts(t1, t2, argu, "add");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String argu) throws Exception {
        if(argu.contains("::/::"))
            argu = argu.substring(0, argu.lastIndexOf("::/::"));
        String t1 = n.f0.accept(this, argu);
        String t2 = n.f2.accept(this, argu);

        return doSmthWithInts(t1, t2, argu, "sub");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String argu) throws Exception {
        if(argu.contains("::/::"))
            argu = argu.substring(0, argu.lastIndexOf("::/::"));
        String t1 = n.f0.accept(this, argu);
        String t2 = n.f2.accept(this, argu);

        return doSmthWithInts(t1, t2, argu, "mul");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, String argu) throws Exception {
        boolean isBool = argu.contains("::/::bool");
        if(argu.contains("::/::"))
            argu = argu.substring(0, argu.lastIndexOf("::/::"));
        String _ret=null;
        String id = n.f0.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);
        String tFin2;
        String fin = "";
        String tFin1 = "%_"+regC++;
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if(pattern.matcher(expr2).matches()){
            int regT = regC++;
            int reg = regC++;
            tFin2 = "%_"+reg;
            fin +=
                "%_"+regT+" = alloca i32\n"+
                "store i32 "+expr2+", i32* %_"+regT+"\n";
            // tFin1 = regC++;
            fin +=
                tFin2+" = load i32, i32* %_"+regT+"\n";
            out.write(fin);
        }else if(expr2.contains("%")){
            tFin2 = expr2;
        }else if(lookUp(expr2, argu)){
            int reg = regC++;
            tFin2 = "%_"+reg;
            fin =
                tFin2+" = load i32, i32* %"+expr2+"\n";
            out.write(fin);
        }else{
            int t1c = regC++;
            int t1cc = regC++;
            int reg = regC++;
            int offset = firstV.getOffset(expr2, argu, false);
            fin =
                "%_"+t1c+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                "%_"+t1cc+" = bitcast i8* %_"+t1c+" to i32*\n"+
                "%_"+reg+ " = load i32, i32* %_"+t1cc+"\n";
            tFin2 = "%_"+reg;
            out.write(fin);
        }

        int t1 = regC++;
        int t2 = regC++;
        int t3 = regC++;
        int t4 = regC++;
        int t5 = regC++;
        int l1 = labelC++;
        int l2 = labelC++;
        int l3 = labelC++;


        if(id.contains("%")){
            if(isBool){
                fin =
                    "%_"+t1+" = load i32, i32* "+id+"\n"+
                    "%_"+t2+" = icmp ult i32 "+tFin2+", %_"+t1+"\n"+
                    "br i1 %_"+t2+", label %oob"+l1+", label %oob"+l2+"\n"+
                "oob"+l1+":\n"+
                    "%_"+t3+" = add i32 "+tFin2+", 1\n"+
                    "%_"+t4+" = getelementptr i32, i32* "+id+", i32 %_"+t3+"\n"+
                    "%_"+t5+" = bitcast i32* %_"+t4+" to i1*\n"+
                    tFin1+" = load i1, i1* %_"+t5+"\n"+
                    "br label %oob"+l3+"\n"+
                "oob"+l2+":\n"+
                    "call void @throw_oob()\n"+
                    "br label %oob"+l3+"\n"+
                    "oob"+l3+":\n";
            }else{
                fin =
                    "%_"+t1+" = load i32, i32* "+id+"\n"+
                    "%_"+t2+" = icmp ult i32 "+tFin2+", %_"+t1+"\n"+
                    "br i1 %_"+t2+", label %oob"+l1+", label %oob"+l2+"\n"+
                "oob"+l1+":\n"+
                    "%_"+t3+" = add i32 "+tFin2+", 1\n"+
                    "%_"+t4+" = getelementptr i32, i32* "+id+", i32 %_"+t3+"\n"+
                    tFin1+" = load i32, i32* %_"+t4+"\n"+
                    "br label %oob"+l3+"\n"+
                "oob"+l2+":\n"+
                    "call void @throw_oob()\n"+
                    "br label %oob"+l3+"\n"+
                    "oob"+l3+":\n";
            }
        }else if(lookUp(id, argu)){
            String type = getDeclarationVar(id, argu);
            if(type.contains("i32")){
                fin =
                        "%_"+t1+" = load i32*, i32** %"+id+"\n"+
                                "%_"+t2+" = load i32, i32* %_"+t1+"\n"+
                                "%_"+t3+" = icmp ult i32 "+tFin2+", %_"+t2+"\n"+
                                "br i1 %_"+t3+", label %oob"+l1+", label %oob"+l2+"\n"+
                                "oob"+l1+":\n"+
                                "%_"+t4+" = add i32 "+tFin2+", 1\n"+
                                "%_"+t5+" = getelementptr i32, i32* %_"+t1+", i32 %_"+t4+"\n"+
                                tFin1+" = load i32, i32* %_"+t5+"\n"+
                                "br label %oob"+l3+"\n"+
                                "oob"+l2+":\n"+
                                "call void @throw_oob()\n"+
                                "br label %oob"+l3+"\n"+
                                "oob"+l3+":\n";
            }else if(type.contains("i1")){
                int t7 = regC++;
                int t8 = regC++;
                fin =
                        "%_"+t1+" = load i32*, i32** %"+id+"\n"+
                                "%_"+t2+" = load i32, i32* %_"+t1+"\n"+
                                "%_"+t3+" = icmp ult i32 "+tFin2+", %_"+t2+"\n"+
                                "br i1 %_"+t3+", label %oob"+l1+", label %oob"+l2+"\n"+
                                "oob"+l1+":\n"+
                                "%_"+t4+" = add i32 "+tFin2+", 1\n"+
                                "%_"+t5+" = getelementptr i32, i32* %_"+t1+", i32 %_"+t4+"\n"+
                                "%_"+t8+" = bitcast i32* %_"+t5+" to i1*\n"+
                                tFin1+" = load i1, i1* %_"+t8+"\n"+
                                "br label %oob"+l3+"\n"+
                                "oob"+l2+":\n"+
                                "call void @throw_oob()\n"+
                                "br label %oob"+l3+"\n"+
                                "oob"+l3+":\n";
            }
        }else{
            int offset = firstV.getOffset(id, argu, false);
            String type = getDeclarationVar(id, argu);
            int t6 = regC++;
            int t7 = regC++;
            if(type.contains("i32")){
                fin =
                        "%_"+t1+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                                "%_"+t2+" = bitcast i8* %_"+t1+" to i32**\n"+
                                "%_"+t3+" = load i32*, i32** %_"+t2+"\n"+
                                "%_"+t4+" = load i32, i32* %_"+t3+"\n"+
                                "%_"+t5+" = icmp ult i32 "+tFin2+", %_"+t4+"\n"+
                                "br i1 %_"+t5+", label %oob"+l1+", label %oob"+l2+"\n"+
                                "oob"+l1+":\n"+
                                "%_"+t6+" = add i32 "+tFin2+", 1\n"+
                                "%_"+t7+" = getelementptr i32, i32* %_"+t3+", i32 %_"+t6+"\n"+
                                tFin1+" = load i32, i32* %_"+t7+"\n"+
                                "br label %oob"+l3+"\n"+
                                "oob"+l2+":\n"+
                                "call void @throw_oob()\n"+
                                "br label %oob"+l3+"\n"+
                                "oob"+l3+":\n";

            }else if(type.contains("i1")){
                int t11 = regC++;
                fin =""+
                        "%_"+t1+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                        "%_"+t2+" = bitcast i8* %_"+t1+" to i32**\n"+
                        "%_"+t3+" = load i32*, i32** %_"+t2+"\n"+
                        "%_"+t4+" = load i32, i32* %_"+t3+"\n"+
                        "%_"+t5+" = icmp ult i32 "+tFin2+", %_"+t4+"\n"+
                        "br i1 %_"+t5+", label %oob"+l1+", label %oob"+l2+"\n"+
                        "oob"+l1+":\n"+
                        "%_"+t6+" = add i32 "+tFin2+", 1\n"+
                        "%_"+t7+" = getelementptr i32, i32* %_"+t3+", i32 %_"+t6+"\n"+
                        "%_"+t11+" = bitcast i32* %_"+t7+" to i1*\n"+
                        tFin1+" = load i1, i1* %_"+t11+"\n"+
                        "br label %oob"+l3+"\n"+
                        "oob"+l2+":\n"+
                        "call void @throw_oob()\n"+
                        "br label %oob"+l3+"\n"+
                        "oob"+l3+":\n";
            }
        }

        out.write(fin);

        return tFin1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String argu) throws Exception {
        if(argu.contains("::/::"))
            argu = argu.substring(0, argu.lastIndexOf("::/::"));
        String _ret=null;
        String expr = n.f0.accept(this, argu);
        String fin = "";
        int t1 = regC++;
        if(expr.contains("%")){
            fin =
                "%_"+t1+" = load i32, i32* "+expr+"\n";
        }else if(lookUp(expr, argu)){
            int t2 = regC++;
            fin =
                "%_"+t2+" = load i32*, i32** %"+expr+"\n"+
                "%_"+t1+" = load i32, i32* %_"+t2+"\n";
        }else{
            int offset = firstV.getOffset(expr, argu, false);
            int t2 = regC++;
            int t3 = regC++;
            int t4 = regC++;
            fin =
                "%_"+t2+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                "%_"+t3+" = bitcast i8* %_12 to i32**\n"+
                "%_"+t4+" = load i32*, i32** %_"+t3+"\n"+
                "%_"+t1+" = load i32, i32* %_"+t4+"\n";
        }
        out.write(fin);
        return "%_"+t1;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n, String argu) throws Exception {
        if(argu.contains("::/::"))
            argu = argu.substring(0, argu.lastIndexOf("::/::"));
        String _ret=null;
        String expr = n.f0.accept(this, argu);
        String id = n.f2.accept(this, argu);
        String args = n.f4.accept(this, argu);
        String fin = "";
        int tFin = regC++;
        if(expr.contains("%")){
            String className;
            if(expr.contains("::/::")){
                className = expr.substring(0, expr.indexOf("::/::"));
                expr = expr.substring(expr.indexOf("::/::")+5);
            }else{
                String temp = argu.substring(0, argu.lastIndexOf("::"));
                if(temp.contains("::")){
                    className = temp.substring(temp.lastIndexOf("::")+2);
                }else{
                    className = temp;
                }
            }

            int offset = firstV.getOffset(id, className, true)/8;
            String type = getDeclarationFunc(id, firstV.classesLookup(className));
            String declaredArgs = getDeclaration(id, className);
            declaredArgs = declaredArgs.substring(0, declaredArgs.lastIndexOf(" @"));
            int t1 = regC++;
            int t2 = regC++;
            int t3 = regC++;
            int t4 = regC++;
            int t5 = regC++;
            fin +=
                "%_"+t1+" = bitcast i8* "+expr+" to i8***\n"+
                "%_"+t2+" = load i8**, i8*** %_"+t1+"\n"+
                "%_"+t3+" = getelementptr i8*, i8** %_"+t2+", i32 "+offset+"\n"+
                "%_"+t4+" = load i8*, i8** %_"+t3+"\n"+
                "%_"+t5+" = bitcast i8* %_"+t4+" to "+declaredArgs+"\n"+
                "%_"+tFin+" = call "+type+" %_"+t5+"(i8* "+expr;
            if(declaredArgs.contains(",")){
                declaredArgs = declaredArgs.substring(declaredArgs.indexOf(",")+1);
                declaredArgs = declaredArgs.substring(0, declaredArgs.length()-2);
                while(declaredArgs.contains(",")){
                    String temp = declaredArgs.substring(0, declaredArgs.indexOf(","));
                    String valueTemp = args.substring(0, args.indexOf("::"));
                    fin += ", "+temp+" "+valueTemp;
                    declaredArgs = declaredArgs.substring(declaredArgs.indexOf(",")+1);
                    args = args.substring(args.indexOf("::")+2);
                }
                fin += ", "+declaredArgs+" "+args+")\n";
            }else{
                fin +=")\n";
            }
        }else{
            if(lookUp(expr, argu)){
                String className = firstV.lookUp(expr, argu, 0);
                int offset = firstV.getOffset(id, className, true)/8;
                String type = getDeclarationFunc(id, firstV.classesLookup(className));
                String declaredArgs = getDeclaration(id, className);
                declaredArgs = declaredArgs.substring(0, declaredArgs.lastIndexOf(" @"));
                int t1 = regC++;
                int t2 = regC++;
                int t3 = regC++;
                int t4 = regC++;
                int t5 = regC++;
                int t7 = regC++;
                fin +=
                    "%_"+t7+" = load i8*, i8** %"+expr+"\n"+
                    "%_"+t1+" = bitcast i8* %_"+t7+" to i8***\n"+
                    "%_"+t2+" = load i8**, i8*** %_"+t1+"\n"+
                    "%_"+t3+" = getelementptr i8*, i8** %_"+t2+", i32 "+offset+"\n"+
                    "%_"+t4+" = load i8*, i8** %_"+t3+"\n"+
                    "%_"+t5+" = bitcast i8* %_"+t4+" to "+declaredArgs+"\n"+
                    "%_"+tFin+" = call "+type+" %_"+t5+"(i8* %_"+t7;
                if(declaredArgs.contains(",")){
                    declaredArgs = declaredArgs.substring(declaredArgs.indexOf(",")+1);
                    declaredArgs = declaredArgs.substring(0, declaredArgs.length()-2);
                    while(declaredArgs.contains(",")){
                        String temp = declaredArgs.substring(0, declaredArgs.indexOf(","));
                        String valueTemp = args.substring(0, args.indexOf("::"));
                        fin += ", "+temp+" "+valueTemp;
                        declaredArgs = declaredArgs.substring(declaredArgs.indexOf(",")+1);
                        args = args.substring(args.indexOf("::")+2);
                    }
                    fin += ", "+declaredArgs+" "+args+")\n";
                }else{
                    fin +=")\n";
                }
            }else{
                String className = firstV.lookUp(expr, argu, 0);//of i
                int myOffset = firstV.getOffset(expr, argu, false);//offset of i
                int offset = firstV.getOffset(id, className, true)/8;//offset of test
                String type = getDeclarationFunc(id, firstV.classesLookup(className));//type of test
                String declaredArgs = getDeclaration(id, className);//arguments of test
                declaredArgs = declaredArgs.substring(0, declaredArgs.lastIndexOf(" @"));
                int t1 = regC++;
                int t2 = regC++;
                int t3 = regC++;
                int t4 = regC++;
                int t5 = regC++;
                int t7 = regC++;
                int t8 = regC++;
                int t9 = regC++;
                fin +=
                    "%_"+t8+" = getelementptr i8, i8* %this, i32 "+myOffset+"\n"+
                    "%_"+t9+" = bitcast i8* %_"+t8+" to i8**\n"+

                    "%_"+t7+" = load i8*, i8** %_"+t9+"\n"+
                    "%_"+t1+" = bitcast i8* %_"+t7+" to i8***\n"+
                    "%_"+t2+" = load i8**, i8*** %_"+t1+"\n"+
                    "%_"+t3+" = getelementptr i8*, i8** %_"+t2+", i32 "+offset+"\n"+
                    "%_"+t4+" = load i8*, i8** %_"+t3+"\n"+
                    "%_"+t5+" = bitcast i8* %_"+t4+" to "+declaredArgs+"\n"+
                    "%_"+tFin+" = call "+type+" %_"+t5+"(i8* %_"+t7;
                if(declaredArgs.contains(",")){
                    declaredArgs = declaredArgs.substring(declaredArgs.indexOf(",")+1);
                    declaredArgs = declaredArgs.substring(0, declaredArgs.length()-2);
                    while(declaredArgs.contains(",")){
                        String temp = declaredArgs.substring(0, declaredArgs.indexOf(","));
                        String valueTemp = args.substring(0, args.indexOf("::"));
                        fin += ", "+temp+" "+valueTemp;
                        declaredArgs = declaredArgs.substring(declaredArgs.indexOf(",")+1);
                        args = args.substring(args.indexOf("::")+2);
                    }
                    fin += ", "+declaredArgs+" "+args+")\n";
                }else{
                    fin +=")\n";
                }
            }
        }
        out.write(fin);
        return "%_"+tFin;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String argu) throws Exception {
        String expr1 = n.f0.accept(this, argu);
        String expr2 = n.f1.accept(this, argu);
        if(expr2 != null){
            return n.f0.accept(this, argu)+n.f1.accept(this, argu);
        }else{
            return n.f0.accept(this, argu);
        }
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, String argu) throws Exception {
        return "::"+n.f1.accept(this, argu);
    }

    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    public String visit(Clause n, String argu) throws Exception {
        boolean isInt = argu.contains("::/::");
        if(argu.contains("::/::"))
            argu = argu.substring(0, argu.lastIndexOf("::/::"));
        String t = n.f0.accept(this, argu);
        int reg;
        String fin = "";
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if(pattern.matcher(t).matches()){
            int regT = regC++;
            fin +=
                "%_"+regT+" = alloca i32\n"+
                "store i32 "+t+", i32* %_"+regT+"\n";
            reg = regC++;
            fin +=
                "%_"+reg+" = load i32, i32* %_"+regT+"\n";
        }else if(t.equals("true")){
            int regT = regC++;
            fin +=
                "%_"+regT+" = alloca i1\n"+
                "store i1 1, i1* %_"+regT+"\n";
            reg = regC++;
            fin += "%_"+reg+" = load i1, i1* %_"+regT+"\n";
        }else if(t.equals("false")){
            int regT = regC++;
            fin +=
                    "%_"+regT+" = alloca i1\n"+
                    "store i1 0, i1* %_"+regT+"\n";
            reg = regC++;
            fin += "%_"+reg+" = load i1, i1* %_"+regT+"\n";
        }else if(t.contains("%")){
            return t;
        }else{
            reg = regC++;
            if(lookUp(t, argu)){//check if variable is declared inside the function
                String type = getDeclarationVar(t, argu);
                if(type.contains("i32")){
                    fin +=
                        "%_"+reg+" = load i32, i32* %"+t+"\n";
                }else if(type.contains("i1")){
                    fin +=
                        "%_"+reg+" = load i1, i1* %"+t+"\n";
                }
            }else{
                int t1c = regC++;
                int t1cc = regC++;
                String type = getDeclarationVar(t, argu);
                int offset = firstV.getOffset(t, argu, false);
                if(type.contains("i32")){
                    fin +=
                        "%_"+t1c+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                        "%_"+t1cc+" = bitcast i8* %_"+t1c+" to i32*\n"+
                        "%_"+reg+ " = load i32, i32* %_"+t1cc+"\n";

                }else if(type.contains("i1")){
                    fin +=
                        "%_"+t1c+" = getelementptr i8, i8* %this, i32 "+offset+"\n"+
                        "%_"+t1cc+" = bitcast i8* %_"+t1c+" to i1*\n"+
                        "%_"+reg+ " = load i1, i1* %_"+t1cc+"\n";
                }
            }
        }
        out.write(fin);
        return "%_"+reg;
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, String argu) throws Exception {
        String _ret=null;
        String t = n.f1.accept(this, argu);
        int reg = regC++;
        String fin =
                "%_"+reg+" = xor i1 "+t+", true\n";//TODO: check if xor is 100% correct for not.
        out.write(fin);
        return "%_"+reg;
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
    public String visit(PrimaryExpression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
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
        return "i32*";
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
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String argu) throws Exception {
        return n.f0.tokenImage;
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, String argu) throws Exception {
        return "true";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, String argu) throws Exception {
        return "false";
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

    /**
        * f0 -> "this"
        */
    public String visit(ThisExpression n, String argu) throws Exception {
        return "%this";
    }

    /**
     * f0 -> BooleanArrayAllocationExpression()
     *       | IntegerArrayAllocationExpression()
     */
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "new"
     * f1 -> "boolean"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(BooleanArrayAllocationExpression n, String argu) throws Exception {
        String expr = n.f3.accept(this, argu);
        int t1 = regC++;
        int t2 = regC++;
        int t3 = regC++;
        int t4 = regC++;
        int l1 = labelC++;
        int l2 = labelC++;
        String fin =
                "%_"+t1+" = icmp slt i32 "+expr+", 0\n"+
                        "br i1 %_"+t1+", label %oob"+l1+", label %arr_alloc"+l2+"\n"+
                        "oob"+l1+":\n"+
                        "call void @throw_oob()\n"+
                        "br label %arr_alloc"+l2+"\n"+
                        "arr_alloc"+l2+":\n"+
                        "%_"+t2+" = add i32 "+expr+", 1\n"+
                        "%_"+t3+" = call i8* @calloc(i32 4, i32 %_"+t2+")\n"+
                        "%_"+t4+" = bitcast i8* %_"+t3+" to i32*\n"+
                        "store i32 "+expr+", i32* %_"+t4+"\n";
        out.write(fin);
        return "%_"+t4;
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(IntegerArrayAllocationExpression n, String argu) throws Exception {
        String expr = n.f3.accept(this, argu);
        int t1 = regC++;
        int t2 = regC++;
        int t3 = regC++;
        int t4 = regC++;
        int l1 = labelC++;
        int l2 = labelC++;
        String fin =
                "%_"+t1+" = icmp slt i32 "+expr+", 0\n"+
                "br i1 %_"+t1+", label %oob"+l1+", label %arr_alloc"+l2+"\n"+
            "oob"+l1+":\n"+
                "call void @throw_oob()\n"+
                "br label %arr_alloc"+l2+"\n"+
            "arr_alloc"+l2+":\n"+
                "%_"+t2+" = add i32 "+expr+", 1\n"+
                "%_"+t3+" = call i8* @calloc(i32 4, i32 %_"+t2+")\n"+
                "%_"+t4+" = bitcast i8* %_"+t3+" to i32*\n"+
                "store i32 "+expr+", i32* %_"+t4+"\n";
        out.write(fin);
        return "%_"+t4;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, String argu) throws Exception {
        String id = n.f1.accept(this, argu);
        int offset = firstV.getOffsetLast(id, false);
        int funcsLen = firstV.getOffsetLen(id, true);

        int t1 = regC++;
        int t2 = regC++;
        int t3 = regC++;
        String fin =
                "%_"+t1+" = call i8* @calloc(i32 1, i32 "+(offset+8)+")\n"+
                "%_"+t2+" = bitcast i8* %_"+t1+" to i8***\n"+
                "%_"+t3+" = getelementptr ["+funcsLen+" x i8*], ["+funcsLen+" x i8*]* @."+id+"_vtable, i32 0, i32 0\n"+
                "store i8** %_"+t3+", i8*** %_"+t2+"\n";
        out.write(fin);
        return id+"::/::%_"+t1;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }
}