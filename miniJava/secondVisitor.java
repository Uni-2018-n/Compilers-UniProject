import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.HashMap;

import javax.print.DocFlavor.STRING;


public class secondVisitor extends GJDepthFirst<String, String> {
    firstVisitor firstV;
    public secondVisitor(firstVisitor f){
        super();
        firstV = f;
    }

    public boolean isSubClass(String type, String id){
        String checkExt = firstV.classesLookup(id);
        if(checkExt != null && !checkExt.equals("")){
            while(true){
                if(checkExt.lastIndexOf("::") != -1){
                    String checkTemp = checkExt.substring(checkExt.lastIndexOf("::"));
                    if(type.equals(checkTemp)){
                        return true;
                    }
                    checkExt = checkExt.substring(0, checkExt.lastIndexOf("::"));
                }else{
                    break;
                }
            }
        }
        if(type.equals(checkExt)){
            return true;
        }
        return false;
    }

    public boolean hasSameParametersWithSuper(String id, String scope, ArrayList<String> currParams){
        while(true){
            if(scope.lastIndexOf("::") != -1){
                scope = scope.substring(0, scope.lastIndexOf("::"));
                if(firstV.functions.containsKey(id)){
                    if(firstV.functions.get(id).containsKey(scope)){
                        ArrayList<String> params = firstV.functions.get(id).get(scope);

                        if(currParams.size() == params.size()){
                            for(int i=0;i<currParams.size();i++){
                                if(currParams.get(i) != params.get(i)){
                                    return false;
                                }
                            }
                            
                            return true;
                        }

                    }else{
                        return true;
                    }

                }else{
                    return true;
                }
            }else{
                return true;
            }
        }
    }

