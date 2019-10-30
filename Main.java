import org.jblas.FloatMatrix;
import static java.lang.System.out;
import java.util.Arrays;


public class Main implements Runnable
{
    GameCore gameCore;
    Display display;

    public Main()
    {
        gameCore = new GameCore();
        display = new Display("Software Renderer", 256, 320);
        gameCore.init(display);
        display.gameCore = gameCore;
    }

    public static void main(String[]args)
    {
        float[][] dat = {{1,2,3},
                        {2,3,4},
                        {6,7,8}};

        System.setProperty("sun.java2d.opengl", "True");
        System.setProperty("sun.java2d.translaccel", "True");
        new Thread(new Main()).start();
    }

    public static void printm(FloatMatrix m)
    {
        for (int i = 0; i < m.getRows(); i++) {
            out.println(m.getRow(i));
        }
    }

    public void run()
    {
        // calculates how many miliseconds to wait for the next update
        int waitToUpdate = (1000/ 60);

        long startTime = System.nanoTime();

        while(gameCore.isLooping)
        {
            // is true when you update
            boolean shouldRender = false;

            // Finds the current time
            long currentTime = System.nanoTime();

            // Finds out how many updates are needed
            long updatesNeeded = (((currentTime-startTime) / 1000000))/ waitToUpdate;
            for(long x = display.updateCount; x< updatesNeeded; x++)
            {
                gameCore.update(display);
                shouldRender=true;
                display.updateCount++;
            }
            if(shouldRender)
                display.render(this.gameCore);
            // sleep so other threads have time to run
            try
            {
                Thread.sleep(5);
            }
            catch(Exception e)
            {
                System.out.println("Error sleeping in run method: " +e.getMessage());
            }
        }
    }
}
