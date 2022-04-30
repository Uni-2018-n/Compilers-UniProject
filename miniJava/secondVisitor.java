import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.ArrayList;
import java.util.HashMap;


public class secondVisitor extends GJDepthFirst<String, String> {
    firstVisitor firstV;
    public secondVisitor(firstVisitor f){
        super();
        firstV = f;
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
//        System.out.println(firstV.classes.get(cID)+cID);
        n.f15.accept(this, firstV.classes.get(cID)+cID+"::main");
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
        String cID = n.f1.accept(this, argu);
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
        String cID = n.f1.accept(this, argu);
        String cExID = n.f3.accept(this, argu);
        n.f6.accept(this, cExID+"::"+cID);
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
        String id = n.f2.accept(this, argu);
//        n.f8.accept(this, argu);
        String ReType = n.f10.accept(this, argu+"::"+id);
        String actualRetType = firstV.lookUp(id, argu+"::"+id);
//        System.out.println(ReType+"__"+actualRetType);
        if(!ReType.equals(actualRetType)){
            System.err.println("error method declaration: "+id+" wrong return type");
        }
        return _ret;
    }


    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n, String argu) throws Exception {
        String id = n.f0.accept(this, argu);
        String type = firstV.lookUp(id, argu);
        String temp = n.f2.accept(this, argu);
        System.out.println(type+"__"+temp);
//        if(!type.equals(n.f2.accept(this, argu))){
//            System.err.println("wrong assign value to "+id+" of type "+ type);
//        }else{
//            return type;
//        }
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
        String One = n.f0.accept(this, argu);
        String Two = n.f2.accept(this, argu);
        String typeOne;
        String typeTwo;
        if(!One.equals("int") && !One.equals("bool")){
            typeOne = firstV.lookUp(One, argu);
            if(typeOne == null){
                System.err.println("error "+One+" not found");
                throw new Exception("error "+One+" not found");
            }
        }else{
            typeOne = One;
        }

        if(!Two.equals("int") && !Two.equals("bool")){
            typeTwo = firstV.lookUp(Two, argu);
            if(typeTwo == null){
                System.err.println("error "+Two+" not found");
                throw new Exception("error "+Two+" not found");
            }
        }else{
            typeTwo = Two;
        }

        if(typeOne.equals("bool") && typeTwo.equals("bool")){
            return "bool";
        }else{
            System.err.println("error (&&) expression");
        }
        return null;
    }

    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    public String visit(Clause n, String argu) throws Exception {
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
     *       | ThisExpression()  --:TODO
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
    public String visit(PrimaryExpression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String argu) throws Exception {
        String One = n.f0.accept(this, argu);
        String Two = n.f2.accept(this, argu);
        String typeOne;
        String typeTwo;
        if(!One.equals("int") && !One.equals("bool")){
            typeOne = firstV.lookUp(One, argu);
            if(typeOne == null){
                System.err.println("error "+One+" not found");
                throw new Exception("error "+One+" not found");
            }
        }else{
            typeOne = One;
        }

        if(!Two.equals("int") && !Two.equals("bool")){
            typeTwo = firstV.lookUp(Two, argu);
            if(typeTwo == null){
                System.err.println("error "+Two+" not found");
                throw new Exception("error "+Two+" not found");
            }
        }else{
            typeTwo = Two;
        }

        if(typeOne.equals("int") && typeTwo.equals("int")){
            return "bool";
        }else{
            System.err.println("error (<) expression");
        }
        return null;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception {
        String One = n.f0.accept(this, argu);
        String Two = n.f2.accept(this, argu);
        String typeOne;
        String typeTwo;
        if(!One.equals("int") && !One.equals("bool")){
            typeOne = firstV.lookUp(One, argu);
            if(typeOne == null){
                System.err.println("error "+One+" not found");
                throw new Exception("error "+One+" not found");
            }
        }else{
            typeOne = One;
        }

        if(!Two.equals("int") && !Two.equals("bool")){
            typeTwo = firstV.lookUp(Two, argu);
            if(typeTwo == null){
                System.err.println("error "+Two+" not found");
                throw new Exception("error "+Two+" not found");
            }
        }else{
            typeTwo = Two;
        }

        if(typeOne.equals("int") && typeTwo.equals("int")){
            return "int";
        }else{
            System.err.println("error (+) expression");
        }
        return null;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String argu) throws Exception {
        String One = n.f0.accept(this, argu);
        String Two = n.f2.accept(this, argu);
        String typeOne;
        String typeTwo;
        if(!One.equals("int") && !One.equals("bool")){
            typeOne = firstV.lookUp(One, argu);
            if(typeOne == null){
                System.err.println("error "+One+" not found");
                throw new Exception("error "+One+" not found");
            }
        }else{
            typeOne = One;
        }

        if(!Two.equals("int") && !Two.equals("bool")){
            typeTwo = firstV.lookUp(Two, argu);
            if(typeTwo == null){
                System.err.println("error "+Two+" not found");
                throw new Exception("error "+Two+" not found");
            }
        }else{
            typeTwo = Two;
        }

        if(typeOne.equals("int") && typeTwo.equals("int")){
            return "int";
        }else{
            System.err.println("error (+) expression");
        }
        return null;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String argu) throws Exception {
        String One = n.f0.accept(this, argu);
        String Two = n.f2.accept(this, argu);
        String typeOne;
        String typeTwo;
        if(!One.equals("int") && !One.equals("bool")){
            typeOne = firstV.lookUp(One, argu);
            if(typeOne == null){
                System.err.println("error "+One+" not found");
                throw new Exception("error "+One+" not found");
            }
        }else{
            typeOne = One;
        }

        if(!Two.equals("int") && !Two.equals("bool")){
            typeTwo = firstV.lookUp(Two, argu);
            if(typeTwo == null){
                System.err.println("error "+Two+" not found");
                throw new Exception("error "+Two+" not found");
            }
        }else{
            typeTwo = Two;
        }

        if(typeOne.equals("int") && typeTwo.equals("int")){
            return "int";
        }else{
            System.err.println("error (+) expression");
        }
        return null;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, String argu) throws Exception {
        String One = n.f0.accept(this, argu);
        String Two = n.f2.accept(this, argu);
        String typeOne;
        String typeTwo;
        if(!One.equals("int") && !One.equals("bool")){
            typeOne = firstV.lookUp(One, argu);
            if(typeOne == null){
                System.err.println("error "+One+" not found");
                throw new Exception("error "+One+" not found");
            }
        }else{
            typeOne = One;
        }

        if(!Two.equals("int") && !Two.equals("bool")){
            typeTwo = firstV.lookUp(Two, argu);
            if(typeTwo == null){
                System.err.println("error "+Two+" not found");
                throw new Exception("error "+Two+" not found");
            }
        }else{
            typeTwo = Two;
        }
        if(typeOne.contains("Array")){
            if(typeTwo.equals("int")){
                return typeOne.substring(0, typeOne.indexOf("Array"));
            }else {
                System.err.println("error array iteration must be with integer");
            }
        }else{
            System.err.println("error "+One+" is not an array");
        }
        return null;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String argu) throws Exception {
        String One = n.f0.accept(this, argu);
        String typeOne;
        if(!One.equals("int") && !One.equals("bool")){
            typeOne = firstV.lookUp(One, argu);
            if(typeOne == null){
                System.err.println("error "+One+" not found");
                throw new Exception("error "+One+" not found");
            }
        }else{
            typeOne = One;
        }

        if(typeOne.contains("Array")){
            return "int";
        }else{
            System.err.println("error "+One+" is not an array");
        }
        return null;
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
        String id = n.f0.accept(this, argu);
        String funcID = n.f2.accept(this, argu);

        String idType = firstV.lookUp(id, argu);
        if(!idType.equals("int") && !idType.equals("bool")){
            String idHistory = firstV.classes.get(idType);
            String funcReturnType = firstV.lookUp(funcID, idHistory);
            n.f4.accept(this, idHistory+"::"+funcID);
            return funcReturnType;
        }else{
            System.err.println("error " + id+" dosent have functions to call");
        }

        return null;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String argu) throws Exception {
        int temp = argu.lastIndexOf("::");
        String funcClass = argu.substring(0, temp);
        String funcID = argu.substring(temp+2, argu.length());
        ArrayList<String> actualParams = firstV.functions.get(funcID).get(funcClass);

        ArrayList<String> params = new ArrayList<String>();
        params.add(n.f0.accept(this, null));
        for(Node i : n.f1.f0.nodes){
            params.add(i.accept(this, argu));
        }

        if(actualParams.size() == params.size()){
            for(int i=0;i<actualParams.size();i++){
                if(!actualParams.get(i).equals(params.get(i)) ){
                    System.err.println("wrong type parameters given for "+ funcID);
                    break;
                }
            }
        }else{
            System.err.println("wrong type parameters given for "+ funcID);
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
        return n.f0.tokenImage;
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
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
        return "bool";
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    public String visit(IntegerArrayAllocationExpression n, String argu) throws Exception {
        return "int";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, String argu) throws Exception {
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return _ret;
    }
}