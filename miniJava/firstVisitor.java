import syntaxtree.*;
import visitor.GJDepthFirst;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


class pair{
    LinkedList<offsetItem> vars;
    LinkedList<offsetItem> functions;
    public pair(){
        vars = new LinkedList<offsetItem>();
        functions = new LinkedList<offsetItem>();
    }
    public void push(String i, int o, int j, int initialValue){
        if(j == 0){
            if(vars.size() == 0){
                vars.add(new offsetItem(i, initialValue, initialValue+o));
            }else{
                vars.add(new offsetItem(i, vars.getLast().size, vars.getLast().size+o));
            }
        }else{
            if(functions.size() == 0){
                functions.add(new offsetItem(i, initialValue, initialValue+o));
            }else{
                functions.add(new offsetItem(i, functions.getLast().size, functions.getLast().size+o));
            }
        }
    }

}
class offsetItem {
    String id;
    int myOffset;
    int size;

    public offsetItem(String i, int o, int s){
        id = i;
        myOffset = o;
        size = s;
//        System.out.println(myOffset + " "+ size);
    }
}

public class firstVisitor extends GJDepthFirst<String, String> {
    //    HashMap<Pair<String, String>, String> fields = new HashMap<new Pair<String, String>, String> ();
    //className -> fieldName -> type
    HashMap<String, HashMap<String, List<String>>> fields  = new HashMap<String, HashMap<String, List<String>>>();
    HashMap<String, String>                  classes = new HashMap<String, String>();

    HashMap<String, HashMap<String, ArrayList<String>>> functions  = new HashMap<String, HashMap<String, ArrayList<String>>>();

    LinkedHashMap<String, pair> offsets = new LinkedHashMap<String, pair>();



    public String lookUp(String id, String scope, int j){
        String tempScope = scope;
        if(fields.containsKey(id)){
            while(true){
                if(fields.get(id).containsKey(tempScope)){
                    return fields.get(id).get(tempScope).get(j);
                }else{
                    int temp = tempScope.lastIndexOf("::");
                    if(temp == -1){
                        return null;
                    }
                    
                    tempScope = tempScope.substring(0, temp);
                }
            }
        }
        return null;
    }

    public String classesLookup(String id){
        while(true){
            if(id.lastIndexOf("::") != -1){
                String temp = id.substring(id.lastIndexOf("::"));
                if(classes.containsKey(temp)){
                    if(!classes.get(temp).equals("")){
                        return classes.get(temp)+"::"+temp;
                    }else{
                        return temp;
                    }
                }
                id=id.substring(0, id.lastIndexOf("::"));
            }else{
                if(classes.containsKey(id)){
                    if(!classes.get(id).equals("")){
                        return classes.get(id)+"::"+id;
                    }else{
                        return id;
                    }
                }else{
                    return null;
                }
            }
        }
    }

    public void offsetPush(String id, String className, String type, int j, int initialValue){
        if(j==1){
            offsets.get(className).push(id, 8, j, initialValue);
            return;
        }
        switch (type){
            case "int":
                offsets.get(className).push(id, 4, j, initialValue);
                return;
            case "bool":
                offsets.get(className).push(id, 1, j, initialValue);
                return;
            default:
                offsets.get(className).push(id, 8, j, initialValue);
        }
    }

//    public void offsetPrint(String file){
//        try{
//            PrintWriter temp = new PrintWriter("../examples/output_my/"+file.substring(file.lastIndexOf('/'), file.lastIndexOf('.'))+".txt", "UTF-8");
//            for(Map.Entry<String, pair>ite : offsets.entrySet()){
//                temp.println("-----------Class "+ite.getKey()+"-----------");
//                temp.println("--Variables---");
//                for(int i=0;i<ite.getValue().vars.size();i++){
//                    temp.println(ite.getKey()+"."+ite.getValue().vars.get(i).id+" : "+ite.getValue().vars.get(i).myOffset);
//                }
//                temp.println("---Methods---");
//                for(int i=0;i<ite.getValue().functions.size();i++){
//                    temp.println(ite.getKey()+"."+ite.getValue().functions.get(i).id+" : "+ite.getValue().functions.get(i).myOffset);
//                }
//                temp.println();
//            }
//            temp.close();
//
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//    }
    public void offsetPrint(){
        for(Map.Entry<String, pair>ite : offsets.entrySet()){
            System.out.println("-----------Class "+ite.getKey()+"-----------");
            System.out.println("--Variables---");
            for(int i=0;i<ite.getValue().vars.size();i++){
                System.out.println(ite.getKey()+"."+ite.getValue().vars.get(i).id+" : "+ite.getValue().vars.get(i).myOffset);
            }
            System.out.println("---Methods---");
            for(int i=0;i<ite.getValue().functions.size();i++){
                System.out.println(ite.getKey()+"."+ite.getValue().functions.get(i).id+" : "+ite.getValue().functions.get(i).myOffset);
            }
            System.out.println();
        }
    }

