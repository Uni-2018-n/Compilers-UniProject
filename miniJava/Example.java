class Example {
    public static void main(String[] x) {
//         C c;
//         A a;
//         B b;
         int i;
//         a= new A();
//         b = new B();
//         c= new C();
//         i = c.test(a);
        // b = test.reset();
    }
}

class D {

}
class A {
    boolean data;
    boolean data2;
    public int foo(int i){
        return 5;
    }
//    public int E(){
//        return 5;
//    }
}


class B extends A {
//    public int E(int p){
//        return 5;
//    }
    public int foo(int i){
        return 0;
    }
}
class C extends D{
    public int foo(){return 0;}
}
