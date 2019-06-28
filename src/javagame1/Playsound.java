/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javagame1;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Joe
 */
public class Playsound implements Runnable {
    
    public static void play(String url) throws InterruptedException
    {
        /*
        try{
            AudioClip clip = Applet.newAudioClip(new URL(url));
            clip.play();
            //Thread.sleep(5);
        }
        catch(MalformedURLException e){System.out.println(e);}
        */
    }

    @Override
    public void run() {
        try{Playsound.play("file:rollDie.wav");}
        catch(InterruptedException e)
        {System.out.println(e);}
    }
    
}
