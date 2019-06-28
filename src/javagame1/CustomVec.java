/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javagame1;

/**
 *
 * @author joseph_kessler
 */
public class CustomVec {
    
    private double[] pos;
    private double[] pastpos;

    public double getPos(int dim){return pos[dim%pos.length];}
    public double getPrevPos(int dim){return pastpos[dim%pastpos.length];}
    public void setPos(int dim, double bubble){pastpos[dim%pastpos.length] = pos[dim%pos.length] = bubble;}
    public void multiplyBy(int dim, double bubble){pos[dim] *= bubble;}
    public void Update(double dt)
    {
        if(pos.length == 1) return;        
        for(int n=pos.length-2;n>=0;n--)
        {
            pastpos[n] = pos[n];
            pos[n] += pos[n+1] * dt;   
        }
    }

    public boolean reflect(double bound, boolean upper, double energyscale)
    {
        if(pos.length < 2) return false;
        
        if(upper) // these are both if false, negate all operators and results to get the original.
        {   if(pos[0] < bound || pos[1] <= 0) return false;}
        else
        {   if(pos[0] > bound || pos[1] >= 0) return false;}
        
        //if(Pos >= bound && Vel > 0)
        // Original If Statement
        
        pos[0] = bound;
        pos[1] *= -energyscale;//-1 * 0.9;
        return true;
    }
    
    private void initVec(int dimensions)
    {
        pos = new double[dimensions];
        pastpos = new double[dimensions];
    }
    
    public CustomVec(int dimensions)
    {
        initVec(dimensions);
    }
    
    public CustomVec()
    {
        initVec(3);
    }
}
