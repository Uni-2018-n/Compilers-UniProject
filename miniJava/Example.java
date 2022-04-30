class Example {
    public static void main(String[] args) {
        A test;
        int second;

        test = new A();
        second = test.foo(5, 5);
    }
}

class A {
    int i;
//    A a;

    public int foo(int i, int j) {
        return i+j;
    }
//    public int bar(){ return 1; }
}

//class B extends A {
////    int i;
//
//    public int foo(int i, int j) {
//        int k;
//        return i+j;
//    }
////    public int foobar(boolean k){ return 1; }
//}
