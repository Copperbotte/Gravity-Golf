/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javagame1;

/**
 *
 * @author Joe
 */
class Matrix {
    
    private double[] Data;
    private int WIDTH,HEIGHT,SIZE;
    private int VertexBufferOffset;
    
    
    public double getData(int x, int y){return Data[y*WIDTH+x];}
    public double setData(int x, int y, double bubble)
        {return Data[(y*WIDTH+x) % SIZE] = bubble;}
    
    public int getWidth(){return WIDTH;}
    public int getHeight(){return HEIGHT;}
    
    public Matrix(int width,int height)
    {
        WIDTH = width;
        HEIGHT = height;
        SIZE = WIDTH*HEIGHT;
        Data = new double[width*(height+1)]; // height+1 for the extra vertex buffer.
        VertexBufferOffset = width*height;
        //for(int n=0;n<width;n++) setData(n, n, 1); // Identity matrix
        init();
    }
    
    public void init()
    {
        for(int n=0;n<WIDTH*HEIGHT;n++)
        {
            if(n%WIDTH == n/WIDTH)
                Data[n] = 1;
            else
                Data[n] = 0;
        }
    }
    
    
    public double[] mul(double[] Vertex)
    {
        if(Vertex.length != WIDTH) return Vertex;
        for(int a=0;a<HEIGHT;a++)
        {
            Data[VertexBufferOffset+a] = Vertex[a]; //Clone the array to the temporary array.
            Vertex[a] = 0;                          //Zero the source array.
        }
        //System.arraycopy(Vertex, 0, Data, VertexBufferOffset, WIDTH); 

        for(int a=0;a<HEIGHT;a++)
            for(int b=0;b<WIDTH;b++)
                Vertex[a] += getData(b,a) * Data[VertexBufferOffset+b];
        return Vertex;
    }
    
    public Matrix matMul(Matrix right) // DANGEROUS!!!!!!
    {
        if(right.getHeight() != WIDTH) return this;
        Matrix ret = new Matrix(right.getWidth(),HEIGHT); // BAD DANGEROUS SCARY
        for(int a=0;a<HEIGHT;a++)
            for(int b=0;b<right.getWidth();b++)
            {
                double bubble = 0;
                for(int l=0;l<WIDTH;l++)
                    bubble += getData(l,a)*right.getData(b,l);
                ret.setData(b, a, bubble);
            }
        
        return ret;
    }
    

    
    
    @Override
    public String toString()
    {
        String ret = "";
        for(int a=0;a<HEIGHT;a++)
        {
            for(int b=0;b<WIDTH;b++)
                ret += String.format("%9f ", getData(b,a));
            ret += '\n';
        }
        return ret;
    }
    
}
