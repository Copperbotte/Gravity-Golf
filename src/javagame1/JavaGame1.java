/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javagame1;

import javax.swing.*;


/**
 *
 * @author joseph_kessler
 */
public class JavaGame1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        
        JFrame frame = new JFrame("Joe's Gravity Golf");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.getContentPane().add(new ReboundPanel());
        frame.pack();
        frame.setVisible(true);
    }
    
}
