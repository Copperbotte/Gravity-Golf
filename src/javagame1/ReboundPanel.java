/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javagame1;

import java.awt.*;
import java.awt.event.*;

import static java.lang.Math.random;
import java.util.ArrayList;
import javax.swing.*;

/**
 * 
 *
 * @author joseph_kessler
 */
public class ReboundPanel extends JPanel
{
    private final int WIDTH = 800, HEIGHT = 600;
    private final double Aspect;
    private final int DELAY = 1;//16;
    public double IMAGE_SCALE;
    private double VIEWPORT_SCALE = 1;
    private double[] VIEWPORT_SCALE_TARGETS = {1,2};

    boolean[] Key_Array;
    
    private BSPLeaf BSPRoot;
    private AttractorLeaf AttractorRoot;
    private RenderablePoint GolfBall;
    private RenderablePoint[] Reticle;
    
    public ImageIcon golf,star,face,shine,shrekshine,goal;

    private RenderablePoint hole;
    private RenderablePoint[] fireworks;
    private boolean win;
    
    private int Hits;
    
    private Matrix ElOrtho,ElProjo;
    private Matrix CamRotate, CamTilt;
    private CustomVec Pitch, Yaw;
    private CustomVec[] CursorAngles;
    private CustomVec[] Position;
    private Matrix World;
    
    
    private Timer timer;
    private Timer soundDelay;
    private boolean canPlaySound;
    Thread soundthread;
            
            
            
    private long InitTime, PrevTime, CurTime;
    
    
    
    private void bounceSound()
    {
        if(!canPlaySound)return;
        soundthread = new Thread(new Playsound());
        soundthread.start();
        canPlaySound = false;
        soundDelay.restart();
    }
    
