/*
Code Converted and Ported from Mike Ash "Fluid Simulation for Dummies"
https://mikeash.com/pyblog/fluid-simulation-for-dummies.html

Huge thanks to him and would highly recommend reading the original article
 */

public class Fluid {
    static int size;
    double dt; // time step
    double diffusion; // diffusion amount, controls how the velocity diffuses throughout the fluid
    double viscosity; // thickness of the fluid

    double[] density; // density
    double[] density0; // previous density

    double[] vX; // velocity x
    double[] vY; // velocity y

    double[] vX0; // previous velocity x
    double[] vY0; // previous velocity y

    Fluid(int size, double diffusion, double viscosity, double dt){
        this.size = size;
        this.diffusion = diffusion;
        this.viscosity = viscosity;
        this.dt = dt;

        this.density = new double[size * size];
        this.density0 = new double[size * size];

        this.vX = new double[size * size];
        this.vY = new double[size * size];

        this.vX0 = new double[size * size];
        this.vY0 = new double[size * size];

    }

    void step() {

        diffuse(1, vX0, vX, viscosity, dt, 4);
        diffuse(2, vY0, vY, viscosity, dt, 4);

        project(vX0, vY0, vX, vY, 4);

        advect(1, vX, vX0, vX0, vY0, dt);
        advect(2, vY, vY0, vX0, vY0, dt);

        project(vX, vY, vX0, vY0, 4);

        diffuse(0, density0, density, diffusion, dt, 4);
        advect(0, density, density0, vX, vY, dt);
    }

    void addDensity(int x, int y, double amount){
        this.density[index(x, y)] += amount;
    }

    void addVelocity(int x, int y, float amountX, float amountY){
        vX[index(x, y)] += amountX;
        vY[index(x, y)] += amountY;
    }

    void diffuse(int b, double[] vX, double[] vX0, double diffusion, double dt, int iterations){
        double a = dt * diffusion * (size - 2) * (size - 2);
        linearSolve(b, vX, vX0, a, 1 + 6 * a, iterations);
    }

    void setBounds(int b, double[] x)
    {
        for(int i = 1; i < size - 1; i++) {
            x[index(i, 0)] = b == 2 ? -x[index(i, 1)] : x[index(i, 1)];
            x[index(i, size-1)] = b == 2 ? -x[index(i, size-2)] : x[index(i, size-2)];
        }
        for(int j = 1; j < size - 1; j++) {
            x[index(0, j)] = b == 1 ? -x[index(1, j)] : x[index(1, j)];
            x[index(size-1, j)] = b == 1 ? -x[index(size-2, j)] : x[index(size-2, j)];
        }

        x[index(0, 0)] = 0.5d * (x[index(1, 0)]
                + x[index(0, 1)]);
        x[index(0, size-1)] = 0.5d * (x[index(1, size-1)]
                + x[index(0, size-2)]);
        x[index(size-1, 0)] = 0.5d * (x[index(size-2, 0)]
                + x[index(size-1, 1)]);
        x[index(size-1, size-1)]   = 0.5d * (x[index(size-2, size-1)]
                + x[index(size-1, size-2)]);
    }

    void linearSolve(int b, double[] vX, double[] vX0, double a, double c, int iterations)
    {
        double cRecip = 1d / c;
        for (int k = 0; k < iterations; k++) {
            for (int j = 1; j < size - 1; j++) {
                for (int i = 1; i < size - 1; i++) {
                    vX[index(i, j)] =
                            (vX0[index(i, j)]
                                    + a*(    vX[index(i+1, j)]
                                    +vX[index(i-1, j)]
                                    +vX[index(i, j+1)]
                                    +vX[index(i, j-1)]
                            )) * cRecip;
                }
            }
            setBounds(b, vX);
        }
    }

    void project(double[] vX, double[] vY, double[] p, double[] div, int iterations)
    {
        for (int j = 1; j < size - 1; j++) {
            for (int i = 1; i < size - 1; i++) {
                div[index(i, j)] = -0.5d*(
                        vX[index(i+1, j)]
                                -vX[index(i-1, j)]
                                +vY[index(i, j + 1)]
                                -vY[index(i, j - 1)]
                ) / size;
                p[index(i, j)] = 0;
            }
        }
        setBounds(0, div);
        setBounds(0, p);
        linearSolve(0, p, div, 1, 6, iterations);

        for (int j = 1; j < size - 1; j++) {
            for (int i = 1; i < size - 1; i++) {
                vX[index(i, j)] -= 0.5f * (  p[index(i+1, j)]
                        -p[index(i-1, j)]) * size;
                vY[index(i, j)] -= 0.5f * (  p[index(i, j+1)]
                        -p[index(i, j-1)]) * size;
            }
        }
        setBounds(1, vX);
        setBounds(2, vY);
    }

    void advect(int b, double[] d, double[] d0,  double[] vX, double[] vY, double dt)
    {
        double i0, i1, j0, j1;

        double dtx = dt * (size - 2);
        double dty = dt * (size - 2);

        double s0, s1, t0, t1;
        double tmp1, tmp2, x, y;

        double sizeDouble = size;
        double iDouble, jDouble;
        int i, j;

        for(j = 1, jDouble = 1; j < size - 1; j++, jDouble++) {
            for(i = 1, iDouble = 1; i < size - 1; i++, iDouble++) {
                tmp1 = dtx * vX[index(i, j)];
                tmp2 = dty * vY[index(i, j)];
                x    = iDouble - tmp1;
                y    = jDouble - tmp2;

                if(x < 0.5d) x = 0.5d;
                if(x > sizeDouble + 0.5d) x = sizeDouble + 0.5d;
                i0 = Math.floor(x);
                i1 = i0 + 1.0f;
                if(y < 0.5d) y = 0.5d;
                if(y > sizeDouble + 0.5d) y = sizeDouble + 0.5d;
                j0 = Math.floor(y);
                j1 = j0 + 1.0d;

                s1 = x - i0;
                s0 = 1.0d - s1;
                t1 = y - j0;
                t0 = 1.0d - t1;

                int i0i = (int)i0;
                int i1i = (int)i1;
                int j0i = (int)j0;
                int j1i = (int)j1;

                d[index(i, j)] =
                        s0 * (t0 * d0[index(i0i, j0i)]) + (t1 * d0[index(i0i, j1i)]) +
                        s1 * (t0 * d0[index(i1i, j0i)]) + (t1 * d0[index(i1i, j1i)]);
            }
        }

        setBounds(b, d);
    }

    private static int index(int x, int y){
        x = clamp(x, 0, size - 1);
        y = clamp(y, 0, size - 1);

        return x + y * size;
    }

    private static int clamp(int value, int min, int max){
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
