/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javagame1;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author Joe
 */
public class RenderablePolygon implements Renderable {

    private boolean purgable;
    private double Verts[][];
    private int length;
    private String name;
    private Color shade;
    
    
    @Override
    public boolean isPurgable(){return purgable;}
    public void setPurgable(boolean in){purgable = true;}
    public String getName(){return name;}
    public Color getColor(){return shade;}
    public void setColor(Color in){shade = in;}
    public final double[][] getVertexBuffers(){return Verts;}
    public final int getVertexLength(){return length;}
    
    RenderablePolygon(double[] xverts, double[] yverts, double[] zverts, 
            String inName, Color cool)
    {
        purgable = false;
        length = xverts.length;
        Verts = new double[3][];
        Verts[0] = xverts;
        Verts[1] = yverts;
        Verts[2] = zverts;
        name = inName;
        shade = cool; // (⌐■_■)
    }
    
    
    
    @Override
    public void Render(ReboundPanel graphics, Graphics page, double[] campos, boolean isfront) {

        double bubble[] = {0,0,0,1};
        int xstuff[] = new int[length];
        int ystuff[] = new int[length];
        
        boolean rend = isfront;
        rend = rend || true;
        
        if(rend)
        {
            for(int i=0;i<length;i++)
            {
                for(int n=0;n<3;n++)
                    bubble[n] = Verts[n][i];
                bubble[3] = 1;
                double w = graphics.TransformBSP(bubble);
                if(w == 0)
                    return;
                xstuff[i] = (int)bubble[0];
                ystuff[i] = (int)bubble[1];
            }
            
            Color cool = page.getColor();
            
            if(graphics.Key_Array[' '])
            {
                Color rad;
                if(isfront)
                    rad = new Color(shade.getRed(),shade.getGreen(),shade.getBlue(),shade.getAlpha() / 2);
                else
                    rad = new Color(shade.getRed(),shade.getGreen(),shade.getBlue(),shade.getAlpha() / 2);
                page.setColor(rad);
            }
            else page.setColor(shade);
            
            page.fillPolygon(xstuff,ystuff,length);
            page.setColor(cool);
        }

    }
    
    
    // this should only be used for purgable polygons
    public void AppendToBottom(BSPLeaf root, double[] origin, double[] norm)
    {
        purgable = true;

        boolean infront = false;
        BSPLeaf temp = root;
        while(true)
        {
            infront = root.inFront(origin);
            if(infront)
            {
                temp = root.getFront();
                if(temp == null) break;
                root = temp;
            }
            else
            {
                temp = root.getBack();
                if(temp == null) break;
                root = temp;
            }
        }
        
        BSPLeaf insert = new BSPLeaf(norm,origin);
        insert.setRenderable(this);

        if(infront)
            root.setFront(insert);
        else
            root.setBack(insert);
        
    }
    
}
