package kodigoa;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JPanel;

public class NirePanela extends JPanel { 
	Image irudia;
	
	public NirePanela (Image imagen) {
		this.irudia = imagen;
	}

	@Override
	protected void paintComponent(Graphics g) { //LoginPaneleko fondoa margotzeko
		// TODO Auto-generated method stub
		Graphics2D gr = (Graphics2D) g;
		if (irudia != null) {
			gr.drawImage(irudia,0,0, this.getWidth(), this.getHeight(), this);
		//	this.setOpaque(false);
		} else super.paintComponent(g);
	}
}
