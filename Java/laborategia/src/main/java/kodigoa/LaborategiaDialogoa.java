package kodigoa;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

// LaborategiaDialogoa klasea, JDialog erabiltzen duena interfaze grafikoa sortzeko.
public class LaborategiaDialogoa extends JDialog implements ActionListener {

    // Elementu grafikoen definizioa
    JRadioButton borobil, karratu;
    ButtonGroup puntoRecogida; // RadioButton talde bat (une honetan ez da erabiltzen)
    JTextField text1, text2; // Testu-eremuak laborategiaren izena eta IP helbidea jasotzeko
    JLabel label1 = new JLabel("Laborategiaren izena: "); // Etiketa testu-eremuari dagokiona
    JLabel label2 = new JLabel("Laborategiaren ip helbidea: "); // Beste etiketa testu-eremuari dagokiona
    JPanel panelTotxo = new JPanel(new BorderLayout(0, 10)); // Panela osagaiak antolatzeko
    JButton bOk = new JButton("OK"); // "OK" botoia
    JButton bCancel = new JButton("Cancelar"); // "Cancelar" botoia
    boolean sortu = false; // Ezarri gabe dagoen aldagai boolearra
    Laborategia laborategia = null; // Sortuko den laborategia objektua

    // Eraikitzailea, JDialog konfiguratzen du.
    public LaborategiaDialogoa(JFrame leihoa, String titulua, boolean modua) {
        super(leihoa, titulua, modua);
        this.setSize(500, 400); // Leihoaren tamaina
        this.setLocationRelativeTo(null); // Leihoa pantailaren erdian kokatzen da
        this.setContentPane(crearPanelVentana()); // Leihoaren edukia sortzen du
        this.setVisible(true); // Leihoa erakusten du
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Leihoa ixtean baliabideak askatzen ditu
    }

    // Edukien panela sortzen du
    private Container crearPanelVentana() {
        panelTotxo.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Margenak gehitzen dizkio
        panelTotxo.add(sortuTestuPanelaKarratu(), BorderLayout.CENTER); // Testu-eremuen panela gehitzen du
        panelTotxo.add(crearPanelBotones(), BorderLayout.SOUTH); // Botoien panela behealdean kokatzen du
        return panelTotxo;
    }

    // Testu-eremuen panela sortzen du
    private Component sortuTestuPanelaKarratu() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 0, 20)); // GridLayout egitura duen panela sortzen da
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Animaliaren datuak"),
            BorderFactory.createEmptyBorder(20, 20, 20, 20))); // Bordeak eta titulua gehitzen dizkio

        text1 = new JTextField(20); // Izena jasotzeko testu-eremua
        text2 = new JTextField(20); // IP helbidea jasotzeko testu-eremua
        panel.add(label1); // Lehenengo etiketa gehitzen da
        panel.add(text1); // Lehenengo testu-eremua gehitzen da
        panel.add(label2); // Bigarren etiketa gehitzen da
        panel.add(text2); // Bigarren testu-eremua gehitzen da

        return panel; // Sortutako panela bueltatzen du
    }

    // Botoien panela sortzen du
    private Component crearPanelBotones() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0)); // Botoien panela, GridLayout erabiliz
        bOk.setActionCommand("ok"); // "OK" botoiari komando bat esleitzen zaio
        bOk.addActionListener(this); // ActionListener gehitzen zaio
        bCancel.setActionCommand("cancel"); // "Cancelar" botoiari komando bat esleitzen zaio
        bCancel.addActionListener(this); // ActionListener gehitzen zaio
        panel.add(bOk); // "OK" botoia gehitzen da
        panel.add(bCancel); // "Cancelar" botoia gehitzen da
        return panel; // Botoien panela bueltatzen du
    }

    // ActionListener interfaze metodoa
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == "cancel") { 
            // "Cancelar" botoia sakatzen bada
            LaborategiaDialogoa.this.dispose(); // Leihoa ixten du
        } else if (e.getActionCommand() == "ok") {
            // "OK" botoia sakatzen bada
            LaborategiaDialogoa.this.dispose(); // Leihoa ixten du
            laborategia = new Laborategia(text1.getText(), text2.getText()); 
            // Laborategia objektua sortzen du erabiltzaileak idatzitako datuekin
        }
    }

    // Sortutako laborategia bueltatzeko metodoa
    public Laborategia getLaborategia() {
        return laborategia;
    }
}