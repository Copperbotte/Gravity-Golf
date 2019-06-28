/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javagame1;

import java.awt.Graphics;

/**
 *
 * @author Joe
 */
public interface Renderable {
    boolean isPurgable();
    void Render(ReboundPanel graphics, Graphics page, double[] campos, boolean isfront);
}
