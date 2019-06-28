/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javagame1;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JOptionPane;

/**
 * How to generate a normal:
 * normal = normalize(cross(p1-p0,p2-p0));
 * @author Joe
 */
public class BSPLeaf {
    private double plane[];
    private BSPLeaf Back,Front;
    private Renderable Render_Object;
    
    BSPLeaf(double[] normal, double[] pos)
    {
        plane = new double[4];
        reInit(normal,pos);
        Render_Object = null;
    }

    
    public void reInit(double[] normal, double[] pos)
    {
        System.arraycopy(normal, 0, plane, 0, 3);
        plane[3] = 0;
        for(int i=0;i<3;i++)
            plane[3] += plane[i]*pos[i];
        Back = null;
        Front = null;
    }
    
    public boolean inFront(double[] pos)
    {
        double sum = 0;
        for(int i=0;i<3;i++)
            sum += pos[i]*plane[i];
        return sum > plane[3];
    }
    
    public BSPLeaf getFront(){return Front;}
    public void setFront(BSPLeaf in){Front = in;}
    public BSPLeaf getBack(){return Back;}
    public void setBack(BSPLeaf in){Back = in;}
    public final double[] getNormal(){return plane;}
    public Renderable getRenderable(){return Render_Object;}
    public void setRenderable(Renderable In){Render_Object = In;}
    
    public void traverseAndReflect(RenderablePoint point)
    {
        if(this.Render_Object instanceof RenderablePoint) return;
        
        Back.traverseAndReflect(point);
        Front.traverseAndReflect(point);
        
    }
    
    public void traverseAndDraw(ReboundPanel graphics, Graphics page, double[] campos)
    {
        boolean isfront = inFront(campos);
        
        if(isfront)
        {
            if(Back != null)
                Back.traverseAndDraw(graphics,page,campos);
        }
        else
            if(Front != null)
                Front.traverseAndDraw(graphics,page,campos);
        
        Render_Object.Render(graphics, page, campos, isfront);
        
        if(!isfront)
        {
            if(Back != null)
                Back.traverseAndDraw(graphics,page,campos);
        }
        else
            if(Front != null)
                Front.traverseAndDraw(graphics,page,campos);
    }
    
    public static void purge(BSPLeaf root)
    {
        if(root == null) return;
        BSPLeaf back = root.getBack();
        BSPLeaf front = root.getFront();
        purge(back);
        purge(front);
        if(back != null)
            if(back.getRenderable().isPurgable())
                root.setBack(null);
        if(front != null)
            if(front.getRenderable().isPurgable())// instanceof RenderablePoint)
                root.setFront(null);
    }
    
    
    
    //saving
    public static BSPLeaf loadJBSPFile(String path)
    {
        if(path.equals(""))return null;

            BSPLeaf newmodel = null;
        
        try{
            Scanner scan = new Scanner(new File(path)); // This should be ordinary java. Speed is not important here.
            newmodel = loadJBSPLeaf(scan);
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
    
    public static void SaveJBSPFile(BSPLeaf root,String path)
    {
        try{
            String output = SaveJBSPLeaf(root);
            FileOutputStream fis = new FileOutputStream(path);
            fis.write(output.getBytes());
            
        }
        catch (FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(null, e, "EXCEPTION", JOptionPane.WARNING_MESSAGE);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, e, "EXCEPTION", JOptionPane.WARNING_MESSAGE);
        }
        
        
    }
    
    private static BSPLeaf loadJBSPLeaf(Scanner scan)
    {
        String command = scan.next(); // If the node is null, do not perform recursion.
        if(command.equals("NULL"))
            return null;
        
        int vert_count = Integer.parseInt(scan.next());
        
        String Name = ""; // defaults in case of bad data
        double xverts[] = new double[vert_count];
        double yverts[] = new double[vert_count];
        double zverts[] = new double[vert_count];
        double normal[] = {0,1,0};
        double color[] = {1,1,1,1};

        while(true)
        {
            command = scan.next();
            if(command.equals("END")) break;
            switch(command)
            {
                case "NAME":
                    Name = scan.nextLine().substring(1);
                    break;
                case "XPOS":
                    for(int n=0;n<vert_count;n++)
                        xverts[n] = Double.parseDouble(scan.next());
                    break;
                case "YPOS":
                    for(int n=0;n<vert_count;n++)
                        yverts[n] = Double.parseDouble(scan.next());
                    break;
                case "ZPOS":
                    for(int n=0;n<vert_count;n++)
                        zverts[n] = Double.parseDouble(scan.next());
                    break;
                case "NORM":
                    for(int n=0;n<3;n++)
                        normal[n] = Double.parseDouble(scan.next());
                    break;
                case "COLOR":
                    for(int n=0;n<4;n++)
                        color[n] = Double.parseDouble(scan.next());
                    break;
            }
        }
        
        //BSPLeaf ret = new BSPLeaf(xverts,yverts,zverts,normal,Name,
        //    new Color((float)color[0],(float)color[1],(float)color[2],(float)color[3])); // Generate a color, complete with alpha!
        
        double[] pos = {xverts[0],yverts[0],zverts[0]};
        BSPLeaf animalcrossing = new BSPLeaf(normal,pos);
        RenderablePolygon rep = new RenderablePolygon(xverts,yverts,zverts,Name,
            new Color((float)color[0],(float)color[1],(float)color[2],(float)color[3])); // Generate a color, complete with alpha!
        
        animalcrossing.setRenderable(rep);
        animalcrossing.setBack(loadJBSPLeaf(scan)); // Recursion, polish notation.
        animalcrossing.setFront(loadJBSPLeaf(scan));

        return animalcrossing;
    }
    
    private static String SaveJBSPLeaf(BSPLeaf target)
    {
        String VERT = "VERT ";
        String NAME = "NAME ";
        String XPOS = "XPOS ";
        String YPOS = "YPOS ";
        String ZPOS = "ZPOS ";
        String NORM = "NORM ";
        String COLOR = "COLOR ";
        String END = "END\n\n";
        String NULL = "NULL";
        String output = "";
        
        
        
        
        Renderable rendtest = target.getRenderable();
        if(rendtest instanceof RenderablePolygon)
        {
            RenderablePolygon rend = (RenderablePolygon)rendtest;
        
            double[][] verts = rend.getVertexBuffers();
            double[] norm = target.getNormal();
            HandyFuncs.normalize(norm, 3);

            for(int i=0;i<rend.getVertexLength();i++)
            {
                XPOS += Double.toString(verts[0][i]) + ' ';
                YPOS += Double.toString(verts[1][i]) + ' ';        
                ZPOS += Double.toString(verts[2][i]) + ' ';        
            }

            for(int i=0;i<3;i++)
                NORM += Double.toString(norm[i]) + ' ';

            for(int i=0;i<4;i++)
                COLOR += Double.toString(rend.getColor().getRGBComponents(null)[i]) + ' ';

            output += VERT + rend.getVertexLength() + '\n';
            output += NAME + rend.getName() + '\n';
            output += XPOS + '\n' + YPOS + '\n' + ZPOS + '\n';
            output += NORM + '\n';
            output += COLOR + '\n';
            output += END;

        }
        else
            output = "NULL\n\n";
        
        BSPLeaf next = target.getBack();
        for(int i=0;i<2;i++) // compact two-entry switcher
        {
            if(next == null)
                output += NULL + "\n\n";
            else
                output += SaveJBSPLeaf(next);
            next = target.getFront();
        }
        
        return output;
    }

    
}
