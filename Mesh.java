public class Mesh
{
    public float[] verts;
    public float[] normals;
    public float[] uvs;
    public int[] indices;

    public Mesh(float[] verts, float[] normals, float[] uvs, int[] indices) {
        this.verts = verts;
        this.normals = normals;
        this.uvs = uvs;
        this.indices = indices;
    }
}
