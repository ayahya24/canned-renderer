import org.jblas.FloatMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameCore
{
    public boolean isLooping = true;
    FloatMatrix proj;
    FloatMatrix trans;
    FloatMatrix rot;
    FloatMatrix scale;
    float[]verts;
    float[]normals;
    int[]indices;
    int[]colors;
    float[]uvs;
    float theta;

    int[][]texture;

    float znear = 0.1f;
    float zfar = 1000f;

    float[] lightPos = {1,1,4};
    float lightIntensity = .75f;

    public GameCore() {
        //this.proj = Math3d.projection((float)display.getSize().width/(float)display.getSize().height, 70.f, 1000.f, 0.1f);
        this.trans = Math3d.translation(0, 0, -3.5f);
        this.rot = Math3d.rotation(0, 0, 0);
        float scalefac = 1f;
        this.scale = Math3d.scale(scalefac, scalefac, scalefac);

        this.verts = new float[]{
                -0.5f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f, 0.5f, 0.0f,
        };

        /*this.verts = new float[]{
                -.5f, -.5f, 0,
                .5f, -.5f, 0,
                -1.0f, .5f, 0
        };*/

        this.indices = new int[]{
                0, 1, 3, 3, 1, 2};
        //this.indices = new int[]{0,1,2};

        /*this.indices = new int[]{
                0,1,2
        };*/

        this.colors = new int[]{
                0,255,0,
                255,0,255,
                255,0,0,
                0,255,0
        };
        this.uvs = new float[]{
                0.0f, 0.0f,
                0.0f, 1f,
                1f, 1f,
                1f, 0.0f
        };

        this.texture = IOUtils.bytesImage("pengu.png");

        ArrayList<String> lines = IOUtils.readLines("pengu.obj");
        System.out.println(lines);
        List<float[]> vertsobj = (IOUtils.verticesFromLines_v(lines));
        List<float[]> uvsobj = (IOUtils.uvFromLines_v(lines));
        List<float[]> normals = IOUtils.normalsFromLines_v(lines);

        Mesh mesh = IOUtils.getMesh2(vertsobj, uvsobj, normals, lines);
        float[] objverts = ListUtils.toFloatArray(vertsobj);
        float[] objuvs = ListUtils.toFloatArray(vertsobj);

        this.verts = mesh.verts;
        this.uvs = mesh.uvs;
        this.indices = mesh.indices;
        this.normals = mesh.normals;

        this.theta = 0;
    }

    public void init(Display display)
    {
        this.proj = Math3d.projection((float)display.getSize().width/(float)display.getSize().height, 70.f, zfar, znear);
    }

    public void update(Display display)
    {
        this.theta += 0.01f;
        this.rot = Math3d.rotation(this.theta * 0, theta, this.theta*0);
        //this.lightPos[2] -= 0.01f;
        //System.out.println(this.lightPos[2]);
        //this.rot = Math3d.rotation(this.theta * 0, 3.1415f, this.theta*0);
        //this.trans = Math3d.translation(0, 0, -2.25f + theta);
    }
}
