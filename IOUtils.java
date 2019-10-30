import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class IOUtils
{
    public static int[][] bytesImage(String url)
    {
        try
        {
            BufferedImage img = ImageIO.read(new File(url));
            int[][]colors = new int[img.getWidth()][img.getHeight()];
            for(int x=0;x<img.getWidth();x++)
            {
                for(int y=0;y<img.getHeight();y++)
                {
                    colors[x][y] = img.getRGB(x, y);
                }
            }
            return colors;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static int[] getRGB(int color, int[] dest)
    {
        dest[0] = (color>>16) & 0x000000FF;
        dest[1] = (color>>8) & 0x000000FF;
        dest[2] = (color) & 0x000000FF;
        return dest;
    }

    public static ArrayList<String> readLines(String url)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(url));
            String line = reader.readLine();
            ArrayList<String>lines = new ArrayList<String>();
            while(line!=null){
                lines.add(line);
                line = reader.readLine();
            }
            reader.close();
            return lines;
        }
        catch (IOException e)
        {
            e.printStackTrace();;
        }
        return null;
    }

    public static float[]verticesFromLines(ArrayList<String>lines)
    {
        ArrayList<Float> vertal = new ArrayList<Float>();
        for(String line: lines)
        {
            String[] tokens = line.split("\\s+");
            if(tokens[0].equals("v")) {
                vertal.add(Float.parseFloat(tokens[1]));
                vertal.add(Float.parseFloat(tokens[2]));
                vertal.add(Float.parseFloat(tokens[3]));
            }
        }
        float[]verts = new float[vertal.size()];
        for(int i=0;i<vertal.size();i++)
            verts[i] = vertal.get(i);
        return verts;
    }

    public static List<float[]> verticesFromLines_v(ArrayList<String>lines)
    {
        ArrayList<Float> vertal = new ArrayList<Float>();
        for(String line: lines)
        {
            String[] tokens = line.split("\\s+");
            if(tokens[0].equals("v")) {
                vertal.add(Float.parseFloat(tokens[1]));
                vertal.add(Float.parseFloat(tokens[2]));
                vertal.add(Float.parseFloat(tokens[3]));
            }
        }
        //float[]verts = new float[vertal.size()];
        ArrayList<float[]>vectors = new ArrayList<float[]>();
        for(int i=0;i<vertal.size()/3;i++)
            vectors.add(new float[]{vertal.get(3*i), vertal.get(3*i+1), vertal.get(3*i+2)});
        return vectors;
    }

    public static List<float[]> normalsFromLines_v(ArrayList<String>lines)
    {
        ArrayList<Float> normal = new ArrayList<Float>();
        for(String line: lines)
        {
            String[] tokens = line.split("\\s+");
            if(tokens[0].equals("vn")) {
                normal.add(Float.parseFloat(tokens[1]));
                normal.add(Float.parseFloat(tokens[2]));
                normal.add(Float.parseFloat(tokens[3]));
            }
        }
        //float[]verts = new float[vertal.size()];
        ArrayList<float[]>vectors = new ArrayList<float[]>();
        for(int i=0;i<normal.size()/3;i++)
            vectors.add(new float[]{normal.get(3*i), normal.get(3*i+1), normal.get(3*i+2)});
        return vectors;
    }

    public static float[]uvFromLines(ArrayList<String>lines)
    {
        ArrayList<Float> uval = new ArrayList<Float>();
        for(String line: lines)
        {
            String[] tokens = line.split("\\s+");
            if(tokens[0].equals("vt")) {
                uval.add(Float.parseFloat(tokens[1]));
                uval.add(Float.parseFloat(tokens[2]));
            }
        }
        float[]uv = new float[uval.size()];
        for(int i=0;i<uval.size();i++)
            uv[i] = uval.get(i);
        return uv;
    }

    public static List<float[]> uvFromLines_v(ArrayList<String> lines)
    {
        ArrayList<Float> uval = new ArrayList<Float>();
        for(String line: lines)
        {
            String[] tokens = line.split("\\s+");
            if(tokens[0].equals("vt")) {
                uval.add(Float.parseFloat(tokens[1]));
                uval.add(Float.parseFloat(tokens[2]));
            }
        }
        ArrayList<float[]>vectors = new ArrayList<float[]>();
        for(int i=0;i<uval.size()/2;i++)
            vectors.add(new float[]{uval.get(2*i), uval.get(2*i+1)});
        return vectors;
    }

    /*public static int[]indices(List<float[]>pos, List<float[]>uv, ArrayList<String>lines)
    {
        ArrayList<Face>faces = new ArrayList<Face>();
        for(String line: lines)
        {
            String[] tokens = line.split("\\s+");
            if(tokens[0].equals("f")) {
                faces.add(new Face(tokens[1], tokens[2], tokens[3]));
            }
        }
        return reorderLists(pos, uv, faces);
    }*/

    public static Mesh getMesh2(List<float[]> posList, List<float[]> textCoordList, List<float[]> normalList,
                        List<String> lines)
    {
        ArrayList<String>faces = new ArrayList<String>();
        for(String line: lines)
        {
            String[] tokens = line.split("\\s+");
            if(tokens[0].equals("f")) {
                faces.add(tokens[1]);
                faces.add(tokens[2]);
                faces.add(tokens[3]);
            }
        }
        List<float[]> newPosList = new ArrayList<float[]>();
        List<float[]> newTexCoordList = new ArrayList<float[]>();
        List<float[]> newNormalList = new ArrayList<float[]>();
        List<Integer>indices = new ArrayList<Integer>();
        int idx = 0;
        for(String v: faces)
        {
                boolean indexed = false;
                for(Integer i: indices)
                    if(faces.get(i).equals(v))
                        indexed = true;
                if(!indexed) {
                    indices.add(idx);
                }
                idx++;
        }
        HashMap<Integer, String> idx_faces = new HashMap<Integer, String>();
        HashMap<String, Integer> faces_idx = new HashMap<String, Integer>();
        idx = 0;
        for(String v:faces){
            if(! idx_faces.values().contains(v)){
                System.out.println(v.charAt(0));
                idx_faces.put(idx, v);
                faces_idx.put(v, idx);
                idx += 1;
            }
        }

        for(Integer index:indices)
        {
            String[] face_idx = faces.get(index).split("/");
            //System.out.println(faces.get(index));
            newPosList.add(posList.get(Integer.parseInt(face_idx[0])-1));
            newTexCoordList.add(textCoordList.get((Integer.parseInt(face_idx[1])-1)));
            newNormalList.add(normalList.get((Integer.parseInt(face_idx[2])-1)));
        }

        float[] newPos = new float[newPosList.size() * 3];
        float[] newNormal = new float[newNormalList.size() * 3];
        float[] newTexCoord = new float[newTexCoordList.size() * 2];
        int[] indices_arr = new int[faces.size()];

        for(int i=0;i<newPosList.size();i++){
            newPos[3*i] = newPosList.get(i)[0];
            newPos[3*i+1] = newPosList.get(i)[1];
            newPos[3*i+2] = newPosList.get(i)[2];
        }

        for(int i=0;i<newNormalList.size();i++){
            newNormal[3*i] = newNormalList.get(i)[0];
            newNormal[3*i+1] = newNormalList.get(i)[1];
            newNormal[3*i+2] = newNormalList.get(i)[2];
        }

        for(int i=0;i<newTexCoordList.size();i++){
            newTexCoord[2*i] = newTexCoordList.get(i)[0];
            newTexCoord[2*i+1] = 1 - newTexCoordList.get(i)[1];
        }

        for(int i=0;i<faces.size();i++){
            indices_arr[i] = faces_idx.get(faces.get(i));
        }

        return new Mesh(newPos, newNormal, newTexCoord, indices_arr);
    }


    protected static class IdxGroup
    {
        public static final int NO_VALUE = -1;

        public int idxPos;

        public int idxTextCoord;

        public int idxVecNormal;

        public IdxGroup() {
            idxPos = NO_VALUE;
            idxTextCoord = NO_VALUE;
            idxVecNormal = NO_VALUE;
        }
    }

    /*private static int[] reorderLists(List<float[]> posList, List<float[]> textCoordList,
                                     List<Face> facesList) {

        List<Integer> indices = new ArrayList<>();
        // Create position array in the order it has been declared
        float[] posArr = new float[posList.size() * 3];
        int i = 0;
        for (float[] pos : posList) {
            posArr[i * 3] = pos[0];
            posArr[i * 3 + 1] = pos[1];
            posArr[i * 3 + 2] = pos[2];
            i++;
        }
        float[] textCoordArr = new float[posList.size() * 2];
        float[] normArr = new float[posList.size() * 3];

        for (Face face : facesList) {
            IdxGroup[] faceVertexIndices = face.getFaceVertexIndices();
            for (IdxGroup indValue : faceVertexIndices) {
                processFaceVertex(indValue, textCoordList, null,
                        indices, textCoordArr, normArr);
            }
        }
        int[] indicesArr = new int[indices.size()];
        indicesArr = indices.stream().mapToInt((Integer v) -> v).toArray();
        return indicesArr;
    }*/

    public static Mesh getMesh(List<float[]> posList, List<float[]> textCoordList, List<float[]> normalList,
                               List<String> lines)
    {
        ArrayList<Face>faces = new ArrayList<Face>();
        for(String line: lines)
        {
            String[] tokens = line.split("\\s+");
            if(tokens[0].equals("f")) {
                faces.add(new Face(tokens[1], tokens[2], tokens[3]));
            }
        }
        return reorderLists(posList, textCoordList, normalList, faces);
    }

    private static Mesh reorderLists(List<float[]> posList, List<float[]> textCoordList, List<float[]> normalList,
                                      List<Face> facesList) {

        List<Integer> indices = new ArrayList<>();
        // Create position array in the order it has been declared
        float[] posArr = new float[posList.size() * 3];
        int i = 0;
        for (float[] pos : posList) {
            posArr[i * 3] = pos[0];
            posArr[i * 3 + 1] = pos[1];
            posArr[i * 3 + 2] = pos[2];
            i++;
        }
        float[] textCoordArr = new float[posList.size() * 2];
        float[] normArr = new float[posList.size() * 3];

        for (Face face : facesList) {
            IdxGroup[] faceVertexIndices = face.getFaceVertexIndices();
            for (IdxGroup indValue : faceVertexIndices) {
                processFaceVertex(indValue, textCoordList, normalList,
                        indices, textCoordArr, normArr);
            }
        }
        int[] indicesArr = new int[indices.size()];
        indicesArr = indices.stream().mapToInt((Integer v) -> v).toArray();
        for(int ii=0;ii<textCoordArr.length/2;ii++){
            System.out.println("["+textCoordArr[2*ii]+", "+textCoordArr[2*ii+1]);
        }
        System.out.println(Arrays.toString(indicesArr));
        return new Mesh(posArr, normArr, textCoordArr, indicesArr);
    }


    private static void processFaceVertex(IdxGroup indices, List<float[]> textCoordList, List<float[]> normalList,
                                          List<Integer> indicesList,
                                          float[] texCoordArr, float[] normArr) {

        // Set index for vertex coordinates
        int posIndex = indices.idxPos;
        indicesList.add(posIndex);

        // Reorder texture coordinates
        if (indices.idxTextCoord >= 0) {
            float[] textCoord = textCoordList.get(indices.idxTextCoord);
            texCoordArr[posIndex * 2] = textCoord[0];
            texCoordArr[posIndex * 2 + 1] = 1 - textCoord[1];
        }
        if (indices.idxVecNormal >= 0) {
            // Reorder vectornormals
            float[] vecNorm = normalList.get(indices.idxVecNormal);
            normArr[posIndex * 3] = vecNorm[0];
            normArr[posIndex * 3 + 1] = vecNorm[1];
            normArr[posIndex * 3 + 2] = vecNorm[2];
        }
    }

    protected static class Face {

        /**
         * List of idxGroup groups for a face triangle (3 vertices per face).
         */
        private IdxGroup[] idxGroups = new IdxGroup[3];

        public Face(String v1, String v2, String v3) {
            idxGroups = new IdxGroup[3];
            // Parse the lines
            idxGroups[0] = parseLine(v1);
            idxGroups[1] = parseLine(v2);
            idxGroups[2] = parseLine(v3);
        }

        private IdxGroup parseLine(String line) {
            IdxGroup idxGroup = new IdxGroup();

            String[] lineTokens = line.split("/");
            int length = lineTokens.length;
            idxGroup.idxPos = Integer.parseInt(lineTokens[0]) - 1;
            if (length > 1) {
                // It can be empty if the obj does not define text coords
                String textCoord = lineTokens[1];
                idxGroup.idxTextCoord = textCoord.length() > 0 ? Integer.parseInt(textCoord) - 1 : IdxGroup.NO_VALUE;
                if (length > 2) {
                    idxGroup.idxVecNormal = Integer.parseInt(lineTokens[2]) - 1;
                }
            }

            return idxGroup;
        }

        public IdxGroup[] getFaceVertexIndices() {
            return idxGroups;
        }
    }


    protected static class Vector3f{
        public float x;
        public float y;
        public float z;

        public Vector3f(float x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vector3f(float[]c){
            this.x = c[0];
            this.y = c[1];
            this.z = c[2];
        }
    }

    protected static class Vector2f{
        public float x;
        public float y;

        public Vector2f(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public Vector2f(float[]c){
            this.x = c[0];
            this.y = c[1];
        }
    }
}
