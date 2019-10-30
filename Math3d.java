import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Math3d
{
    public static final float L_CONST = 1;
    public static final float L_LINEAR = 0;
    public static final float L_EXP = 0;

    public static FloatMatrix projection(float a, float fov, float zfar, float znear)
    {
        fov = fov * (float)Math.PI / 180f;

        float top = znear * (float) Math.tan(fov/2);
        float bottom = -top;
        float right = top * a;
        float left = -right;

        return frustum(left, right, bottom, top, znear, zfar);
    }

    public static FloatMatrix frustum(float left, float right, float bottom, float top, float znear, float zfar)
    {
        FloatMatrix mat = FloatMatrix.eye(4);
        mat.put(0,0, (2*znear)/(right-left));
        mat.put(1,1, (2*znear)/(top-bottom));
        mat.put(0, 2, (right + left)/(right - left));
        mat.put(1, 2, (top + bottom)/(top-bottom));
        mat.put(2, 2, (znear + zfar)/(znear-zfar));
        mat.put(3, 2, -1.0f);
        mat.put(2, 3, (2*znear*zfar)/(znear-zfar));
        mat.put(3, 3, 0.0f);
        return mat;
    }

    public static FloatMatrix translation(float x, float y, float z)
    {
        FloatMatrix mat = FloatMatrix.eye(4);
        mat.put(0, 3, x);
        mat.put(1, 3, y);
        mat.put(2, 3, z);
        return mat;
    }

    public static FloatMatrix rotation(float x, float y, float z)
    {
        FloatMatrix matx = FloatMatrix.eye(4);
        FloatMatrix maty = FloatMatrix.eye(4);
        FloatMatrix matz = FloatMatrix.eye(4);

        matx.put(1, 1, (float)Math.cos(x));
        matx.put(1, 2,(float)Math.sin(x));
        matx.put(2, 1, (float)-Math.sin(x));
        matx.put(2, 2, (float)Math.cos(x));

        maty.put(0,0,(float)Math.cos(y));
        maty.put(0, 2, -(float)Math.sin(y));
        maty.put(2, 0, (float)Math.sin(y));
        maty.put(2, 2, (float)Math.cos(y));

        matz.put(0,0,(float)Math.cos(z));
        matz.put(0,1,(float)-Math.sin(z));
        matz.put(1,0,(float)Math.sin(z));
        matz.put(1,1,(float)Math.cos(z));

        return matx.mmul(maty).mmul(matz);
    }

    public static FloatMatrix scale(float x, float y, float z)
    {
        FloatMatrix mat = FloatMatrix.eye(4);
        mat.put(0,0,x);
        mat.put(1,1,y);
        mat.put(2,2,z);
        return mat;
    }

    public static FloatMatrix worldMat(FloatMatrix trans, FloatMatrix rot, FloatMatrix scale)
    {
        return trans.mmul(rot).mmul(scale);
    }

    public static FloatMatrix modelWorld(FloatMatrix trans, FloatMatrix rot, FloatMatrix scale, FloatMatrix pos)
    {
        return worldMat(trans, rot, scale).mmul(scale);
    }

    public static FloatMatrix ndc(FloatMatrix trans, FloatMatrix rot, FloatMatrix scale, FloatMatrix pos, FloatMatrix proj)
    {
        //System.out.println("DATA");
        FloatMatrix wmmat = worldMat(trans, rot, scale);
        FloatMatrix p = proj.mmul(wmmat.mmul(pos));
        //System.out.println(p.get(3));
        //Main.printm(wmmat);
        //System.out.println();
        //Main.printm(worldMat(trans, rot, scale));
        p = p.div(p.get(3));
        return p;
    }

    public static float w(FloatMatrix trans, FloatMatrix rot, FloatMatrix scale, FloatMatrix pos, FloatMatrix proj)
    {
        FloatMatrix wmmat = worldMat(trans, rot, scale);
        FloatMatrix p = proj.mmul(wmmat.mmul(pos));
        return p.get(3);
    }

    public static float mag(FloatMatrix vec)
    {
        return MatrixFunctions.sqrt(vec.dot(vec));
    }

    private static float crossz(int n1, int n2, int m1, int m2)
    {
        return n1*m2 - n2*m1;
    }

    public static boolean barycentric_triangle_test(int[]a, int[]p, int[]vs1, int[]vs2)
    {
        int q1 = p[0] - a[0];
        int q2 = p[1] - a[1];
        float s = crossz(q1, q2, (int)vs2[0], (int)vs2[1]) / crossz((int)vs1[0], (int)vs1[1], (int)vs2[0], (int)vs2[1]);
        float t = crossz((int)vs1[0], (int)vs1[1], (int)q1, (int)q2) / crossz((int)vs1[0], (int)vs1[1], (int)vs2[0], (int)vs2[1]);

        return s>=0 && t>=0 && (s+t <= 1);
    }

    public static int[] bounds(int[][]points)
    {
        int xmax = points[0][0];
        int ymax = points[0][1];
        int xmin = xmax;
        int ymin = ymax;

        for(int[] point:points)
        {
            if (point[0] > xmax)
            xmax = point[0];
            if (point[1] > ymax)
            ymax = point[1];
            if (point[0] < xmin)
            xmin = point[0];
            if (point[1] < ymin)
            ymin = point[1];
        }

        return new int[]{xmin, ymin, xmax, ymax};
    }

    protected static float[] barycentric_weights(int[]p1, int[]p2, int[]p3, int[]p)
    {
        float x1 = p1[0];
        float x2 = p2[0];
        float x3 = p3[0];
        float px = p[0];
        float y1 = p1[1];
        float y2 = p2[1];
        float y3 = p3[1];
        float py = p[1];

        float denom = ((y2-y3)*(x1-x3) + (x3-x2)*(y1-y3));
        float pxmx3 = px-x3;
        float pymy3 = py-y3;

        float w1 = ((y2-y3)*pxmx3 + (x3-x2)*pymy3)/
                denom;
        float w2 = ((y3-y1)*pxmx3 + (x1-x3)*pymy3)/
                denom;
        float w3 = 1 - w1 - w2;

        //System.out.println(w1+","+w2+","+w3);

        return new float[]{w1,w2,w3};
    }

    public static float[] barycentric_weights(float[]p1, float[]p2, float[]p3, float[]p)
    {
        float x1 = p1[0];
        float x2 = p2[0];
        float x3 = p3[0];
        float px = p[0];
        float y1 = p1[1];
        float y2 = p2[1];
        float y3 = p3[1];
        float py = p[1];

        float denom = ((y2-y3)*(x1-x3) + (x3-x2)*(y1-y3));
        float pxmx3 = px-x3;
        float pymy3 = py-y3;

        float w1 = ((y2-y3)*pxmx3 + (x3-x2)*pymy3)/
                denom;
        float w2 = ((y3-y1)*pxmx3 + (x1-x3)*pymy3)/
                denom;
        float w3 = 1 - w1 - w2;

        //System.out.println(w1+","+w2+","+w3);

        return new float[]{w1,w2,w3};
    }

    public static int[] interpolate(int[]p1, int[]p2, int[]p3, int[]p, int[]v1, int[]v2, int[]v3)
    {
        float[] weights = barycentric_weights(p1, p2, p3, p);
        int[]value = new int[v1.length];
        for(int i=0;i<v1.length;i++)
        {
            value[i]=(int)(weights[0] * v1[i] + weights[1] * v2[i] + weights[2]*v3[i]);
        }
        return value;
    }

    public static int[] interpolate(int[]p1, int[]p2, int[]p3, int[]p, int[]v1, int[]v2, int[]v3, int[]dest)
    {
        float[] weights = barycentric_weights(p1, p2, p3, p);
        for(int i=0;i<v1.length;i++)
        {
            dest[i]=(int)(weights[0] * v1[i] + weights[1] * v2[i] + weights[2]*v3[i]);
        }
        return dest;
    }

    public static float[] interpolate(int[]p1, int[]p2, int[]p3, int[]p, float[]v1, float[]v2, float[]v3, float[]dest)
    {
        float[] weights = barycentric_weights(p1, p2, p3, p);
        for(int i=0;i<v1.length;i++)
        {
            dest[i]=weights[0] * v1[i] + weights[1] * v2[i] + weights[2]*v3[i];
        }
        return dest;
    }

    public static float[] interpolate(float[] weights, float[]v1, float[]v2, float[]v3, float[]dest){
        for(int i=0;i<v1.length;i++)
        {
            dest[i]=weights[0] * v1[i] + weights[1] * v2[i] + weights[2]*v3[i];
        }
        return dest;
    }

    public static float interpolate(int[]p1, int[]p2, int[]p3, int[]p, float v1, float v2, float v3)
    {
        float[] weights = barycentric_weights(p1, p2, p3, p);
        float dest = weights[0] * v1 + weights[1] * v2 + weights[2]*v3;
        return dest;
    }

    public static float interpolate(float[] weights, float v1, float v2, float v3){
        return weights[0] * v1 + weights[1] * v2 + weights[2]*v3;
    }

    public static float dot(float[] a, float[] b){
        float prod = 0;
        for(int i=0;i<a.length;i++){
            prod += a[i] * b[i];
        }
        return prod;
    }

    public static float[] min(float[]a, float[] b){
        float[] prod = new float[a.length];
        for(int i=0;i<a.length;i++){
            prod[i] = a[i] - b[i];
        }
        return prod;
    }

    public static float[] normalize(float[]a, float[]dest){
        float dist = 0;
        for(float c:a){
            dist += Math.pow(c, 2);
        }
        dist = (float)Math.sqrt(dist);
        for(int i=0;i<dest.length;i++){
            dest[i] = a[i] / dist;
        }
        return dest;
    }

    public static float length(float[]a){
        float dist = 0;
        for(float c:a){
            dist += Math.pow(c, 2);
        }
        dist = (float)Math.sqrt(dist);
        return dist;
    }

    public static float phongDiffuse(float[] position, float[] normal, float[] lightPosition){
        float[] lightVec = min(lightPosition, position);
        float[] lightDir = new float[3];
        normalize(lightVec, lightDir);
        float dist = length(lightVec);
        float attenuation = L_EXP * (float)Math.pow(dist, 2) + L_LINEAR * dist + L_CONST;
        return Math.max(0.0f, dot(normal, lightDir)) / attenuation;
    }

    public static float clamp(float a, float min, float max)
    {
        float v = a;
        v = (a < min ? min : a);
        v = (a > max ? max : v);
        return v;
    }

    public static float mean(float[]v){
        float sum = 0;
        for(float f:v)
            sum += f;
        sum /= (float)v.length;
        return sum;
    }

    public static float mean_extrema(float[]v){
        float max = v[0];
        float min = v[0];
        for(float f:v) {
            max = (f > max ? f : max);
            min = (f < min ? f : min);
        }
        return (max+min)/2.0f;
    }
}
