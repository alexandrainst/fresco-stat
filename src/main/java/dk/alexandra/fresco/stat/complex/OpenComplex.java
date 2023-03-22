package dk.alexandra.fresco.stat.complex;

import dk.alexandra.fresco.framework.DRes;

public class OpenComplex implements DRes<OpenComplex> {
    public final double a, b;

    public OpenComplex(double a, double b) {
            this.a = a;
            this.b = b;
        }

    @Override
    public OpenComplex out() {
        return this;
    }

    public String toString() {
        return a + " + " + b + "i";
    }
}
