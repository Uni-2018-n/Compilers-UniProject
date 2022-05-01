class Example {
    public static void main(String[] args) {
        A test;
        int[] second;
        boolean third;
        third = true;

        second = new int[5];
        second[0] = 5+5;

        // test = new A();
        // second = test.foo(5, 5);
        // second = test.bar();
        // second = 1-true;
        // second = 1*1;
    }
}

class A {
    int i;
   A a;

    public int foo(int i, int j) {
        return i+j;
    }
   public int bar(){ return i; }
}

// class B extends A {
// //    int i;

//    public int foo(int i, int j) {
//        int k;
//        return i+j;
//    }
// //    public int foobar(boolean k){ return 1; }
// }
