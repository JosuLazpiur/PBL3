package kodigoa;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.paho.client.mqttv3.MqttException;

// Printzipala izeneko klasea sortzen da, JFrame klasea heredatzen du eta bi interfaze implementatzen ditu: ListSelectionListener eta PropertyChangeListener
public class Printzipala extends JFrame implements ListSelectionListener, PropertyChangeListener { 
    // JPanel mota duen aldagaia panela izenarekin deklaratzen da
    JPanel panela; 
    // Timer izeneko aldagaia deklaratzen da
    Timer timer; 
    // NireAkzioa motako hiru aldagaia deklaratzen dira: gehitu, kendu eta irten
    NireAkzioa gehitu, kendu, irten; 
    // MqttSuscribe motako objektuen zerrenda deklaratzen da
    List<MqttSuscribe> mqtt; 
    // Laborategia motako objektuen JList bat deklaratzen da
    JList<Laborategia> jlLaborategiak; 
    // Laborategiak klaseko objektu bat deklaratzen da
    Laborategiak laborategiak; 
    // Laborategia motako array bat deklaratzen da bistaratzekoLaborategiak izenarekin
    Laborategia [] bistaratzekoLaborategiak; 
    // Fitxategiaren ibilbidea gordetzeko string aldagaia
    String rutaArchivo; 

    // Bezeroaren ID-a gordetzeko konstantea definitzen da
    public static final String CLIENT_ID = "TemperatureSimulator"; 

    // Printzipala klasearen konstruktorea definitzen da, MqttException-a jaurtitzeko gaitasunarekin
    public Printzipala(String nungoa) throws MqttException {
        // JFrame klasearen izenburua ezartzen da
        super(nungoa + "-ko Laborategiak"); 
        // mqtt zerrenda berria sortzen da
        mqtt = new ArrayList<>(); 
        // Fitxategiaren ibilbidea ezartzen da
        rutaArchivo = "laborategia/laborategiak/" + nungoa + ".txt"; 
        // Ekintzak sortzeko metodoa deitzen da
        sortuAkzioak(); 
        // Laborategiak objektua sortzen da
        laborategiak = new Laborategiak(); 
        try {
            // Laborategiak irakurtzeko metodoa deitzen da
            laborategiakIrakurri(); 
            // MQTT entzutea hastea eragiten da
            entzutenHasi(); 
        } catch (IOException e) {
            // Salbuespena harrapatu eta errorea inprimatzen da
            e.printStackTrace(); 
        }
        // Menu-barra sortzen da
        this.setJMenuBar(crearMenuBar()); 
        // Leihoaren panela ezartzen da
        this.setContentPane(sortuLeihoPanela()); 
        // Leihoa gehienez handitzen da
        this.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        // Leihoaren kokapena pantailaren erdialdean ezartzen da
        this.setLocationRelativeTo(null); 
        // Leihoa ikusgai ezartzen da
        this.setVisible(true); 
        // Itxiera-ekintza ezartzen da JFrame.EXIT_ON_CLOSE erabiliz
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
    }

    // Ekintzak sortzeko metodoa
    private void sortuAkzioak() {
        // Gehitu ekintza sortzen da
        gehitu = new NireAkzioa ("Gehitu", new ImageIcon("laborategia/irudiak/edit_add.png"), "Datuak fitxategian gehitu", new Integer(KeyEvent.VK_B)); 
        // Kendu ekintza sortzen da
        kendu = new NireAkzioa ("Kendu", new ImageIcon("laborategia/irudiak/edit_remove.png"), "Datuak fitxategitik kendu", new Integer(KeyEvent.VK_C)); 
        // Irten ekintza sortzen da
        irten = new NireAkzioa("Irten", new ImageIcon("laborategia/irudiak/exit.jpg"), "Irten erabiltzailetik", new Integer(KeyEvent.VK_V)); 
    }

    // Laborategiak fitxategitik irakurtzeko metodoa
    private void laborategiakIrakurri() throws IOException {
        // Fitxategia irakurtzeko BufferedReader objektua sortzen da
        BufferedReader lector = new BufferedReader(new FileReader(rutaArchivo)); 
        // Lehenengo lerroa irakurtzen da
        String linea = lector.readLine(); 
        
        while (linea != null) {
            // Lerroa komaz banatzen da eta array batean gordetzen da
            String [] textua = linea.split(", "); 
            // Laborategia objektua sortzen da
            Laborategia lab = new Laborategia(textua[0], textua[1]); 
            // Laborategia objektua zerrendara gehitzen da
            laborategiak.add(lab); 
            // Hurrengo lerroa irakurtzen da
            linea = lector.readLine(); 
        }

        lector.close(); // Fitxategia izteko
    }