    public void insertField(String id, String scope, String type, int j) throws Exception{
        if(!fields.containsKey(id)){
            fields.put(id, new HashMap<String, List<String>>());
            fields.get(id).put(scope, Arrays.asList(new String[2]));
            fields.get(id).get(scope).set(j, type);
        }else{
            if(fields.get(id).containsKey(scope)){
                if(j == 0){
                    if(fields.get(id).get(scope).get(0) != null){
                        throw new Exception("error multiple same-id variables");
                    }else{

                        fields.get(id).get(scope).set(0, type);
                    }
                }else{
                    if(fields.get(id).get(scope).get(1) != null){
                        throw new Exception("error multiple same-id methods");
                    }else{
                        fields.get(id).get(scope).set(1, type);
                    }
                }
            }else{
                fields.get(id).put(scope, Arrays.asList(new String[2]));
                fields.get(id).get(scope).set(j, type);
            }
        }

        String className;
        String tempScope = scope;
        while(true){
            if(scope.lastIndexOf("::") != -1){
                className = tempScope.substring(tempScope.lastIndexOf("::")+2);
                tempScope = tempScope.substring(tempScope.lastIndexOf("::")+2);
            }else{
                className = tempScope;
            }
            if(classes.containsKey(className)){
                if(j==1){
                    if(hasSuper(id, scope)){
                        return;
                    }
                }
                int initialOffset;
                if(scope.lastIndexOf("::") == -1){
                    initialOffset = 0;
                }else{
                    while(true){
                        if(scope.lastIndexOf("::") == -1){
                            initialOffset = getLastFromSuper(scope, j);
                            break;
                        }
                        String temp = scope.substring(scope.lastIndexOf("::")+2);
                        scope = scope.substring(0, scope.lastIndexOf("::"));
                        if(!temp.equals(className)){
                            initialOffset = getLastFromSuper(temp, j);
                            break;
                        }
                    }
                }
//                System.out.println(initialOffset);
                offsetPush(id, className, type, j, initialOffset);
                return;
            }else{
                if(tempScope.lastIndexOf("::") == -1){
                    return;
                }
            }
        }
    }

    public int getLastFromSuper(String className, int j){
        if(offsets.containsKey(className)){
            if(j==0){
                if(offsets.get(className).vars.size() != 0){
                    return (offsets.get(className).vars.getLast().size);
                }else{
                    return 0;
                }
            }else{
                if(offsets.get(className).functions.size() != 0){
                    return offsets.get(className).functions.getLast().size;
                }else{
                    return 0;
                }
            }
        }
        return 0;
    }

    public boolean hasSuper(String id, String scope){
        while(true){
            if(scope.lastIndexOf("::") != -1){
                scope = scope.substring(0, scope.lastIndexOf("::"));
                if(functions.containsKey(id)){
                    if(functions.get(id).containsKey(scope)){
                        return true;
                    }
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }
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
     * f11 -> Identifier() /+
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )* /+
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public String visit(MainClass n, String argu) throws Exception {
        String cID = n.f1.accept(this, null);
        String paramID = n.f11.accept(this, null);
        classes.put(cID, "");
        insertField(paramID, cID+"::main", "stringArray", 0);
        n.f14.accept(this, cID+"::main");
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
     * f3 -> ( VarDeclaration() )* /+
     * f4 -> ( MethodDeclaration() )* /+
     * f5 -> "}"
     */
    public String visit(ClassDeclaration n, String argu) throws Exception {
        String cID = n.f1.accept(this, null);
        if(classes.containsKey(cID)){
            throw new Exception("multiple declaration of same class");
        }
        classes.put(cID, "");
        offsets.put(cID, new pair());
        n.f3.accept(this, cID);
        n.f4.accept(this, cID);
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )* /+
     * f6 -> ( MethodDeclaration() )* /+
     * f7 -> "}"
     */
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        String cID = n.f1.accept(this, null);
        String cExID = n.f3.accept(this, null);
        offsets.put(cID, new pair());
        if(classes.containsKey(cID)){
            throw new Exception("multiple declaration of same class");
        }
        if(classes.containsKey(cExID)){
            if(!classes.get(cExID).equals("")){
                classes.put(cID, classes.get(cExID)+"::"+cExID);
            }else{
                classes.put(cID, cExID);
            }
        }else{
            throw new Exception("class extends undeclared class, classes must be declared in correct order");
        }
        n.f5.accept(this, classes.get(cID)+"::"+cID);
        n.f6.accept(this, classes.get(cID)+"::"+cID);
        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n, String argu) throws Exception {
        String type = n.f0.accept(this, null);
        String id= n.f1.accept(this, null);
        insertField(id, argu, type, 0);

        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )? /+
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )* /+
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public String visit(MethodDeclaration n, String argu) throws Exception {
        String type = n.f1.accept(this, null);
        String id = n.f2.accept(this, null);
        if(n.f4.accept(this, argu+"::"+id) == null){
            if(functions.containsKey(id)){
                if(functions.get(id).containsKey(argu)){
                    throw new Exception("error multiple same-id methods");
                }else{
                    functions.get(id).put(argu, new ArrayList<String>());
                }
            }else{
                functions.put(id, new HashMap<>());
                functions.get(id).put(argu, new ArrayList<String>());
            }
        }
        insertField(id, argu, type, 1);

        n.f7.accept(this, argu+"::"+id);
//        n.f8.accept(this, argu);
//        n.f10.accept(this, argu);
        return null;
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


        if(functions.containsKey(id)){
            if(functions.get(id).containsKey(path)){
                throw new Exception("error multiple same-id methods");
            }else{
                functions.get(id).put(path, nodes);
            }
        }else{
            functions.put(id, new HashMap<>());
            functions.get(id).put(path, nodes);
        }


        return "i did my job";
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, String argu) throws Exception {
        String type = n.f0.accept(this, null);
        String id = n.f1.accept(this, null);
        insertField(id, argu, type, 0);
        return type;
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
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n, String argu) throws Exception {
        return n.f0.tokenImage;
    }

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    public String visit(Type n, String argu) throws Exception {
        return n.f0.accept(this, argu);
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
}