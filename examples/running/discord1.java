class Example {
    public static void main(String[] args) {
        int a;
        a = 3;
    }
}

class A {
    int i;
    A a;
    int c;
    boolean[] arr;
    public A get(){
        return a;
    }
    public int fooo(int i, int j, A a1) {
        boolean d;
        arr = new boolean[10];
        d = (new boolean[3])[3];
        // c=a.fooo(i,j);
        c=arr.length;
        d = arr[(this.fooo(i, j, a1))];
        return i + ((i * j) + (this.fooo(i, j, a1)));
    }

    public int f(int k) {
        c = this.f(k);
        return this.f(k);
    }

    public boolean bar(int k, int d) {
        int p;
        return false;
    }
}

class B extends A {
    int i;
    int k;

    public int foobar(boolean k) {
        return (this.get()).f(i);
    }

}

class C {
    int i;
    B b;
    boolean d;
    int k;
    A a;

    public int fooo(int i, int j, A a1) {
        a = new B();
        if ((a.bar(i, j)) && (!(3 < 2)))
            k = 1;
        else
            k = 2;
        i = a.fooo(i, this.fooo(i, this.fooo(i, j, a1), a1), b);
        return 1;
    }

    public int test(int i, int j) {
        i = 1;
        return 2;
    }
}

