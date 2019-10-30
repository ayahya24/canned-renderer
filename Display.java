import org.jblas.FloatMatrix;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.BufferedReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.out;

public class Display extends JFrame
{
    public int updateCount = 0;

    protected GameCore gameCore;

    private final static Color WHITE = new Color(255,255,255);

    private BufferedImage buffer;
    private float[][] zbuffer;

    public Display(String title, int w, int h)
    {
        super(title);
        setSize(w, h);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        zbuffer = new float[getWidth()][getHeight()];
        for(float[]x:zbuffer)
            Arrays.fill(x, 1e9f);
        //out.println(vbuffer);
    }


    public void render(GameCore gameCore)
    {
        this.gameCore = gameCore;
        repaint();
    }

    public void paint1(Graphics d)
    {
        /*
        Graphics g = buffer.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(),getHeight());

        g.setColor(Color.RED);
        g.fillRect((int)boxX,(int)boxY,20,20);


        d.drawImage(buffer,0,0, null);
        */
        for(float[]x:zbuffer)
            Arrays.fill(x, -1e9f);
        Graphics g = buffer.getGraphics();
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.WHITE);
        //out.println(this.gameCore.trans);
        FloatMatrix rot = gameCore.rot;
        FloatMatrix trans = gameCore.trans;
        FloatMatrix scale = gameCore.scale;
        FloatMatrix proj = gameCore.proj;
        //out.println("FOO");
        //Main.printm(rot);
        for(int i=0;i<gameCore.indices.length / 3; i++)
        {
            ArrayList<int[]> points = new ArrayList<int[]>();
            //ArrayList<int[]>colors = new ArrayList<int[]>();
            ArrayList<float[]>uvs = new ArrayList<float[]>();
            ArrayList<Float>ws = new ArrayList<Float>();
            ArrayList<Float> zs = new ArrayList<Float>();
            ArrayList<Float> zclips = new ArrayList<Float>();
            for(int j=0;j<3;j++)
            {
                float[]point = new float[4];
                //int[]color = new int[3];
                float[]uv = new float[2];
                for(int comp=0;comp<3;comp++)
                {
                    point[comp] = (gameCore.verts[3*gameCore.indices[3*i+j]+comp]);
                    //color[comp] = (gameCore.colors[3*gameCore.indices[3*i+j]+comp]);
                }

                point[3] = (1.0f);
                FloatMatrix pos = new FloatMatrix(point);
                FloatMatrix ndc = Math3d.ndc(trans, rot, scale, pos, proj);
                int[] px = {(int)(getSize().width/2 + (getSize().width * ndc.get(0))), (int)(getSize().height/2 - (getSize().height * ndc.get(1)))};
                points.add(px);
                //colors.add(color);

                FloatMatrix transf = (Math3d.worldMat(trans, rot, scale).mmul(pos));
                //Model view z
                float z = transf.get(2);
                //out.println(z);
                transf = proj.mmul(transf);
                float w = transf.get(3);
                float zclip = transf.get(2) / w; // Clip view z
                ws.add(1.0f/Math3d.w(trans, rot, scale, pos, proj));
                zs.add(z);
                zclips.add(zclip);
                //zclips.add(zclip);
                //out.println();
                //Main.printm(transf);

                ////out.println();
                ////Main.printm(new FloatMatrix().copy(ndc).mul(w));

                uv[0] = (gameCore.uvs[2*gameCore.indices[3*i+j]]) / w;
                uv[1] = (gameCore.uvs[2*gameCore.indices[3*i+j]+1]) / w;
                //out.println("UV: "+uv[0]+","+uv[1]+","+w);
                uvs.add(uv);
            }


            //line(g, Color.white, points.get(0), points.get(1));
            //line(g, Color.white, points.get(0), points.get(2));
            //line(g, Color.white, points.get(1), points.get(2));

            int[] bounds = Math3d.bounds(ListUtils.toInt2Array(points));
            bounds[0] = (int)Math3d.clamp(bounds[0], 0, getWidth()-1);
            bounds[1] = (int)Math3d.clamp(bounds[1], 0, getHeight()-1);
            bounds[2] = (int)Math3d.clamp(bounds[2], 0, getWidth()-1);
            bounds[3] = (int)Math3d.clamp(bounds[3], 0, getHeight()-1);

            int[]vs1 = {points.get(1)[0]-points.get(0)[0], points.get(1)[1]-points.get(0)[1]};
            int[]vs2 = {points.get(2)[0]-points.get(0)[0], points.get(2)[1]-points.get(0)[1]};
            int[] pt = new int[2];
            int[] color = new int[3];
            float[] uv = new float[2];
            int[] pointa = points.get(0);
            //out.println("POINTS");
            //out.println(Arrays.toString(pointa));
            //out.println(Arrays.toString(points.get(1)));
            //out.println(Arrays.toString(points.get(2)));
            //out.println(Arrays.toString(bounds));
            int[]zbuf = new int[3];
            for(int x=bounds[0];x<=bounds[2];x++)
            {
                pt[0] = x;
                for(int y=bounds[1];y<=bounds[3];y++)
                {
                    pt[1] = y;
                    float[] barycentric_weights = Math3d.barycentric_weights(pointa, points.get(1), points.get(2), pt);

                    boolean in_tri = Math3d.barycentric_triangle_test(pointa, pt, vs1, vs2);
                    float z = Math3d.interpolate(barycentric_weights, zs.get(0), zs.get(1), zs.get(2));
                    float zclip = Math3d.interpolate(barycentric_weights, zclips.get(0), zclips.get(1), zclips.get(2));
                    float oneoverw = Math3d.interpolate(barycentric_weights, ws.get(0), ws.get(1), ws.get(2));
                    boolean inView = (Math.abs(1.0f/oneoverw) - Math.abs(zclip * oneoverw)) > 0;
                    inView = zclip >-1f && zclip <1f && Math.abs(z) > gameCore.znear;
                    //if(x < 100)
                      //  out.println(z+","+1.0f/oneoverw);
                    in_tri = inView && in_tri && (z > (zbuffer[x][y]));
                    if(in_tri)
                    {
                        //Math3d.interpolate(pointa, points.get(1), points.get(2), pt, colors.get(0), colors.get(1), colors.get(2), color);
                        Math3d.interpolate(barycentric_weights, uvs.get(0), uvs.get(1), uvs.get(2), uv);

                        int[] pxCoords = new int[]{(int)((gameCore.texture.length-1) * uv[0]/oneoverw), (int)((gameCore.texture[0].length-1) * uv[1]/oneoverw)};
                        //float[]interp_w = Math3d.barycentric_weights(pointa, points.get(1), points.get(2), pt);
                        try{
                            IOUtils.getRGB(gameCore.texture[pxCoords[0]][pxCoords[1]], color);
                            //if(z > 1)
                                //out.println("Success"+uv[0] + "," + uv[1] + "," + Arrays.toString(pt) + "," + z + "," + gameCore.znear + "," + 1f/oneoverw);
                        }
                        catch (ArrayIndexOutOfBoundsException e){
                            //if(z > 1)
                            out.println(uv[0] + "," + uv[1] + "," + Arrays.toString(pt) + "," + zclip + "," + gameCore.znear + "," + 1f/oneoverw);
                            e.printStackTrace();
                            //g.setColor(Color.WHITE);
                            //g.fillRect(x,y,3,3);
                        }
                        //out.println("C: " + uv[1] * 254);
                        //g.setColor(new Color(color[0]*0 + (int)(254*uv[0]), (int)(254 * uv[1])+color[1]*0, 0*color[2]));
                        g.setColor(new Color(color[0], color[1], color[2]));
                        //g.setColor(Color.RED);
                        //g.fillRect((int)(pointa[0]*interp_w[0] + points.get(1)[0]*interp_w[1] + points.get(2)[0]*interp_w[2]), (int)(pointa[1]*interp_w[0] + points.get(1)[1]*interp_w[1] + points.get(2)[1]*interp_w[2]), 1, 1);
                        g.fillRect(x,y,1,1);

                        //int dCol = (int)Math3d.clamp(z, 0, 255);
                        //out.println(dCol+","+z);
                        //g.setColor(new Color(dCol, dCol, dCol));
                        //g.fillRect(x, y, 1, 1);
                        zbuffer[x][y] = z;
                    }
                }
            }
        }

