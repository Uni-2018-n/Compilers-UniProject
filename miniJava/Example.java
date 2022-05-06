class Example {
    public static void main(String[] x) {
//         C c;
         A a;
         B b;
         int i;
         a= new A();
         b = new B();
//         c= new C();
//         i = c.test(a);
         System.out.println(new A().B());
        // b = test.reset();
    }
}


class A {
    boolean data;
    boolean data2;
    public int B(){
        return 5;
    }
    public int E(){
        return 5;
    }
}

class B extends A {
    public int foo(int i){
        return 0;
    }
}

class C {
    public int bar(){return 0;}
    public int fbar(){return 0;}
    public int ffbar(){return 0;}
}