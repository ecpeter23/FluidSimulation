import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.OptionalDouble;
import java.util.stream.DoubleStream;


public class Panel extends JPanel implements Runnable{
    Thread thread;
    Fluid fluid;

    final int tileSize = 5;
    final double divider = 1d / tileSize;
    int visibleMapSizeX;
    int visibleMapSizeY;

    int FPS = 60; // FPS

    public Panel(int width, int height){
        this.setPreferredSize(new Dimension(width, height));
        visibleMapSizeX = (int)(width/(double)tileSize);
        visibleMapSizeY = (int)(height/(double)tileSize);

        fluid = new Fluid(100, 0.2d, 0.2d, 0);

        this.setBackground(Color.white);
        this.setDoubleBuffered(true);
        MouseAdapter adapter = new MouseAdapter() {
            int previouseX;
            int previouseY;

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                fluid.addDensity((int)(e.getX() * divider), (int)(e.getY() * divider), 0.5d);

                previouseX = e.getX();
                previouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);

                fluid.addDensity((int)(e.getX() * divider), (int)(e.getY() * divider), 0.5d);

                float amountX = e.getX() - previouseX;
                float amountY = e.getY() - previouseY;
                fluid.addVelocity((int)(amountX * divider), (int)(amountY * divider), amountX, amountY);
            }
        };
        this.addMouseListener(adapter);
        this.addMouseMotionListener(adapter);
    }

    public void start(){
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000d/FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (thread != null){ // game loop
            fluid.step();

            repaint();

            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = clamp0(remainingTime / 1000000);

                Thread.sleep((long)remainingTime);

                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void paint(Graphics g){
        Graphics2D graphics = (Graphics2D) g;
        float scale = getNomalScale();

        for (int y = 0; y < visibleMapSizeY; y++){
            for (int x = 0; x < visibleMapSizeX; x++){
                float d = (float)fluid.density[index(x, y)] * scale;
                graphics.setColor(new Color(d, d, d));

                graphics.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }

        graphics.dispose();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    float getNomalScale(){
        OptionalDouble max = DoubleStream.of(fluid.density).max();
        return 1 / (float) max.getAsDouble();
    }

    int index(int x, int y){
        return x + y * 100;
    }

    private double clamp0(double value){
        if (value < 0){
            return 0;
        } else{
            return value;
        }
    }

}