        d.drawImage(buffer, 0, 0, null);
    }

    public void paint(Graphics d)
    {
         /*
        Graphics g = buffer.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(),getHeight());

        g.setColor(Color.RED);
        g.fillRect((int)boxX,(int)boxY,20,20);


        d.drawImage(buffer,0,0, null);
        */
        for(float[]x:zbuffer)
            Arrays.fill(x, -1e9f);
        Graphics g = buffer.getGraphics();
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.WHITE);
        //out.println(this.gameCore.trans);
        FloatMatrix rot = gameCore.rot;
        FloatMatrix trans = gameCore.trans;
        FloatMatrix scale = gameCore.scale;
        FloatMatrix proj = gameCore.proj;
        float[]point_temp = new float[3];
        //out.println("FOO");
        //Main.printm(rot);
        for(int i=0;i<gameCore.indices.length / 3; i++) //Iterate over triangles
        {
            ArrayList<int[]> points = new ArrayList<int[]>();
            ArrayList<float[]>worldPoints = new ArrayList<float[]>();
            ArrayList<float[]>uvs = new ArrayList<float[]>();
            ArrayList<Float>ws = new ArrayList<Float>();
            ArrayList<Float> zs = new ArrayList<Float>();
            ArrayList<Float> zclips = new ArrayList<Float>();
            float[]normal4 = new float[4];
            float[]normal = new float[3];
            for(int j=0;j<3;j++)
            {
                float[]point = new float[4];;
                float[]uv = new float[2];
                //float[]normal = new float[3];
                for(int comp=0;comp<3;comp++)
                {
                    point[comp] = (gameCore.verts[3*gameCore.indices[3*i+j]+comp]);
                    point_temp[comp] = point[comp];
                    normal4[comp] = (gameCore.normals[3*gameCore.indices[3*i+j]+comp]);
                    //color[comp] = (gameCore.colors[3*gameCore.indices[3*i+j]+comp]);
                }

                point[3] = (1.0f);
                normal4[3] = 0.0f;
                FloatMatrix pos = new FloatMatrix(point);
                FloatMatrix normalMat = new FloatMatrix(normal4);
                FloatMatrix transf = (Math3d.worldMat(trans, rot, scale).mmul(pos));
                //Model view z
                float z = transf.get(2);
                worldPoints.add(new float[]{transf.get(0), transf.get(1), z});
                transf = proj.mmul(transf);
                float w = transf.get(3);
                float zclip = transf.get(2) / w; // Clip view z
                FloatMatrix ndc = transf.div(w);
                int[] px = {(int)(getSize().width/2 + (getSize().width * ndc.get(0))), (int)(getSize().height/2 - (getSize().height * ndc.get(1)))};
                points.add(px);
                //colors.add(color);

                //out.println(z);

                ws.add(1.0f/Math3d.w(trans, rot, scale, pos, proj));
                zs.add(z);
                zclips.add(zclip);

                uv[0] = (gameCore.uvs[2*gameCore.indices[3*i+j]]) / w;
                uv[1] = (gameCore.uvs[2*gameCore.indices[3*i+j]+1]) / w;

                uvs.add(uv);

                float[]normalTransf = Math3d.worldMat(trans, rot, scale).mmul(normalMat).data;
                normal[0] = normalTransf[0];
                normal[1] = normalTransf[1];
                normal[2] = normalTransf[2];
                Math3d.normalize(normal, normal);
            }

            float[] centroid = new float[]{Math3d.mean_extrema(new float[]{worldPoints.get(0)[0], worldPoints.get(1)[0], worldPoints.get(2)[0]}),
                    Math3d.mean_extrema(new float[]{worldPoints.get(0)[1], worldPoints.get(1)[1], worldPoints.get(2)[1]}),
                    Math3d.mean_extrema(new float[]{worldPoints.get(0)[2], worldPoints.get(1)[2], worldPoints.get(2)[2]})};

            float lightingFac = Math3d.phongDiffuse(centroid, normal, gameCore.lightPos);

            //line(g, Color.white, points.get(0), points.get(1));
            //line(g, Color.white, points.get(0), points.get(2));
            //line(g, Color.white, points.get(1), points.get(2));

            int[] bounds = Math3d.bounds(ListUtils.toInt2Array(points));
            bounds[0] = (int)Math3d.clamp(bounds[0], 0, getWidth()-1);
            bounds[1] = (int)Math3d.clamp(bounds[1], 0, getHeight()-1);
            bounds[2] = (int)Math3d.clamp(bounds[2], 0, getWidth()-1);
            bounds[3] = (int)Math3d.clamp(bounds[3], 0, getHeight()-1);

            int[]vs1 = {points.get(1)[0]-points.get(0)[0], points.get(1)[1]-points.get(0)[1]};
            int[]vs2 = {points.get(2)[0]-points.get(0)[0], points.get(2)[1]-points.get(0)[1]};
            int[] pt = new int[2];
            int[] color = new int[3];
            float[] uv = new float[2];
            int[] pointa = points.get(0);
            //out.println("POINTS");
            //out.println(Arrays.toString(pointa));
            //out.println(Arrays.toString(points.get(1)));
            //out.println(Arrays.toString(points.get(2)));
            //out.println(Arrays.toString(bounds));
            int[]zbuf = new int[3];
            for(int x=bounds[0];x<=bounds[2];x++)
            {
                pt[0] = x;
                for(int y=bounds[1];y<=bounds[3];y++)
                {
                    pt[1] = y;
                    float[] barycentric_weights = Math3d.barycentric_weights(pointa, points.get(1), points.get(2), pt);

                    boolean in_tri = Math3d.barycentric_triangle_test(pointa, pt, vs1, vs2);
                    float z = Math3d.interpolate(barycentric_weights, zs.get(0), zs.get(1), zs.get(2));
                    float zclip = Math3d.interpolate(barycentric_weights, zclips.get(0), zclips.get(1), zclips.get(2));
                    float oneoverw = Math3d.interpolate(barycentric_weights, ws.get(0), ws.get(1), ws.get(2));
                    boolean inView = (Math.abs(1.0f/oneoverw) - Math.abs(zclip * oneoverw)) > 0;
                    inView = zclip >-1f && zclip <1f && Math.abs(z) > gameCore.znear;
                    //if(x < 100)
                    //  out.println(z+","+1.0f/oneoverw);
                    in_tri = inView && in_tri && (z > (zbuffer[x][y]));
                    if(in_tri)
                    {
                        //Math3d.interpolate(pointa, points.get(1), points.get(2), pt, colors.get(0), colors.get(1), colors.get(2), color);
                        Math3d.interpolate(barycentric_weights, uvs.get(0), uvs.get(1), uvs.get(2), uv);

                        int[] pxCoords = new int[]{(int)((gameCore.texture.length-1) * uv[0]/oneoverw), (int)((gameCore.texture[0].length-1) * uv[1]/oneoverw)};
                        //float[]interp_w = Math3d.barycentric_weights(pointa, points.get(1), points.get(2), pt);
                        try{
                            IOUtils.getRGB(gameCore.texture[pxCoords[0]][pxCoords[1]], color);
                            //if(z > 1)
                            //out.println("Success"+uv[0] + "," + uv[1] + "," + Arrays.toString(pt) + "," + z + "," + gameCore.znear + "," + 1f/oneoverw);
                        }
                        catch (ArrayIndexOutOfBoundsException e){
                            //if(z > 1)
                            out.println(uv[0] + "," + uv[1] + "," + Arrays.toString(pt) + "," + zclip + "," + gameCore.znear + "," + 1f/oneoverw);
                            e.printStackTrace();
                            //g.setColor(Color.WHITE);
                            //g.fillRect(x,y,3,3);
                        }
                        float diffuse = (lightingFac);
                        //out.println(diffuse);
                        float ambient = 0.25f;
                        float light = Math3d.clamp(ambient + diffuse * gameCore.lightIntensity, 0, 1);
                        //out.println(light);
                        //out.println("C: " + uv[1] * 254);
                        //g.setColor(new Color(color[0]*0 + (int)(254*uv[0]), (int)(254 * uv[1])+color[1]*0, 0*color[2]));
                        g.setColor(new Color((int)(color[0] * light), (int)(color[1] * light), (int)(color[2] * light)));
                        //out.println(lightingFac);
                        //g.setColor(new Color(diffuse, diffuse, diffuse));

                        g.fillRect(x,y,1,1);

                        //int dCol = (int)Math3d.clamp(z, 0, 255);
                        //out.println(dCol+","+z);
                        //g.setColor(new Color(dCol, dCol, dCol));
                        //g.fillRect(x, y, 1, 1);
                        zbuffer[x][y] = z;
                    }
                }
            }
            g.setColor(Color.white);
            FloatMatrix ndc = Math3d.ndc(Math3d.translation(centroid[0], centroid[1], centroid[2]), Math3d.rotation(0,0,0), Math3d.scale(1,1,1), new FloatMatrix(new float[]{0,0,0,1}), proj);
            int[] px = {(int)(getSize().width/2 + (getSize().width * ndc.get(0))), (int)(getSize().height/2 - (getSize().height * ndc.get(1)))};
            //g.fillRect(px[0], px[1], 5, 5);

        }

        d.drawImage(buffer, 0, 0, null);
    }

    public void line(Graphics g, Color color, int[]p1, int[]p2)
    {
        int dx = Math.abs(p1[0]-p2[0]);
        int dy = Math.abs(p1[1]-p2[1]);
        int x = p1[0];
        int y = p1[1];
        int sx = (p1[0]>p2[0]) ? -1 : 1;
        int sy = (p1[1]>p2[1]) ? -1 : 1;

        g.setColor(color);

        if(dx>dy)
        {
            float err = dx / 2.0f;
            while(x != p2[0]) {
                g.fillRect( x,  y, 1, 1);
                err -= dy;
                if (err < 0) {
                    y += sy;
                    err += dx;
                }
                x += sx;
            }
        }
        else
        {
            float err = dy/2.0f;
            while(y != p2[1])
            {
                g.fillRect(x, y, 1, 1);
                err -= dx;
                if(err < 0)
                {
                    x += sx;
                    err += dy;
                }
                y += sy;
            }
        }
        g.fillRect((int)x,(int) y, 1, 1);
    }
}
