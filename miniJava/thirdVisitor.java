import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
        System.out.println(t1);
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
            t1c2= regC++;
            if(lookUp(t1, argu)){//check if variable is declared inside the function
                fin +=
                        "%_"+t1c2+" = load i32, i32* %"+t1+"\n";
            }else{//TODO: case variable is a field

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
            t2c2= regC++;
            if(lookUp(t2, argu)){//check if variable is declared inside the function
                fin +=
                        "%_"+t2c2+" = load i32, i32* %"+t2+"\n";
            }else{//TODO: case variable is a field

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
        regC =0;
        labelC =0;
        String _ret=null;
        String type = n.f1.accept(this, argu);
        String id = n.f2.accept(this, argu);
        String params = n.f4.accept(this, argu);
        n.f8.accept(this, argu);

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
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
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
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
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
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    public String visit(Clause n, String argu) throws Exception {
        String t = n.f0.accept(this, argu);
        int reg;
        String fin = "";
        if(t.equals("true")){
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
                fin +=
                    "%_"+reg+" = load i1, i1* %"+t+"\n";
            }else{//TODO: case variable is a field

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
                "%_"+reg+" = xor i1 "+t+", true\n";
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
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }
}