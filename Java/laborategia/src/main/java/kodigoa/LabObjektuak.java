package kodigoa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Timer;

import org.eclipse.paho.client.mqttv3.MqttException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LabObjektuak extends JPanel implements ActionListener {
    public String BROKER; // MQTT brokeraren helbidea
    public static final String CLIENT_ID = "TemperatureIdazle"; // MQTT client ID-a
    final static int DIMENSIONX = 800; // Panelaren zabalera
    final static int DIMENSIONY = 900; // Panelaren altuera
    List<Drawable> dibujables; // Marraztu daitezkeen objektuak
    private Image background; // Fondoaren irudia
    Color kolorea = Color.WHITE; // Fondeko hasierako kolorea
    JLabel alarma; // Alarma label-a
    Timer timer; // Tenperatura aldaketa behatzeko timerra
    double balioGasa; // Gasa balioa
    MqttPublish mqttPublish; // MQTT publikatzailea
    boolean piztuta = false;

    // Eraikitzaile metodoa, 'dibujables' eta 'ip' parametroak jasotzen ditu
    public LabObjektuak(List<Drawable> dibujables, String ip) {
        super();
        this.dibujables = dibujables; // 'dibujables' lista hasieratzen da
        BROKER = ip; // Brokeraren helbidea jasotzen da parametro bezala
        configurarFondo(); // Fondoaren konfigurazioa egitea
        this.setPreferredSize(new Dimension(DIMENSIONX, DIMENSIONY)); // Panelaren dimentsioak ezartzen dira
        timer = null; // Timerra hasieratu gabe

        // Alarma ON botoia sortzen da
        JButton alarmaOnButton = new JButton(new ImageIcon("laborategia/irudiak/alarmaON.png")); // Alarma ON botoiarentzat irudia kargatzen da
        alarmaOnButton.setBounds(428, 70, 50, 50); // Alarma ON botoiaren kokapena

        alarmaOnButton.setActionCommand("AlarmaON"); // Botoiaren ekintza komandoa
        alarmaOnButton.addActionListener(this); // Ekintza entzulea gehitzen da

        this.setLayout(null); // Panelaren layout-a ez dago antolatuta (null layout)
        this.add(alarmaOnButton); // Botoia panela gaineratzen da
        
        // Alarma OFF botoia sortzen da
        JButton alarmaButton = new JButton(new ImageIcon("laborategia/irudiak/alarmaOFF.png")); // Alarma OFF botoiarentzat irudia kargatzen da
        alarmaButton.setBounds(428, 130, 50, 50); // Alarma OFF botoiaren kokapena

        alarmaButton.setActionCommand("AlarmaOFF"); // Botoiaren ekintza komandoa
        alarmaButton.addActionListener(this); // Ekintza entzulea gehitzen da

        this.setLayout(null); // Panelaren layout-a ez dago antolatuta (null layout)
        this.add(alarmaButton); // Botoia panela gaineratzen da
    }

    // Fondoaren konfigurazioa egiteko metodoa
    private void configurarFondo() {
        String icono = "laborategia/irudiak/laborategia.jpg"; // Fondoaren irudiaren bidea
        this.background = new ImageIcon(icono).getImage(); // Irudia kargatzen da
    }

    // Kolorea ezartzen duen metodoa
    public void setKolorea(Color kolorea) {
        this.kolorea = kolorea; // Kolorea ezartzen da
    }

    // Kolorea lortzeko metodoa
    public Color getKolorea() {
        return kolorea; // Kolorea itzultzen da
    }

    // Gasa balioa lortzeko metodoa
    public double getBalioGasa() {
        return balioGasa; // Gasa balioa itzultzen da
    }

    // Gasa balioa ezartzeko metodoa
    public void setBalioGasa(double balioGasa) {
        this.balioGasa = balioGasa; // Gasa balioa ezartzen da
        if(balioGasa >= 1000) { // Balio gasa 1000 edo handiagoa bada
            startFondoAldaketa(); // Fondoaren aldaketa hasteko metodoa deitzen da
        }
    }

    // Fondoaren aldaketa hasteko metodoa
    public void startFondoAldaketa() {
        if(timer == null){ // Timerra ez badago
            timer = new Timer(1000, this); // Timerra sortzen da eta 1 segundoan behin ekintza egiten da
            timer.start(); // Timerra abiatzen da
        }
    }

    // Fondoaren aldaketa gelditzeko metodoa
    public void stopFondoAldaketa(){
        if(timer != null){ // Timerra badago
            timer.stop(); // Timerra gelditzen da
            timer = null; // Timerra null bihurtzen da
        }
    }

    // Panela marrazteko metodoa
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Panela garbitzen dela egiaztatzen da
        Graphics2D gr = (Graphics2D) g; // Graphics objektua Graphics2D bihurtzen da

        // Fondoaren kolorea ezarri eta betetzen da
        gr.setColor(kolorea);
        gr.fillRect(0, 0, getWidth(), getHeight());

        // Fondoa zentratuta marraztu
        if (this.background != null) {
            int imgWidth = background.getWidth(null); // Irudiaren zabalera
            int imgHeight = background.getHeight(null); // Irudiaren altuera

            // Irudia erdian jartzeko kordenadak kalkulatu
            int x = (getWidth() - imgWidth) / 2; 
            int y = (getHeight() - imgHeight) / 2;

            g.drawImage(this.background, x, y, null); // Irudia marrazten da
        }

        // Dibujables objektuak marrazten dira
        for (Drawable drawable : dibujables) {
            drawable.draw(gr); // Objektua marrazten da
        }
    }

    // Ekintza kudeatzeko metodoa
    @Override
    public void actionPerformed(ActionEvent e) {
        // Alarma ON botoian egin den klikaren kudeaketa
        if(e.getActionCommand() != null && e.getActionCommand().equals("AlarmaON")){
            try {
                mqttPublish = new MqttPublish(BROKER, CLIENT_ID); // MQTT publikatzailea sortzen da
                mqttPublish.publish(String.valueOf(1)); // Alarma ON mezua argitaratzen da
                mqttPublish.disconnect(); // MQTT deskonektatzen da
            } catch (MqttException e1) {
                e1.printStackTrace(); // Akatsak agertzen badira, berriz inprimatzen dira
            }
        }

        // Alarma OFF botoian egin den klikaren kudeaketa
        if(e.getActionCommand() != null && e.getActionCommand().equals("AlarmaOFF")){
            piztuta = false;
            try {
                mqttPublish = new MqttPublish(BROKER, CLIENT_ID); // MQTT publikatzailea sortzen da
                mqttPublish.publish(String.valueOf(0)); // Alarma OFF mezua argitaratzen da
                mqttPublish.disconnect(); // MQTT deskonektatzen da
            } catch (MqttException e1) {
                e1.printStackTrace(); // Akatsak agertzen badira, berriz inprimatzen dira
            }
        }

        // Gasa balioa 1000 baino txikiagoa bada, fondo aldaketa gelditzen da
        if (balioGasa < 3000) {
            stopFondoAldaketa(); // Fondoaren aldaketa gelditzen da
            if (getKolorea() != Color.WHITE) {
                setKolorea(Color.WHITE); // Kolorea zuria jartzen da
                repaint(); // Panelaren marrazketa berriro egiten da
            }
        } else { 
            // Kolorea aldatzen da, kolore zurira edo gorrira
            Color nuevoColor = (getKolorea() == Color.WHITE) ? Color.RED : Color.WHITE;
            if (!nuevoColor.equals(getKolorea())) {
                setKolorea(nuevoColor); // Kolorea ezartzen da
                repaint(); // Panelaren marrazketa berriro egiten da
                if(!piztuta){
                    try {
                        mqttPublish = new MqttPublish(BROKER, CLIENT_ID); // MQTT publikatzailea sortzen da
                        mqttPublish.publish(String.valueOf(1)); // Alarma OFF mezua argitaratzen da
                        mqttPublish.disconnect(); // MQTT deskonektatzen da
                        piztuta = true;
                    } catch (MqttException e1) {
                        e1.printStackTrace(); // Akatsak agertzen badira, berriz inprimatzen dira
                    }
                }
            }
        }
    }
}