    // MQTT entzutea hasteko metodoa
    private void entzutenHasi() throws MqttException {
        // Laborategi bakoitzerako MQTT entzuleak sortzen dira
        for (Laborategia lab : laborategiak.getLaborategiak()) { 
            MqttSuscribe mqtt1 = new MqttSuscribe(lab.ip_helbidea, CLIENT_ID); 
            mqtt1.addPropertyChangeListener(this); 
            mqtt.add(mqtt1); 
        }
    }

    // MQTT entzutea amaitzeko metodoa
    private void entzutenBukatu() throws MqttException {
        for (MqttSuscribe mq : mqtt) {
            mq.disconnect(); 
        }
    }

    // Menu-barra sortzeko metodoa
    private JMenuBar crearMenuBar() {
        JMenuBar barra = new JMenuBar(); 
        barra.add (crearMenuCrear()); 
        barra.add(Box.createHorizontalGlue()); 
        barra.add (crearMenuSalir()); 
        return barra; 
    }

    // "Ekintza" menu sortzeko metodoa
    private JMenu crearMenuCrear() {
        JMenu menuEditar = new JMenu ("Ekintza"); 
        menuEditar.setMnemonic(new Integer(KeyEvent.VK_E)); 
        menuEditar.add(gehitu); 
        menuEditar.addSeparator(); 
        menuEditar.add(kendu); 
        return menuEditar; 
    }

    // "Irten" menu sortzeko metodoa
    private JMenu crearMenuSalir() {
        JMenu menuSalir = new JMenu ("Irten"); 
        menuSalir.setMnemonic(new Integer(KeyEvent.VK_S)); 
        menuSalir.add(irten); 
        return menuSalir; 
    }

