/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javagame1;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;

/**
 *
 * @author Joe
 */
public class RenderablePoint implements Renderable {
    
    private CustomVec[] pos;
    private BSPLeaf parent;
    private ImageIcon image;
    private double scale;
    
    private boolean sleep;
    
    private double shift[];
    
    @Override
    public boolean isPurgable(){return true;}
    public CustomVec getVec(int dim){return pos[dim%pos.length];}
    public void setVec(int dim, CustomVec In){pos[dim%pos.length] = In;}
    public BSPLeaf getRoot(){return parent;}
    public void setRoot(BSPLeaf In){parent = In;}
    public ImageIcon getImage(){return image;}
    public void setImage(ImageIcon In){image = In;}
    public double getScale(){return scale;}
    public void setScale(double in){scale = in;}
    public boolean getSleep(){return sleep;}
    public void setSleep(boolean in){sleep = in;}
    public double getShift(int dim){return shift[dim%shift.length];}
    public void setShift(int dim, double In){shift[dim%shift.length] = In;}
    
    
    RenderablePoint(double x, double y, double z, ImageIcon mage)
    {
        shift = new double[2];
        pos = new CustomVec[3];
        for(int i=0;i<3;i++)
            pos[i] = new CustomVec();
        pos[0].setPos(0, x);
        pos[1].setPos(0, y);
        pos[2].setPos(0, z);
        parent = null;
        image = mage;
        scale = 1;
        sleep = false;
    }
    
    public void Update(double dt)
    {
        for(int i=0;i<3;i++)
            pos[i].Update(dt);
    }
    
    public void AppendToBottom(BSPLeaf root, double[] campos)
    {
        double[] uvw = new double[3];
        for(int i=0;i<3;i++)
            uvw[i] = pos[i].getPos(0);
        
        boolean infront = false;
        BSPLeaf temp;
        while(true)
        {
            infront = root.inFront(uvw);
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
        
        double[] norm = new double[3];
        for(int i=0;i<3;i++)
            norm[i] = uvw[i] - campos[i];
        
        
        if(parent == null)
        {
            parent = new BSPLeaf(norm,uvw);
            parent.setRenderable(this);
        }
        else parent.reInit(norm,uvw);
        
        if(infront)
            root.setFront(parent);
        else
            root.setBack(parent);
        
    }
    
    @Override
    public void Render(ReboundPanel graphics, Graphics page, double[] campos, boolean isfront) {
        AffineTransform FaceScale = new AffineTransform();
        AffineTransform FaceLift = new AffineTransform();
        AffineTransform FacePos = new AffineTransform();
        double image_dub = -(double)image.getIconWidth()/2.0; // image dub centers the transform.
        FaceLift.setTransform(1, 0, 0, 1, image_dub*(1+shift[0]), image_dub*(1+shift[1]));
        Graphics2D gud = (Graphics2D)page.create();
        
        double bubble[] = {0,0,0,1};
        
        for(int n=0;n<3;n++)
            bubble[n] = pos[n].getPos(0);
        bubble[3] = 1;
        
        double component = scale*graphics.IMAGE_SCALE/(graphics.TransformBSP(bubble) * -image_dub);

        FaceScale.setTransform(component, 0, 0, component, 0, 0); // Scale is where the magic happens, change the 1/w to become SCALE/w.
        FaceLift.setTransform(1, 0, 0, 1, image_dub, image_dub);
        FacePos.setTransform(1, 0, 0, 1, bubble[0], bubble[1]);
        FaceScale.concatenate(FaceLift);
        FacePos.concatenate(FaceScale);
        
        gud.setTransform(FacePos);
        image.paintIcon(graphics, gud, 0, 0);
        
        //BSPLeaf.yolostring += "Sprite -> ";
    }
    
}