    public boolean typeExists(String type){
        if(type.equals("int") || type.equals("bool") || type.equals("intArray") || type.equals("boolArray") || firstV.classesLookup(type) != null){
            return true;
        }
        return false;
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
        String cID = n.f1.accept(this, null);
        n.f14.accept(this, null);
//        System.out.println(firstV.classes.get(cID)+cID);
        n.f15.accept(this, firstV.classes.get(cID)+cID+"::main");
        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    public String visit(VarDeclaration n, String argu) throws Exception {
        String type =n.f0.accept(this, null);
        if(!typeExists(type)){
            throw new Exception("error type: "+type+" isnt a valid type");
        }
        return null;
    }

    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public String visit(TypeDeclaration n, String argu) throws Exception {
        return n.f0.accept(this, argu);
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
        String cID = n.f1.accept(this, null);
        n.f3.accept(this, null);
        n.f4.accept(this, cID);
        return null;
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
        String cID = n.f1.accept(this, null);
        String cExID = n.f3.accept(this, null);
        n.f5.accept(this, null);
        n.f6.accept(this, firstV.classesLookup(cExID)+"::"+cID);
        return null;
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
        String type = n.f1.accept(this, null);
        if(!typeExists(type)){
            throw new Exception("error type: "+type+" isnt a valid type");
        }
        String id = n.f2.accept(this, null);

        if(argu.lastIndexOf("::") != -1){
            String tempType = firstV.lookUp(id, argu.substring(0, argu.lastIndexOf("::")), 1);
            if(tempType != null && !tempType.equals(type)){
                throw new Exception("error method declaration has diffrent return type from super");
            }
        }

        n.f4.accept(this, argu+"::"+id);
        n.f7.accept(this, null);
        n.f8.accept(this, argu+"::"+id);
        String ReType = n.f10.accept(this, argu+"::"+id);
        String actualRetType = firstV.lookUp(id, argu+"::"+id, 1);
        if(!ReType.equals(actualRetType)){
            throw new Exception("error method declaration: "+id+" wrong return type, given "+ReType+" needed "+actualRetType);
        }
        return _ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterList n, String argu) throws Exception {
        String id = argu.substring(argu.lastIndexOf("::")+2, argu.length());
        String path = argu.substring(0, argu.lastIndexOf("::"));

        ArrayList<String> nodes = new ArrayList<String>();
        String curr = n.f0.accept(this, argu);
        nodes.add(curr);
        for(Node i : n.f1.f0.nodes){
            curr = i.accept(this, argu);
            nodes.add(curr);

        }
        if(!hasSameParametersWithSuper(id, path, nodes)){
            throw new Exception("error method is overloading a super method with diffrent parameters");
        }

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, String argu) throws Exception {
        String type = n.f0.accept(this, null);
        if(!typeExists(type)){
            throw new Exception("error type: "+type+" isnt a valid type");
        }
        return type;
    }

    /**
     * f0 -> "boolean"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(BooleanArrayType n, String argu) throws Exception {
        return "boolArray";
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(IntegerArrayType n, String argu) throws Exception {
        return "intArray";
    }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n, String argu) throws Exception {
        return "bool";
    }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n, String argu) throws Exception {
        return "int";
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
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, String argu) throws Exception {
        String type = n.f0.accept(this, argu);
        // String type = firstV.lookUp(id, argu);
        String temp = n.f2.accept(this, argu);
       if(!type.equals(temp)){
           if(isSubClass(type, temp)){
               return type;
           }
           throw new Exception("wrong assign value to identifier of type "+ type);
       }else{
           return type;
       }
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
    String type = n.f0.accept(this, argu);
    String iterator = n.f2.accept(this, argu);
    String assignment = n.f5.accept(this, argu);
    if(type.contains("Array")){
        if(iterator.equals("int")){
            if(assignment.equals("int")){
                if(type.contains("int")){
                    return "int";
                }else{
                    throw new Exception("error trying to assign data into an array ");
                }
            }else if(assignment.equals("bool")){
                if(type.contains("bool")){
                    return "bool";
                }else{
                    throw new Exception("error trying to assign data into an array ");
                }
            }else{
                throw new Exception("error array assignment dont know what type the array is");
            }
        }else{
            throw new Exception("error arrays can be iterated only with integers");
        }
    }else{
        throw new Exception("error identifier is not an array");
    }
    
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
        String condType = n.f2.accept(this, argu);
        if(!condType.equals("bool")){
            throw new Exception("error if condition must be boolean");
        }
        n.f4.accept(this, argu);
        n.f6.accept(this, argu);
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
        String condType = n.f2.accept(this, argu);
        if(!condType.equals("bool")){
            throw new Exception("error while condition must be boolean");
        }
        n.f4.accept(this, argu);
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
    String expr = n.f2.accept(this, argu);
    if(!expr.equals("int")){
        throw new Exception("error print statement accepts only integers");
    }
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
        String typeOne = n.f0.accept(this, argu);
        String typeTwo = n.f2.accept(this, argu);
        
        if(typeOne.equals("bool") && typeTwo.equals("bool")){
            return "bool";
        }else{
            throw new Exception("error (&&) expression");
        }
    }

    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    public String visit(Clause n, String argu) throws Exception {
        String temp = n.f0.accept(this, argu);
        if(temp.equals("this")){
            return "this";
        }
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    public String visit(NotExpression n, String argu) throws Exception {
        return n.f1.accept(this, argu);
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
        String temp = n.f0.accept(this, argu);
        if(temp.equals("this")){
            String p = firstV.classesLookup(argu);
            if(p.lastIndexOf("::") != -1){
                return p.substring(p.lastIndexOf("::"));
            }else{
                return p;
            }
        }
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String argu) throws Exception {
        String typeOne = n.f0.accept(this, argu);
        String typeTwo = n.f2.accept(this, argu);
       
        if(typeOne.equals("int") && typeTwo.equals("int")){
            return "bool";
        }else{
            throw new Exception("error (<) expression");
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception {
        String typeOne = n.f0.accept(this, argu);
        String typeTwo = n.f2.accept(this, argu);

        if(typeOne.equals("int") && typeTwo.equals("int")){
            return "int";
        }else{
            throw new Exception("error (+) expression");
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String argu) throws Exception {
        String typeOne = n.f0.accept(this, argu);
        String typeTwo = n.f2.accept(this, argu);
        
        if(typeOne.equals("int") && typeTwo.equals("int")){
            return "int";
        }else{
            throw new Exception("error (-) expression");
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String argu) throws Exception {
        String typeOne = n.f0.accept(this, argu);
        String typeTwo = n.f2.accept(this, argu);

        if(typeOne.equals("int") && typeTwo.equals("int")){
            return "int";
        }else{
            throw new Exception("error (*) expression");
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, String argu) throws Exception {
        String typeOne = n.f0.accept(this, argu);
        String typeTwo = n.f2.accept(this, argu);
        
        if(typeOne.contains("Array")){
            if(typeTwo.equals("int")){
                return typeOne.substring(0, typeOne.indexOf("Array"));
            }else {
                throw new Exception("error array iteration must be with integer");
            }
        }else{
            throw new Exception("error "+typeOne+" is not an array");
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String argu) throws Exception {
        String typeOne = n.f0.accept(this, argu);
        
        if(typeOne.contains("Array")){
            return "int";
        }else{
            throw new Exception("error "+typeOne+" is not an array");
        }
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
        String idType = n.f0.accept(this, argu);
        if(!idType.equals("int") && !idType.equals("bool") && !idType.equals("boolArray") && !idType.equals("intArray")){
            String idHistory;
            if(idType.equals("this")){
                idHistory = firstV.classesLookup(argu);
            }else{
                idHistory = firstV.classesLookup(idType);
            }
            String funcID = n.f2.accept(this, null);
            String funcReturnType = firstV.lookUp(funcID, idHistory, 1);
            if(funcReturnType == null){
                throw new Exception("error class has no method named "+funcID);
            }
            n.f4.accept(this, idHistory+"::"+funcID+"/+/"+argu);
            return funcReturnType;
        }else{
            throw new Exception("error identifier dosent have functions to call");
        }

    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String argu) throws Exception {
        String funcArg = argu.substring(0, argu.lastIndexOf("/+/"));
        String paramsArg = argu.substring(argu.lastIndexOf("/+/")+3);

        int temp = funcArg.lastIndexOf("::");
        String funcClass = funcArg.substring(0, temp);
        String funcID = funcArg.substring(temp+2, funcArg.length());
        ArrayList<String> actualParams = firstV.functions.get(funcID).get(funcClass);

        ArrayList<String> params = new ArrayList<String>();
        params.add(n.f0.accept(this, paramsArg));
        for(Node i : n.f1.f0.nodes){
            params.add(i.accept(this, paramsArg));
        }

        if(actualParams.size() == params.size()){
            for(int i=0;i<actualParams.size();i++){
                if(!actualParams.get(i).equals(params.get(i)) ){
                    if(!isSubClass(actualParams.get(i), params.get(i))){
                        throw new Exception("wrong type parameters given for "+ funcID);
                    }
                }
            }
        }else{
            throw new Exception("wrong type parameters given for "+ funcID);
        }
        return null;
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
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n, String argu) throws Exception {
        return "int";
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n, String argu) throws Exception {
        return "bool";
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n, String argu) throws Exception {
        return "bool";
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, String argu) throws Exception {
        if(argu != null){
            String temp = firstV.lookUp(n.f0.tokenImage, argu, 0);//TODO: check j here
            if(temp == null){
                throw new Exception("cant find variable "+ n.f0.tokenImage+" in the scope "+argu);
            }
            return temp;
        }
        return n.f0.tokenImage;
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, String argu) throws Exception {
        return "this";
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
        String expre = n.f3.accept(this, argu);
        if(!expre.equals("int")){
            throw new Exception("error array size not an integer");
        }
        return "boolArray";
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(IntegerArrayAllocationExpression n, String argu) throws Exception {
        String expre = n.f3.accept(this, argu);
        if(!expre.equals("int")){
            throw new Exception("error array size not an integer");
        }
        return "intArray";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, String argu) throws Exception {
        return n.f1.accept(this, null);
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