    // Leihoaren panela sortzeko metodoa, Container objektua itzultzen du
    private Container sortuLeihoPanela() {
        // JPanel objektu bat sortzen da BorderLayout diseinuarekin, 0 zutabe eta 20 errenkada tartearekin
        panela = new JPanel(new BorderLayout(0, 20)); 
        // Panelaren ertzak hutsik uzteko mugak definitzen dira (20 pixel alde bakoitzean)
        panela.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); 
        // Tresna-barra panelaren iparraldean gehitzen da
        panela.add(sortuToolBar(), BorderLayout.NORTH); 
        // Laborategien panela panelaren erdialdean gehitzen da
        panela.add(sortuLaborategienPanela(), BorderLayout.CENTER); 
        // Panela itzultzen da
        return panela; 
    }

    // Tresna-barra sortzeko metodoa, Component objektua itzultzen du
    private Component sortuToolBar() {
        // JToolBar objektu bat sortzen da
        JToolBar toolBar = new JToolBar(); 
        // Tresna-barrari mugak ematen zaizkio (beveled border)
        toolBar.setBorder(BorderFactory.createRaisedBevelBorder()); 

        // "Gehitu" ekintza tresna-barrara gehitzen da
        toolBar.add(gehitu); 
        // "Kendu" ekintza tresna-barrara gehitzen da
        toolBar.add(kendu); 
        // Hutsegite horizontal bat gehitzen da tresna-barra betetzeko
        toolBar.add(Box.createHorizontalGlue()); 
        // "Irten" ekintza tresna-barrara gehitzen da
        toolBar.add(irten); 

        // Tresna-barra itzultzen da
        return toolBar; 
    }

    // Laborategien panela sortzeko metodoa, Component objektua itzultzen du
    private Component sortuLaborategienPanela() {
        // JScrollPane objektua sortzen da bertikal eta horizontal eskalatuarekin
        JScrollPane panel = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
        // JList objektu bat sortzen da laborategiak bistaratuko dituena
        jlLaborategiak = new JList<>(); 
        // JList-aren gelaxka-renderizatzailea pertsonalizatzen da
        jlLaborategiak.setCellRenderer(new NireAdaptadorea()); 
        // Hautapen modua bakarkakoa (SINGLE_SELECTION) ezartzen zaio JList-i
        jlLaborategiak.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
        // ListSelectionListener gehitzen zaio JList-i, aldaketak entzuteko
        jlLaborategiak.addListSelectionListener(this); 
        // Bistaratzeko laborategiak sortzen dira
        bistaratzekoLaborategiak = sortuBistaratzekoak(); 
        // JList-ari bistaratzeko laborategiak ezartzen zaizkio
        jlLaborategiak.setListData(bistaratzekoLaborategiak); 
        // JList-a panelaren bistaratze-eremuan ezartzen da
        panel.setViewportView(jlLaborategiak); 

        // Panela itzultzen da
        return panel; 
    }

    // Bistaratzeko laborategiak sortzeko metodoa, Laborategia motako array bat itzultzen du
    private Laborategia[] sortuBistaratzekoak() { 
        // Laborategien zerrenda eskuratzen da
        List<Laborategia> bistaratzekoakList = laborategiak.getLaborategiak(); 
        // Zerrenda array bihurtzen da eta itzultzen da
        return (bistaratzekoakList.toArray(new Laborategia[0])); 
    }

    // Fitxategi bat sortzeko metodoa, izena eta kokapena parametro gisa hartzen ditu
    private void fitxategiaSortu(String nungoa, String izena) {
        // Fitxategiaren bidea ezartzen da
        String ruta = "laborategia/labBalioak/" + izena + "_" + nungoa + ".txt"; 
        // Fitxategia sortzen da
        File archivo = new File(ruta); 

        try {
            // Fitxategia ez bada existitzen, berria sortzen da
            if (!archivo.exists()) { 
                archivo.createNewFile(); 
            }
        } catch (IOException e) {
            // Salbuespena harrapatu eta errorea inprimatzen da
            e.printStackTrace(); 
        }
    }

    // Fitxategia ezabatzeko metodoa, izena eta kokapena parametro gisa hartzen ditu
    private void fitxategiaEzabatu(String nungoa, String izena) {
        // Fitxategiaren bidea ezartzen da
        String ruta = "laborategia/labBalioak/" + izena + "_" + nungoa + ".txt"; 
        // Fitxategia sortzen da
        File archivo = new File(ruta); 

        // Fitxategia existitzen bada
        if (archivo.exists()) { 
            // Fitxategia ezabatzen saiatzen da
            if (archivo.delete()) { 
                // Ezabatzea arrakastatsua izan dela adierazten da
                System.out.println("Archivo eliminado correctamente."); 
            } else {
                // Ezabatzean errorea izan dela adierazten da
                System.out.println("Error al eliminar el archivo."); 
            }
        } else {
            // Fitxategia ez dela existitzen adierazten da
            System.out.println("El archivo no existe."); 
        }
    }

    // Laborategi bat izen edo IP helbidearen arabera jasotzeko metodoa
    private Laborategia laborategiaJaso(String izenaEdoIP) {
        // Laborategi bakoitza zerrendan aztertzen da
        for (Laborategia lab : laborategiak.getLaborategiak()) { 
            // Izen edo IP helbideak bat egiten badu, laborategia itzultzen da
            if (lab.getIzena().equals(izenaEdoIP) || lab.getIp_helbidea().equals(izenaEdoIP)) { 
                return lab; 
            }
        }
        // Ez bada aurkitzen, null itzultzen da
        return null; 
    }    

    private class NireAkzioa extends AbstractAction {
        String texto;
        // Ekintza mota gordetzeko atributua
    
        public NireAkzioa(String texto, Icon imagen, String descrip, Integer nemonic) {
            super(texto, imagen); // Ekintzaren testua eta ikonoa ezartzen dira
            this.texto = texto; // Ekintzaren testua gordetzen da
            this.putValue(Action.SHORT_DESCRIPTION, descrip); // Laburpen deskribapena ezartzen da
            this.putValue(Action.MNEMONIC_KEY, nemonic); // Teklatu lasterbidea ezartzen da
        }
    
        @Override
        public void actionPerformed(ActionEvent e) {
            if (texto.equals("Gehitu")) { // "Gehitu" ekintza hautatuz gero
                LaborategiaDialogoa berria = new LaborategiaDialogoa(Printzipala.this, "Laborategia sortzen", true); 
                // Laborategi berria sortzeko elkarrizketa leihoa irekitzen du
                Laborategia newLaborategia = berria.getLaborategia(); 
                // Elkarrizketa leihotik datuak lortzen dira
    
                try {
                    if (newLaborategia != null) { 
                        // Laborategia existitzen bada (null ez bada)
                        if (!badagoLaborategia(newLaborategia)) { 
                            // Laborategia ez bada existitzen fitxategian
                            try (BufferedWriter escritor = new BufferedWriter(new FileWriter(rutaArchivo, true))) {
                                escritor.write(newLaborategia.getIzena()); // Izena fitxategian idazten du
                                escritor.write(", "); // Komaz bereizten du
                                escritor.write(newLaborategia.getIp_helbidea()); // IP helbidea idazten du
                                escritor.newLine(); // Lerro berria gehitzen du
                                escritor.close();
                            }
    
                            laborategiak.add(newLaborategia); 
                            // Zerrendan laborategi berria gehitzen da
                            bistaratzekoLaborategiak = sortuBistaratzekoak(); 
                            // Zerrenda eguneratzen da interfazerako
                            jlLaborategiak.setListData(bistaratzekoLaborategiak); 
                            // Eguneratutako zerrenda interfazean bistaratzen da
                            String[] partes = newLaborategia.getIp_helbidea().split("//"); 
                            // IP helbidea zatitzen du
                            String[] split = partes[1].split(":"); 
                            // Portutik banantzen du
                            fitxategiaSortu(split[0], newLaborategia.getIzena()); 
                            // Laborategiari dagokion fitxategia sortzen du
                        } else {
                            JOptionPane.showMessageDialog(panela, "Laborategia edo ip helbide hori jada badaude", "Errorea", JOptionPane.ERROR_MESSAGE); 
                            // Laborategia existitzen bada, errore mezu bat bistaratzen da
                        }
                    }
                } catch (IOException e1) {
                    // Fitxategia idaztean errorea gertatzen bada
                    e1.printStackTrace(); // Errorea inprimatzen da
                }
            } else if (texto.equals("Kendu")) { // "Kendu" ekintza hautatuz gero
                String borratzekoLaborategia = JOptionPane.showInputDialog(panela, "Sartu ezabatu nahi duzun laborategiaren izena edo IP helbidea:", "Laborategia Kendu", JOptionPane.QUESTION_MESSAGE); 
                // Ezabatu nahi den laborategiaren izena edo IP helbidea eskatzen du
    
                if (borratzekoLaborategia != null && !borratzekoLaborategia.trim().isEmpty()) { 
                    // Balioa hutsik ez badago
                    boolean aurkituta = false; 
                    // Aurkitu den adierazteko aldagai bat
    
                    try (BufferedReader lector = new BufferedReader(new FileReader(rutaArchivo));
                        BufferedWriter escritor = new BufferedWriter(new FileWriter("temp.txt"))) {
                        // Aldi baterako fitxategia sortzen da ezabatu behar ez diren datuak gordetzeko
                        String lineaActual;
    
                        while ((lineaActual = lector.readLine()) != null && aurkituta != true) {
                            if (!lineaActual.contains(borratzekoLaborategia)) {
                                // Ezabatu behar ez den lerroa aldi baterako fitxategian idazten du
                                escritor.write(lineaActual);
                                escritor.newLine();
                            } else {
                                aurkituta = true; 
                                // Ezabatu beharreko laborategia aurkituta
                            }
                        }
                        lector.close();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(panela, "Errorea fitxategia irakurtzean edo idaztean.", "Errorea", JOptionPane.ERROR_MESSAGE);
                        // Fitxategi kudeaketan errore bat gertatu bada
                        ex.printStackTrace(); 
                        return;
                    }
    
                    if (aurkituta) { 
                        // Laborategia aurkituta ezabatu egiten da
                        try {
                            java.io.File archivoOriginal = new java.io.File(rutaArchivo); 
                            java.io.File archivoTemporal = new java.io.File("temp.txt"); 
    
                            if (archivoOriginal.delete()) { 
                                if (archivoTemporal.renameTo(archivoOriginal)) {
                                    JOptionPane.showMessageDialog(panela, "Laborategia ondo ezabatu da!", "Arrakasta", JOptionPane.INFORMATION_MESSAGE); 
                                    // Laborategia ondo ezabatu dela jakinarazten da
    
                                    Laborategia labAEliminar = laborategiaJaso(borratzekoLaborategia); 
                                    // Laborategiaren objektua bilatzen du zerrendan
                                    if (labAEliminar != null) {
                                        String[] partes = labAEliminar.getIp_helbidea().split("//");
                                        String[] split = partes[1].split(":");
                                        fitxategiaEzabatu(split[0], labAEliminar.getIzena()); 
                                        // Laborategiari dagokion fitxategia ezabatzen du
                                    }
                                    laborategiak = new Laborategiak(); 
                                    // Laborategien zerrenda hasieratzen du
                                    laborategiakIrakurri(); 
                                    // Fitxategia berriz irakurtzen du
                                    bistaratzekoLaborategiak = sortuBistaratzekoak(); 
                                    // Zerrenda eguneratzen da interfazerako
                                    jlLaborategiak.setListData(bistaratzekoLaborategiak); 
                                    // Interfazean bistaratzen da
                                }
                            }
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(panela, "Errorea fitxategiak kudeatzean: " + ex.getMessage(), "Errorea", JOptionPane.ERROR_MESSAGE);
                            // Errorea fitxategiak ezabatzean edo berrizendatzean
                            ex.printStackTrace();
                        }
                    }
                }
            } else if (texto.equals("Irten")) { 
                System.exit(0); 
                // Aplikazioa amaitzen du
            }
        }
    }    

    public boolean badagoLaborategia(Laborategia lab) throws IOException {
        // Fitxategian laborategia existitzen den egiaztatzeko metodoa
        BufferedReader lector = null;
        lector = new BufferedReader(new FileReader(rutaArchivo)); 
        // Fitxategia irakurtzeko erabiliko da
    
        String linea = lector.readLine(); // Lerro bat irakurri
        boolean berdin = false; // Aurkitzen den adierazteko aldagaia
    
        while (linea != null && berdin == false) {
            // Fitxategiko lerroak irakurri berdina den ala ez egiaztatzeko
            String[] textua = linea.split(", "); 
            // Lerroa zatitzen da izena eta IP helbidea bereizteko
            if (textua[0].equals(lab.getIzena()) || textua[1].equals(lab.getIp_helbidea())) {
                // Izena edo IP helbidea bat badator, laborategia existitzen da
                berdin = true;
            }
            linea = lector.readLine(); // Hurrengo lerroa irakurri
        }
        return berdin; 
        // Egia edo gezurra bueltatzen du laborategia existitzen denaren arabera
    }
    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        // Zerrendako elementu bat hautatzen denean gertatzen den ekintza
        if (e.getValueIsAdjusting()) return; 
        // Aldaketak oraindik ez badira amaitu, ez du ezer egiten
        if (jlLaborategiak.getSelectedIndex() != -1) {
            // Hautatutako laborategi bat badago
            try {
                entzutenBukatu(); 
                // MQTT konexioa amaitzen du
            } catch (MqttException e1) {
                e1.printStackTrace(); 
                // Konexioarekin errorea gertatzen bada, errorea inprimatzen du
            }
            Printzipala.this.dispose(); 
            // Uneko leihoa ixten du
            new LaborategiaPanela(jlLaborategiak.getSelectedValue()); 
            // Hautatutako laborategiaren panela irekitzen du
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Propietate aldaketa bat gertatzen denean exekutatzen den metodoa
        double balioa = (double) evt.getNewValue(); 
        // Propietatearen balio berria lortzen da
        switch (evt.getPropertyName()) {
            case "aldatuGasa":
                // "Gasa" propietatea aldatu bada
                for (int i = 0; i < laborategiak.getLaborategiak().size(); i++) {
                    // Laborategi bakoitza begiratzen du
                    if (laborategiak.getLaborategiak().get(i).getIp_helbidea() == evt.getOldValue()) {
                        // Propietate aldaketa dagokion laborategiarena den egiaztatzen du
                        if (balioa >= 1000) {
                            // Gasa maila 1000 edo handiagoa bada
                            laborategiak.getLaborategiak().get(i).setGasak(true); 
                            // Laborategi horretan gasa detektatu dela adierazten du
                        } else {
                            laborategiak.getLaborategiak().get(i).setGasak(false); 
                            // Gasa ez dela detektatu adierazten du
                        }
    
                        jlLaborategiak.setCellRenderer(new NireAdaptadorea()); 
                        // Zerrendaren bistaratzeko modua eguneratzen du
                        bistaratzekoLaborategiak = sortuBistaratzekoak(); 
                        // Laborategien zerrenda berria sortzen du
                        jlLaborategiak.setListData(bistaratzekoLaborategiak); 
                        // Zerrenda interfazean eguneratzen da
                    }
                }
                break;
            case "aldatuJendea":
                // "Jendea" propietatea aldatu bada
                for (int i = 0; i < laborategiak.getLaborategiak().size(); i++) {
                    if (laborategiak.getLaborategiak().get(i).getIp_helbidea() == evt.getOldValue()) {
                        // Propietate aldaketa dagokion laborategiarena den egiaztatzen du
                        if (balioa == 1) {
                            // Jendea badago
                            laborategiak.getLaborategiak().get(i).setJendea(true); 
                            // Laborategian jendea badagoela adierazten du
                        } else {
                            laborategiak.getLaborategiak().get(i).setJendea(false); 
                            // Jendea ez dagoela adierazten du
                        }
    
                        jlLaborategiak.setCellRenderer(new NireAdaptadorea()); 
                        // Zerrendaren bistaratzeko modua eguneratzen du
                        bistaratzekoLaborategiak = sortuBistaratzekoak(); 
                        // Laborategien zerrenda berria sortzen du
                        jlLaborategiak.setListData(bistaratzekoLaborategiak); 
                        // Zerrenda interfazean eguneratzen da
                    }
                }
                break;
        }
    }    
}