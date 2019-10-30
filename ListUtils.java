import java.util.ArrayList;
import java.util.List;

public class ListUtils
{
    public static int[][] toInt2Array(ArrayList<int[]>data)
    {
        int[][] arr = new int[data.size()][(data.get(0).length)];
        for(int i=0;i<arr.length;i++)
        {
            arr[i] = data.get(i);
        }
        return arr;
    }

    public static float[][] toFloat2Array(List<float[]> data)
    {
        float[][] arr = new float[data.size()][(data.get(0).length)];
        for(int i=0;i<arr.length;i++)
        {
            arr[i] = data.get(i);
        }
        return arr;
    }

    public static float[] toFloatArray(List<float[]> data)
    {
        float[] arr = new float[data.size() * (data.get(0).length)];
        for(int i=0;i<data.size();i++)
        {
            arr[2* i] = data.get(i)[0];
            arr[2*i+1] = data.get(i)[1];
        }
        return arr;
    }
}
