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
public class HandyFuncs {
    
    public static int max(int A, int B)
    {
        if(A < B) return B;
        return A;
    }
    
    public static double max(double A, double B)
    {
        if(A < B) return B;
        return A;
    }
    
    public static int min(int A, int B)
    {
        if(A > B) return B;
        return A;
    }
    
    public static double min(double A, double B)
    {
        if(A > B) return B;
        return A;
    }
    
    public static double Lerp(double P0, double P1, double X)
    {
        //mx+b
        //x=0: P0=x
        //x=1: m+P0=P1
        //     P1-P0=m
        //(P1-P0)m+P0
        return (P1-P0)*X+P0;
    }
    
    public static double Clamp(double P0, double P1, double X)
    {
        if(X < P0)
            return P0;
        if(X > P1)
            return P1;
        return X;
    }
    
    public static double dot(double[] A, double[] B)
    {
        int count = min(A.length,B.length);
        double sum = 0;
        for(int n=0;n<count;n++)
            sum += A[n] * B[n];
        return sum;
    }
    
    public static double[] cross(double[] A, double[] B)
    {
        int count = min(A.length,B.length);
        if(count < 3) return A;
        double ret[] = new double[3];
        for(int i=0;i<3;i++)
        {
            int u = (i+1)%3;
            int v = (i+2)%3;
            ret[i] = A[u]*B[v] - A[v]*B[u];
        }
        return ret;
    }
    
    public static double normalize(double[] A, int count)
    {
        int len = min(count,A.length);
        double sum = 0;
        for(int n=0;n<len;n++)
            sum += A[n]*A[n];
        sum = Math.sqrt(sum);
        for(int n=0;n<len;n++)
            A[n] = A[n]/sum;
        return sum;
    }
    
    public static double normalize(double[] A)
    {
        return normalize(A,A.length);
    }
    
    public static double ForceConst = -0.0000001;
    
    public static void Attract(RenderablePoint point, double []chewycenter, double Volume)
    {
        
        double coord[] = {0,0,0};
        double accel[] = {0,0,0};
        double r2 = 0;
        for(int i=0;i<3;i++)
        {
            CustomVec v = point.getVec(i);
            accel[i] = v.getPos(2);
            coord[i] = v.getPos(0) - chewycenter[i];
            r2 += coord[i]*coord[i];
        }
        double mass = Volume*1;
        double force = (ForceConst*mass)/r2;
        double component = force/Math.sqrt(r2); // this might be able to be done using a dot product.
        for(int i=0;i<3;i++)
            point.getVec(i).setPos(2, accel[i] + coord[i]*component);
    }
    
}