    public ReboundPanel()
    {
        Hits = 0;
        Pitch = new CustomVec();
        Yaw = new CustomVec();
        Position = new CustomVec[3];
        for(int i=0;i<3;i++)
            Position[i] = new CustomVec();
        
        CursorAngles = new CustomVec[2];
        for(int i=0;i<2;i++)
            CursorAngles[i] = new CustomVec();
        
        Key_Array = new boolean[256];
        
        InitTime = System.currentTimeMillis();
        PrevTime = InitTime;
        
        GolfListener listen = new GolfListener();
        addKeyListener(listen);
        setFocusable(true);
        
        canPlaySound = true;
        timer = new Timer(DELAY, listen);
        soundDelay = new Timer(100, new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                canPlaySound = true;
                soundDelay.stop();
            }});
        golf = new ImageIcon("Golf_ball.gif");
        star = new ImageIcon("star.png");
        face = new ImageIcon("happyFace.gif");
        shine = new ImageIcon("shine.png");
        shrekshine = new ImageIcon("shrekshine.png");
        goal = new ImageIcon("brazil.png");
        
        
        ElOrtho = new Matrix(4,4);
        double bubble = (double)WIDTH/2.0;
        ElOrtho.setData(0, 0, bubble);
        ElOrtho.setData(3, 0, bubble);
        bubble = (double)HEIGHT/2.0;
        ElOrtho.setData(1, 1, bubble);
        ElOrtho.setData(3, 1, bubble);
        Aspect = (double)HEIGHT/(double)WIDTH;

        //*/
        ElProjo = new Matrix(4,4);
        for(int i=0;i<3;i++)
            ElProjo.setData(i, i, VIEWPORT_SCALE);
        ElProjo.setData(3, 2, 1); // Set the z-transform to 1, internet said so
        ElProjo.setData(2, 3, 1); // Set the w-component to equal the z-component
        ElProjo.setData(3, 3, 0); // Set the w-transform to 0
        
        CamRotate = new Matrix(4,4);
        CamTilt = new Matrix(4,4);
        World = new Matrix(4,4);
        
        String target;
        //target = "JBSP Sample.txt";
        target = "Alpha Alley.txt";
        
        BSPRoot = BSPLeaf.loadJBSPFile(target);
        AttractorRoot = AttractorLeaf.loadAttractorOctree("Alpha Alley Octree.txt");
        AttractorRoot.UnOptomize();
        
        
        GolfBall = new RenderablePoint(0.5,-1.01,0.5,golf);
        GolfBall.setScale(0.5);
        GolfBall.setSleep(true);
        
        Reticle = new RenderablePoint[8];
        for(int i=0;i<Reticle.length;i++)
        {
            Reticle[i] = new RenderablePoint(0,0,0,shine);
            Reticle[i].setScale(0.33333);
        }            
        Reticle[Reticle.length-1].setScale(0.6666666);
        Reticle[Reticle.length-1].setImage(shrekshine);
        
        hole = new RenderablePoint(0.25,0.1,2,goal);
        win = false;
        
        //BSPLeaf.SaveJBSPFile(BSPRoot,"Alpha Alley.txt");
        

        setPreferredSize(new Dimension(WIDTH,HEIGHT));
        setBackground(Color.black);
        timer.start();
    }
    
    
    private class GolfListener implements ActionListener, KeyListener
    {
        @Override
        public void actionPerformed(ActionEvent event)
        {
            repaint();
        }
        
        public void keyTyped(KeyEvent ke) {
        }

        public void keyPressed(KeyEvent ke) {
            int code = ke.getKeyCode();
            Key_Array[code%256] = true;
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        public void keyReleased(KeyEvent ke) {
            int code = ke.getKeyCode();
            Key_Array[code%256] = false;
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
   
    }
    
    public void coolide(RenderablePoint in, BSPLeaf root, double Energy)
    {
        if(root == null) return;
        if(!(root.getRenderable() instanceof RenderablePolygon)) return;
        
        coolide(in,root.getBack(),Energy);
        coolide(in,root.getFront(),Energy);
        
        RenderablePolygon splish = (RenderablePolygon)root.getRenderable();
        
        double[] cur = {0,0,0};
        double[] prev = {0,0,0};
        for(int i=0;i<3;i++)
        {
            cur[i] = in.getVec(i).getPos(0);
            prev[i] = in.getVec(i).getPrevPos(0);
        }
        
        boolean current = root.inFront(cur);
        boolean flow = root.inFront(prev);
        if(current == flow) return; // if the previous and current times are on the same side, ignore.
        
        // Test each point to see if the current point crosses toward the same direction.

        double[][] SWOLL = splish.getVertexBuffers();
        double[] Surface = root.getNormal();
        
        boolean dir = false;
        boolean dirset = true;
        for(int i=0;i<splish.getVertexLength();i++)
        {
            double[] couldron = new double[3];
            double[] bubble = new double[3];
            for(int n=0;n<3;n++)
            {
                double potion = SWOLL[n][i]; // origin vector
                bubble[n] = cur[n] - potion; // test point
                couldron[n] = SWOLL[n][(i+1)%splish.getVertexLength()] - potion; // next vector
            }
            
            double[] cross = HandyFuncs.cross(bubble, couldron);
            //HandyFuncs.normalize(cross);
            double ret = HandyFuncs.dot(cross, Surface);
            //if(ret < 0) return;/* // > for internal reflections, < for external.
            if(dirset)
            {
                if(ret > 0)
                    dir = true;
                else
                    dir = false;
                dirset = false;
            }
            else
            {
                if(dir)
                {
                    if(ret <= 0) return;
                }
                else
                {
                    if(ret >= 0) return;
                }
            }
            //*/
        }
        
        double T = Surface[3];
        double[] dPos = new double[3];
        for(int i=0;i<3;i++)
        {
            dPos[i] = cur[i] - prev[i];
            T -= Surface[i] * prev[i];
        }
        double Sum = 0;
        for(int i=0;i<3;i++)
            Sum += Surface[i] * dPos[i];
        T /= Sum;
        for(int i=0;i<3;i++)
        {
            double adjust = 0;
            /*
            adjust = Surface[i] * 0.05;
            if(dir)adjust *= -1;
            */
            in.getVec(i).setPos(0, prev[i] + dPos[i]*T + adjust);
            cur[i] = in.getVec(i).getPos(1);// exchange position for velocity
        }

        double splash = HandyFuncs.dot(cur, Surface);
        Sum = 0;
        for(int i=0;i<3;i++)
        {
            double vel = cur[i] - 2*splash*Surface[i];
            in.getVec(i).setPos(1, vel*Energy);
            Sum += vel*Energy*vel*Energy;
        }
        
        if(Math.sqrt(Sum) < 0.0001)
            in.setSleep(true);

        //if(!win) bounceSound();
    }
    
    public double TransformBSP(double[] bubble)
    {
        if(bubble.length != 4) return 0;
        bubble[3] = 1;
        
        World.mul(bubble);
        CamRotate.mul(bubble);
        CamTilt.mul(bubble);
        ElProjo.mul(bubble);
        
        if(bubble[3] < 0) // This prevents projecting backwards.
            return 0;
        
        double w = bubble[3];
        
        for(int n=0;n<4;n++)
            bubble[n] /= w;
        
        ElOrtho.mul(bubble);
        return w;
    }
    
    private void KeyMove(CustomVec vec, int DownKey, int UpKey, double rate, double dt)
    {
        vec.setPos(1, 0);
        double sum = 0;
        if(Key_Array[UpKey])
            sum += rate;
        if(Key_Array[DownKey])
            sum -= rate;
        vec.setPos(1, sum);
        vec.Update(dt);
    }
    
    @Override
    public void paintComponent(Graphics page)
    {
        super.paintComponent(page);
        PrevTime = CurTime;
        CurTime = System.currentTimeMillis() - InitTime;
        double time = (double)(CurTime)/1000.0;
        double dt = (double)(CurTime - PrevTime);
        
        double windt = dt;
        if(win)dt = 0;
        
        Color cool = page.getColor();
        
        
        double viewport_dir;
        if(Key_Array[KeyEvent.VK_SHIFT])
            viewport_dir = VIEWPORT_SCALE_TARGETS[1];
        else
            viewport_dir = VIEWPORT_SCALE_TARGETS[0];
        
        VIEWPORT_SCALE += 0.01*(viewport_dir - VIEWPORT_SCALE)*dt;
        for(int i=0;i<3;i++)
            ElProjo.setData(i, i, VIEWPORT_SCALE);
        
        
        
        double dip = (double)getSize().width/2.0;
        IMAGE_SCALE = VIEWPORT_SCALE*50*dip / (double)WIDTH;
        ElOrtho.setData(0, 0, dip);
        ElOrtho.setData(3, 0, dip);
        dip *= Aspect;
        ElOrtho.setData(1, 1, dip);
        ElOrtho.setData(3, 1, (double)getSize().height/2.0); // Translation doesn't use aspect ratio, keeps it in the middle.
        

        
        double genericrate = 1.0/1000.0;
        KeyMove(Pitch,KeyEvent.VK_UP,KeyEvent.VK_DOWN,genericrate,windt);
        KeyMove(Yaw,KeyEvent.VK_LEFT,KeyEvent.VK_RIGHT,genericrate,windt);
        KeyMove(CursorAngles[0],KeyEvent.VK_S,KeyEvent.VK_W,genericrate,windt);
        KeyMove(CursorAngles[1],KeyEvent.VK_D,KeyEvent.VK_A,genericrate,windt);
        //if(CursorAngles[0].getPos(0) > D3DX_PI/2.0)
        
        
        double pcos = Math.cos(Pitch.getPos(0));
        double psin = Math.sin(Pitch.getPos(0));
        double ycos = Math.cos(Yaw.getPos(0));
        double ysin = Math.sin(Yaw.getPos(0));
        
        CamRotate.init();
        CamRotate.setData(0, 0, ycos);
        CamRotate.setData(2, 0, ysin);
        CamRotate.setData(0, 2, -ysin);
        CamRotate.setData(2, 2, ycos);
        
        CamTilt.init();
        CamTilt.setData(1, 1, pcos);
        CamTilt.setData(2, 1, psin);
        CamTilt.setData(1, 2, -psin);
        CamTilt.setData(2, 2, pcos);
        
        //KeyMove(Position[1],KeyEvent.VK_CONTROL,KeyEvent.VK_SHIFT,genericrate,dt);
        double ingredients[] = {0,0,3};
        Matrix couldron = new Matrix(3,3);
        double angles[] = {Pitch.getPos(0),Yaw.getPos(0)};
        int directions[] = {1,0};
        for(int i=0;i<2;i++)
        {
            couldron.init();
            double theta = angles[i];// + Math.PI;// / 2;
            double coolcos = Math.cos(theta);
            double coolsin = Math.sin(theta);
            couldron.setData(directions[i], 0, coolcos);
            couldron.setData(2, directions[i], -coolsin);
            couldron.setData(directions[i], 2, coolsin);
            couldron.setData(2, 2, coolcos);
            couldron.mul(ingredients);
        }
        for(int i=0;i<3;i++)
            Position[i].setPos(0,ingredients[i]);
        
        /*
        //FPS Controls
        KeyMove(Position[1],KeyEvent.VK_CONTROL,KeyEvent.VK_SHIFT,genericrate,dt);
        KeyMove(Position[0],KeyEvent.VK_D,KeyEvent.VK_A,genericrate,0); // no dt here, use it again later
        KeyMove(Position[2],KeyEvent.VK_W,KeyEvent.VK_S,genericrate,0);
        double posrate[] = {Position[0].getPos(1),Position[2].getPos(1)};
        Matrix posrotate = new Matrix(2,2);
        posrotate.setData(0, 0, ycos);
        posrotate.setData(1, 0, -ysin);
        posrotate.setData(0, 1, ysin);
        posrotate.setData(1, 1, ycos);
        posrotate.mul(posrate);
        if(posrate[0] > 1)
            System.out.print("LUDICROUS SPEED");
        for(int i=0;i<2;i++)
        {
            Position[2*i].setPos(1, posrate[i]);
            Position[2*i].Update(dt);
        }
        */
        
        
        

      
        double campos[] = {0,0,0,1};
        
        double bub = -1;// + 2*Math.cos(down * camspeed);
        double scrub = -1;// + 2*Math.sin(down * camspeed);
        double camera[] = {bub,0,scrub};
        for(int i=0;i<3;i++)
            camera[i] += Position[i].getPos(0);
        
        
        for(int i=0;i<3;i++)
        {
            World.setData(3, i, camera[i]);
            campos[i] -= camera[i];
        }
        
        BSPLeaf.purge(BSPRoot);
        
        if(GolfBall.getSleep())
        {
            ingredients[0] = 0;
            ingredients[1] = 0;
            ingredients[2] = 1;
            
            couldron.init();
            for(int i=0;i<2;i++)
            {
                couldron.init();
                double theta = CursorAngles[i].getPos(0);
                double coolcos = Math.cos(theta);
                double coolsin = Math.sin(theta);
                couldron.setData(directions[i], 0, coolcos);
                couldron.setData(2, directions[i], -coolsin);
                couldron.setData(directions[i], 2, coolsin);
                couldron.setData(2, 2, coolcos);
                couldron.mul(ingredients);
            }
            
            for(int i=0;i<Reticle.length;i++)
            {
                double scale = HandyFuncs.Lerp(0, 0.5, (double)(i+1)/(double)Reticle.length);
                for(int n=0;n<3;n++)
                    Reticle[i].getVec(n).setPos(0,GolfBall.getVec(n).getPos(0) + scale * ingredients[n]);
                Reticle[i].AppendToBottom(BSPRoot, campos);
            }
            
            if(Key_Array[KeyEvent.VK_ENTER])
            {
                Hits++;
                GolfBall.setSleep(false);
                for(int i=0;i<3;i++)
                    GolfBall.getVec(i).setPos(1,ingredients[i] / 1500.0);
            }
            
        }
        
        
        if(!GolfBall.getSleep())
        {
            for(int n=0;n<3;n++)GolfBall.getVec(n).setPos(2, 0);
            AttractorRoot.traverseAndAttract(GolfBall);
            
            GolfBall.Update(dt);
            coolide(GolfBall,BSPRoot,0.9);
        }
        GolfBall.AppendToBottom(BSPRoot,campos);
        
        double sum = 0;
        for(int i=0;i<3;i++)
        {
            double temp = GolfBall.getVec(i).getPos(0) - hole.getVec(i).getPos(0);
            sum += temp*temp;
        }
        if(sum < 0.01) // 0.1^2
        {
            if(!win)
            {
                fireworks = new RenderablePoint[500];
                for(int i=0;i<500;i++)
                {
                    fireworks[i] = new RenderablePoint(0,0,0,shine);
                    fireworks[i].setScale(0.1);
                    if(i == 0)
                    {
                        fireworks[i].setImage(star);
                        fireworks[i].setScale(2);
                    }
                    for(int n=0;n<3;n++)
                    {
                        fireworks[i].getVec(n).setPos(0, GolfBall.getVec(n).getPos(0));
                        fireworks[i].getVec(n).setPos(1, HandyFuncs.Lerp(-1/1500.0, 1/1500.0, Math.random()));
                    }
                }
                
            }
            win = true;
        }
        
        if(win)
        {
            double[] pos = new double[3];
            for(int i=0;i<3;i++)
                pos[i] = GolfBall.getVec(i).getPos(0);
            for(int i=0;i<500;i++)
            {
                for(int n=0;n<3;n++)
                    fireworks[i].getVec(n).setPos(2, 0);
                AttractorRoot.traverseAndAttract(fireworks[i]);
                fireworks[i].Update(windt);
                coolide(fireworks[i],BSPRoot,1);
                fireworks[i].AppendToBottom(BSPRoot, campos);
            }
        }
        
        hole.AppendToBottom(BSPRoot, campos);
        
        BSPRoot.traverseAndDraw(this, page, campos);//0.75, 0.5, 0);
        page.setColor(Color.WHITE);
        page.drawString("Press the Arrow keys to move the camera",0 ,10);
        page.drawString("Press Space to turn the world transparent",0 ,20);
        page.drawString("Press Shift to zoom in",0 ,30);
        page.drawString("Press WASD to move the ball's targeting reticle",0 ,40);
        page.drawString("Press Enter to hit the ball",0 ,50);

        page.drawString("Hits: " + Hits + " / Par 4",0 ,getSize().height);
        
        page.setColor(cool);

    }
    
    
    
}
