/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javagame1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author Joe
 */
public class AttractorLeaf {
    
    double Volume;
    double[] ranges;
    double[] chewycenter;
    int State;
    AttractorLeaf[] Leaves;
       
    double getVolume(){return Volume;}
    double getRange(int in){return ranges[in%6];}
    double getRangeDifference(int in){return chewycenter[in%3];}
    AttractorLeaf getLeaf(int pos){return Leaves[pos%8];}
    void setLeaf(int pos, AttractorLeaf in){Leaves[pos%8] = in;}
    int getState(){return State;}
    void setState(int in){State = in;}
    
    AttractorLeaf(double dx, double ux, double dy, double uy, double dz, double uz)
    {
        ranges = new double[6];
        ranges[0] = dx;
        ranges[1] = ux;
        ranges[2] = dy;
        ranges[3] = uy;
        ranges[4] = dz;
        ranges[5] = uz;
        
        chewycenter = new double[3];
        for(int i=0;i<3;i++)
            chewycenter[i] = (ranges[2*i+1] + ranges[2*i])/2.0;
        
        
        Volume = 1;
        for(int i=0;i<3;i++)
            Volume *= ux-dx;
        
        Leaves = null;
    }
    
    public int Optomize()
    {
        if(Leaves == null) return State + 1;
        int ALLRIGHTFIRSTPLACE = Leaves[0].Optomize();
        if(ALLRIGHTFIRSTPLACE == 0) return 0; // 0 is inconclusive.
        for(int i=1;i<8;i++)
            if(ALLRIGHTFIRSTPLACE != Leaves[i].Optomize())
                return 0;
        Leaves = null;
        State = ALLRIGHTFIRSTPLACE - 1;
        return ALLRIGHTFIRSTPLACE;
    }
    
    public int GetDepth(int prevdepth)
    {
        if(Leaves == null) return prevdepth;
        int depth = prevdepth;
        for(int i=0;i<8;i++)
        {
            int dive = Leaves[i].GetDepth(prevdepth+1);
            if(dive > depth)
                depth = dive;
        }
        return depth;
    }
    
    public void UnOptomize(int depth)
    {
        if(depth == 0)return;
        if(Leaves == null)
        {
            Leaves = new AttractorLeaf[8];
        
            double halves[] = new double[3];
            for(int i=0;i<3;i++)
                halves[i] = (ranges[2*i+1] - ranges[2*i])/2.0;
            
            for(int y=0;y<2;y++)
                for(int z=0;z<2;z++)
                    for(int x=0;x<2;x++)
                    {
                        double dx = ranges[0] + x*halves[0];
                        double ux = ranges[0] + halves[0]*(x+1);
                        double dy = ranges[2] + y*halves[1];
                        double uy = ranges[2] + halves[1]*(y+1);
                        double dz = ranges[4] + z*halves[2];
                        double uz = ranges[4] + halves[2]*(z+1);
                        AttractorLeaf leaf = new AttractorLeaf(dx,ux,dy,uy,dz,uz);
                        leaf.setState(State);
                        setLeaf(4*z+2*y+x, leaf);
                    }
            
            State = 0;
        }
        
        for(int i=0;i<8;i++)
            Leaves[i].UnOptomize(depth - 1);
        
    }
    
    public void UnOptomize()
    {
        int depth = GetDepth(0);
        UnOptomize(depth);
    }
    
    public void traverseAndAttract(RenderablePoint point)
    {
        switch(State)
        {
            case 0: // 
                for(int i=0;i<8;i++)
                    Leaves[i].traverseAndAttract(point);
                return;
            case 1: // solid
                //calculate force, taken from my c++ code
                HandyFuncs.Attract(point, chewycenter, Volume);
                return;
                
            case 2: // air
                //do nothing
                return;
        }
        
        
        
    }
    
    public void traverseAndAppendPoint(BSPLeaf target, double[] pos, ImageIcon mage)
    {
        if(State == 1)
        {
            RenderablePoint p = new RenderablePoint(chewycenter[0],chewycenter[1],chewycenter[2],mage);
            p.AppendToBottom(target, pos);
        }
        if(Leaves != null)
            for(int i=0;i<8;i++)
                Leaves[i].traverseAndAppendPoint(target, pos, mage);
    }
    
    
    public static AttractorLeaf loadAttractorOctree(String path)
    {
        if(path.equals(""))return null;

            AttractorLeaf newmodel = null;
        
        try{
            Scanner scan = new Scanner(new File(path)); // This should be ordinary java. Speed is not important here.
            
            scan.next(); // x
            double dx = Double.parseDouble(scan.next());
            double ux = Double.parseDouble(scan.next());
            scan.next(); // y
            double dy = Double.parseDouble(scan.next());
            double uy = Double.parseDouble(scan.next());
            scan.next(); // z
            double dz = Double.parseDouble(scan.next());
            double uz = Double.parseDouble(scan.next());
            
            newmodel = new AttractorLeaf(dx,ux,dy,uy,dz,uz);
            
            loadAttractorLeaf(scan,newmodel);
            scan.close();
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, e, "EXCEPTION", JOptionPane.WARNING_MESSAGE);
            try{new FileOutputStream("sample.txt");}
            catch (IOException e2)
            {
                JOptionPane.showMessageDialog(null, e2, "EXCEPTION", JOptionPane.WARNING_MESSAGE);
            }
            return null;
        }
        return newmodel;
        
        
    }
    
    private static void loadAttractorLeaf(Scanner scan, AttractorLeaf root)
    {
  
        String command = scan.next();
        //System.out.println(command);
        switch(command)
        {
            case "AIR":
                root.setState(2);
                return;
            case "SOLID":
                root.setState(1);
                return;
        }
        
        if(!command.equals("ROOT")) return;
        
        double halves[] = new double[3];
        for(int i=0;i<3;i++)
            halves[i] = (root.getRange(2*i+1) - root.getRange(2*i))/2.0;
        
        root.Leaves = new AttractorLeaf[8];
        
        for(int y=0;y<2;y++)
            for(int z=0;z<2;z++)
                for(int x=0;x<2;x++)
                {
                    double dx = root.getRange(0) + x*halves[0];
                    double ux = root.getRange(0) + halves[0]*(x+1);
                    double dy = root.getRange(2) + y*halves[1];
                    double uy = root.getRange(2) + halves[1]*(y+1);
                    double dz = root.getRange(4) + z*halves[2];
                    double uz = root.getRange(4) + halves[2]*(z+1);
                    AttractorLeaf leaf = new AttractorLeaf(dx,ux,dy,uy,dz,uz);
                    root.setLeaf(4*z+2*y+x, leaf);
                    loadAttractorLeaf(scan,leaf);
                }
        
    }
    
}
