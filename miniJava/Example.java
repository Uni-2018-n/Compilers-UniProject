class Example {
    public static void main(String[] args) {
        A test;
        // int second;
        // boolean third;
        // third = true;

        // test = new A();
        // second = new int[5];
        // second[0] = 5+5;

        // test = new A();
        // second = test.foo(5, 5);
        // second = test.bar();
        // second = 1-true;
        // second = 1*1;
    }
}

class A {
    int i;
   B a;

    public int foo(int i, int j) {
        B k;
        k= new B();
        i = k.foobar(a);
        return i+j;
    }
   public int bar(){ return i; }
}

class B extends A {
   int i;

   public int foo(int i, int j) {
    //    int k;
    //    k = this.a.bar();
       return i+j;
   }
   public int foobar(A k){ 
    //    int p;
    //    p = this.bar();
    //    p = this.foo(5,5);
       return 1; 
    }
}
