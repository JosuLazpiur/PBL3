package kodigoa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class NireAdaptadorea implements ListCellRenderer<Laborategia> {
    @Override
	public Component getListCellRendererComponent(JList<? extends Laborategia> list, Laborategia c,  int index, boolean isSelected,  boolean cellHasFocus)  {
		JPanel panel = new JPanel(new BorderLayout(10,0));
		
		panel.add(crearDatos(c), BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createLineBorder(Color.RED)));
		panel.setBackground(isSelected ? Color.BLUE : Color.WHITE);
		panel.setOpaque(true);
	
		return panel;
	}

	private Component crearDatos(Laborategia c) {
		JPanel panel = new JPanel (new BorderLayout(0,10));
		JPanel panelInterior = new JPanel(new GridLayout(3,2));
		Color koloreaJendea = Color.RED;
		Color koloreaGasak = Color.RED;
		String gasTextua = "Arriskuan";
		String jendeTextua = "Ez dago";

		if(!c.isGasak()){ // True edo false den begiratu kolorea eta textua aldatzeko
			koloreaGasak = Color.GREEN;
			gasTextua = "Arriskurik ez";
		}
		if(c.isJendea()){ // True edo false den begiratu kolorea eta textua aldatzeko
			koloreaJendea = Color.GREEN;
			jendeTextua = "Badago";
		}

		panelInterior.add(sortuAtala(" ", c.getIzena().toString(), new Font("Arial", Font.BOLD, 16), Color.BLACK));
		panelInterior.add(sortuAtala("Gasak: ", gasTextua, new Font("Arial", Font.BOLD, 16), koloreaGasak));
		panelInterior.add(sortuAtala(" ", c.getIp_helbidea().toString(), new Font("Arial", Font.BOLD, 16), Color.BLACK));
		panelInterior.add(sortuAtala("Jendea: ", jendeTextua, new Font("Arial", Font.BOLD, 16), koloreaJendea));

		panel.add(panelInterior);

		return panel;
	}
	private Component sortuAtala (String titulo, String valor, Font font, Color color) {
		JLabel label = new JLabel (titulo + " " + valor);

		label.setFont(font);
		label.setForeground(color);
		
		return label;
	}
}
