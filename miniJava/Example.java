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

class A{
    int i;
    boolean flag;
    int j;
    public int foo() {return 5;}
    public boolean fa() {return true;}
}

class B extends A{
    A type;
    int k;
    public int foo() {return 5;}
    public boolean bla() {return true;}
}
class C{
}
