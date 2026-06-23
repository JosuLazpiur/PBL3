package kodigoa;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.Timer;

import org.eclipse.paho.client.mqttv3.MqttException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class LaborategiaPanela extends JFrame implements PropertyChangeListener, ActionListener {
    // Laborategia panelaren klasea, JFrame-kontainer bat erabiliz
    List<Drawable> dibujables; // Objektuak marraztu ahal izateko zerrenda
    LabObjektuak laborategikoObjektuak; // Laborategiko objektuen zerrenda
    Pertsona pertsona; // Laborategian dagoen pertsona
    JPanel panela; // GUI panel nagusia
    JButton alarma; // Alarmaren botoia
    boolean grafikaPanelean = true; // Grafikoak panelean ikusteko aldagai bat
    NireAkzioa grafikak, mapa, irten, filtratuEguneka; // Akzioak (botoiak) definitzen dituzten objektuak
    MqttSuscribe mqtt; // MQTT subscribentzia objektua
    Timer timer; // Denbora-txartela (grafikoak eguneratzeko)
    private XYSeries evolucionSeries; // Grafiko normala egiteko seriea
    private final Queue<Double> historialValores; // Grafiko normala elikatzeko balioen historiala
    public static final String CLIENT_ID = "TemperatureSimulator"; // MQTT client ID
    private final String FILE_NAME; // Balioak gordetzeko fitxategiaren izena
    String ip; // IP helbidea jasotzeko
    public static final String CLIENT_ID_BIDALI = "TemperatureIdazle"; // MQTT client ID-a

    public LaborategiaPanela(Laborategia lab) {
        super(lab.getIzena()); // JFrame izena laborategiaren izenarekin abiarazten da
        ip = lab.getIp_helbidea(); // IP helbidea jaso
        String[] partes = ip.split("//"); // IP helbidea bi zatitan banatzen du
        String[] split = partes[1].split(":"); // IP eta portuaren artean banaketa
        FILE_NAME = "laborategia\\labBalioak\\" + lab.getIzena() + "_" + split[0] + ".txt"; // Fitxategi izena definitzen du
        pertsona = new Pertsona("Pertsona", 0, 8, 500, 200); // Pertsona bat sortzen da
        historialValores = new LinkedList<>(); // Historiala gorde ahal izateko Cola bat sortzen da
        evolucionSeries = new XYSeries("Eboluzioa"); // Grafikoaren datuen seriea sortzen da
        timer = null; // Denbora-txartela hasieratzen da
        try {
            mqtt = new MqttSuscribe(lab.getIp_helbidea(), CLIENT_ID); // MQTT subscribentzia objektua sortzen da
            mqtt.addPropertyChangeListener(this); // Listener bat gehitzen da MQTT aldaketak jasotzeko
        } catch (MqttException e) {
            e.printStackTrace(); // MQTT errorea kudeatzen da
        }
        sortuAkzioak(); // Akzioak sortzen dira (botoiak eta ekintzak)
        this.setJMenuBar(crearMenuBar()); // Menu barra sortzen da
        this.setContentPane(sortuLeihoPanela()); // Leihoaren edukia definitzen da
        inicializarPantallaCompleta(); // Pantaila osoan bistaratzen da
        this.setSize(1920, 1080); // Leihoaren tamaina definitzen da (1920x1080)
        this.setLocationRelativeTo(null); // Leihoa pantaila erdian kokatzen da
        this.setVisible(true); // Leihoa bistaratzen da
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Leihoak itxi egin behar du aplikazioa
    }

    private void sortuAkzioak() {
        // Akzioak botoietan definitzen dira
        grafikak = new NireAkzioa("Grafikak", new ImageIcon("laborategia/irudiak/iconograficas.jpg"), "Grafikak ikusi", KeyEvent.VK_B); // Grafikak botoia
        mapa = new NireAkzioa("Mapa", new ImageIcon("laborategia/irudiak/iconomapa.jpg"), "Mapa ikusi", KeyEvent.VK_C); // Mapa botoia
        irten = new NireAkzioa("Irten", new ImageIcon("laborategia/irudiak/exit.jpg"), "Irten laborategitik", KeyEvent.VK_V); // Irten botoia
        filtratuEguneka = new NireAkzioa("Filtratu", new ImageIcon("laborategia/irudiak/iconofiltratu.jpg"), "Filtrar datos por día", KeyEvent.VK_D); // Filtratu botoia
    }

    private Component mostrarGrafikak() {
        // Grafikoak bistaratzen ditu
        double valorActual = mqtt.getTemperature(); // Balio egungo tenperatura jaso
        actualizarHistorial(valorActual); // Balio historikoak eguneratzen dira
        guardarValorEnArchivo(valorActual); // Balioa fitxategian gordetzen da

        // Grafikoei dagokien paneletan bistaratzen dira
        JPanel graficoCilindrico = crearGraficoCilindrico(valorActual); // Grafiko zilindriko bat sortzen da
        JPanel graficoNormal = crearGraficoNormal(); // Grafiko normala sortzen da

        // Panelen arteko banaketa (horizontal) sortzen da
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graficoCilindrico, graficoNormal);
        
        // Banaketa hasierako posizioa definitzen da (barraren lehen zatia 30%)
        splitPane.setDividerLocation(0.3);
        splitPane.setResizeWeight(0.3); // Erreskatatzeko proportzioa mantendu

        return splitPane; // Banatutako panela itzultzen da
    }

    private void guardarValorEnArchivo(double valor) {
        // Balioa fitxategian gordetzeko metodoa
        int urtea = new Day().getYear(); // Urtearen datua lortzen da
        int hilabetea = new Day().getMonth(); // Hilabetea lortzen da
        int eguna = new Day().getDayOfMonth(); // Egunaren datua lortzen da
        int orduak = new Hour().getHour(); // Orduak lortzen dira
        int minutuak = new Minute().getMinute(); // Minutuak lortzen dira
        int segunduak = new Second().getSecond(); // Segunduak lortzen dira
        String datestamp = String.valueOf(urtea) + "-" + String.valueOf(hilabetea) + "-" + String.valueOf(eguna); // Data lortzen da
        String timestamp = String.valueOf(orduak) + ":" + String.valueOf(minutuak) + ":" + String.valueOf(segunduak); // Ordua eta minutuak lortzen dira
        String data = datestamp + "," + timestamp + "," + valor + "\n"; // Datuak osatzen dira CSV formatuan
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) { // Fitxategian idazteko
            if(valor != 0.0000000){ // Balioak 0 ez badira, fitxategian gordetzen da
                writer.write(data);
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace(); // Fitxategiko erroreak kudeatzen dira
        }
        
    }

    private JPanel crearGraficoCilindrico(double valorActual) {
        // Grafiko zilindrikoaren panela sortzen du
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); // Datu multzoa sortzen da

        if(valorActual != 0.000000){ // Balio egonkorra badago
            dataset.addValue(Math.min(valorActual, 3500), "Balioa", "Momentukoa"); // Balioa gehitzen da datasetera
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Momentuko Gas Balio Grafika", // Grafikoaren izena
                "Egoera", // X ardatza
                "Gas Balioa", // Y ardatza
                dataset, // Datu multzoa
                PlotOrientation.VERTICAL, // Grafikaren orientazioa
                false, // Legendari ez
                true, // Tooltips
                false // URL aintzat hartzea ez
        );

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter()); // Barra margotzeko estiloa
        renderer.setMaximumBarWidth(0.3); // Maximo zabalera
        plot.setRangeGridlinePaint(Color.BLACK); // Liniak jartzen dira

        // Barra koloreak dinamikoak balioen arabera
        if (valorActual < 1500) {
            renderer.setSeriesPaint(0, new GradientPaint(0, 0, Color.GREEN, 0, 0, Color.LIGHT_GRAY, true)); // Berde
        } else if (valorActual <= 3000) {
            renderer.setSeriesPaint(0, new GradientPaint(0, 0, Color.ORANGE, 0, 0, Color.LIGHT_GRAY, true)); // Laranja
        } else {
            renderer.setSeriesPaint(0, new GradientPaint(0, 0, Color.RED, 0, 0, Color.LIGHT_GRAY, true)); // Gorria
        }

        plot.getRangeAxis().setRange(0, 3500); // Y ardatzaren tartea
        plot.setBackgroundPaint(new Color(230, 230, 250)); // Grafikoaren atzeko kolorea
        plot.setOutlinePaint(Color.DARK_GRAY); // Borda kolorea

        return new ChartPanel(chart); // Grafikoa panel batean bistaratzen da
    }

    private JPanel crearGraficoNormal() {
        // Grafiko normala sortzen du (denboraren eta balioaren bilakaera)
        XYSeriesCollection dataset = new XYSeriesCollection(evolucionSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Balioen eboluzioa", // Grafikoaren izena
                "Denbora", // X ardatza
                "Balioa", // Y ardatza
                dataset, // Datu multzoa
                PlotOrientation.VERTICAL, // Grafikaren orientazioa
                true, // Legendari bai
                true, // Tooltips
                false // URL aintzat hartzea ez
        );

        chart.setBackgroundPaint(new Color(240, 240, 240)); // Grafikoaren atzeko kolorea
        chart.getTitle().setPaint(new Color(70, 70, 70)); // Tituluaren kolorea
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 16)); // Tituluaren fontua

        // Grafikoaren esparruaren koloreak
        chart.getXYPlot().setBackgroundPaint(new Color(220, 220, 220));
        chart.getXYPlot().setDomainGridlinePaint(Color.GRAY); // X ardatzaren lerroak
        chart.getXYPlot().setRangeGridlinePaint(Color.GRAY); // Y ardatzaren lerroak

        // Lerroen koloreak eta lodiera
        chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.RED);
        chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(2.0f)); // Lerro lodiera

        NumberAxis ejeY = (NumberAxis) chart.getXYPlot().getRangeAxis();
        ejeY.setRange(0, 3500); // Y ardatzaren tartea finkoa

        return new ChartPanel(chart); // Grafikoa panel batean bistaratzen da
    }

    public void inicializarPantallaCompleta() {
        this.setExtendedState(JFrame.MAXIMIZED_BOTH); // Leihoa pantaila osoan maximizatzea
    }
    
    private void actualizarHistorial(double valorActual) {
        if (historialValores.size() >= 20 ) { // Mantendu historioko 20 balio gehienez
            historialValores.poll(); // Lehenengo balioa kentzen da
        }
    
        if(valorActual != 0.000000){ // Balio ez-zeroa badago
            historialValores.offer(valorActual); // Balioa historialera gehitzen da
        }
    
        evolucionSeries.clear(); // Eboluzioaren seriea garbitzen da
        int index = 0;
        for (double valor : historialValores) { // Historialeko balioak gehitzen dira
            if(valor != 0.000000){ // Balio ez-zeroak baino ez dira gehitzen
                evolucionSeries.add(index++, valor); // Balioa eta indizea gehitzen dira
            }
        }
    }
    
    public void generarGraficoPorFecha(String fecha) {
        try {
            List<String> lineas = Files.readAllLines(Paths.get(FILE_NAME)); // Fitxategitik lerro guztiak irakurtzen dira
            XYSeries seriesCompleta = new XYSeries(fecha + "-ko Balioak"); // Serie osoa sortzen da
    
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Dataren formatua definitzen da
    
            // Data guztien kargaketa
            for (String linea : lineas) {
                String[] partes = linea.split(","); // Komaren bidez zatitzen da lerroa
                if (partes.length < 3) continue; // Formatua okerra bada, lerroa saltatzen da
    
                String fechaArchivo = partes[0].trim(); // Fitxategiko data
                String horaArchivo = partes[1].trim();  // Fitxategiko ordua
                double valor;
    
                try {
                    valor = Double.parseDouble(partes[2].trim()); // Balioa konbertitzen da
                } catch (NumberFormatException e) {
                    continue; // Balio okerra badago, lerroa saltatzen da
                }
    
                if (fechaArchivo.equals(fecha)) { // Data egokia bada
                    Date fechaHora = dateFormat.parse(fechaArchivo + " " + horaArchivo); // Data eta ordua lotzen dira
                    seriesCompleta.add(fechaHora.getTime(), valor); // Denbora (miliseko) eta balioa gehitzen dira
                }
            }
    
            if (seriesCompleta.isEmpty()) { // Balioak ez badira aurkitu
                JOptionPane.showMessageDialog(null, "Data horretako datuak ez dira aurkitu: " + fecha, "Balio gabe", JOptionPane.INFORMATION_MESSAGE);
                return; // Ezarritako datarekin datuak ez badira aurkitu, gelditu
            }
    
            // Ikusgai den tamaina gehienez (20 puntu)
            int maxVisible = 20;
            int totalPuntos = seriesCompleta.getItemCount();
    
            // Lehenengo puntu batzuen seriea sortzen da
            XYSeries seriesVisible = new XYSeries("Ikusgarri dauden balioak");
            for (int i = 0; i < Math.min(maxVisible, totalPuntos); i++) { // Lehen 20 puntuak gehitzen dira
                seriesVisible.add(seriesCompleta.getX(i), seriesCompleta.getY(i));
            }
    
            XYSeriesCollection dataset = new XYSeriesCollection(seriesVisible); // Datu multzoa sortzen da
            JFreeChart chart = ChartFactory.createXYLineChart(
                    fecha + "-ko Balioak", // Grafikoaren izena
                    "Ordua", // X ardatza (ordua)
                    "Balioa", // Y ardatza (balioa)
                    dataset, // Datu multzoa
                    PlotOrientation.VERTICAL, // Orientazio bertikala
                    true, // Legendak erakutsi
                    true, // Tooltips aktibatuta
                    false // URL aintzat hartu gabe
            );
    
            // Grafikoaren pertsonalizazioa
            chart.setBackgroundPaint(new Color(240, 240, 240)); // Grafikoaren atzeko kolorea
            chart.getTitle().setPaint(new Color(70, 70, 70)); // Tituluaren kolorea
            chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 16)); // Tituluaren fontua
    
            // Trazatuaren arloaren pertsonalizazioa
            chart.getXYPlot().setBackgroundPaint(new Color(220, 220, 220)); // Trazatuaren atzeko kolorea
            chart.getXYPlot().setDomainGridlinePaint(Color.GRAY); // X ardatzaren sareko lerroak
            chart.getXYPlot().setRangeGridlinePaint(Color.GRAY); // Y ardatzaren sareko lerroak
    
            // Lerro eta puntu pertsonalizazioa
            chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE); // Lerroen kolorea
            chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(2.0f)); // Lerroen lodiera
    
            // Eje X data axis gisa konfiguratu
            DateAxis ejeX = new DateAxis("Ordua");
            ejeX.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss")); // Formatua HH:mm:ss gisa ezarri
            chart.getXYPlot().setDomainAxis(ejeX);
    
            // Eje Y 2000 arteko balioekin finkatu
            NumberAxis ejeY = (NumberAxis) chart.getXYPlot().getRangeAxis();
            ejeY.setRange(0, 3500); // Y ardatza 0 eta 2000 bitartekoa
    
            // Grafikoaren panela sortzen da
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(1200, 600)); // Tamaina lehenetsia
            chartPanel.setMouseWheelEnabled(true); // Sorgailuaren erabili ahalmena aktibatuta
    
            // ScrollBar bat sortzen da desplazamendua kudeatzeko
            JScrollBar scrollBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, maxVisible, 0, totalPuntos);
            scrollBar.addAdjustmentListener(e -> { // Scroll-barrean aldaketa bat gertatzen denean
                int startIndex = scrollBar.getValue(); // Scrollaren hasierako indizea
                seriesVisible.clear(); // Seriea garbitzen da
    
                // Puntuak berriro gehitzen dira
                for (int i = startIndex; i < Math.min(startIndex + maxVisible, totalPuntos); i++) {
                    seriesVisible.add(seriesCompleta.getX(i), seriesCompleta.getY(i));
                }
            });
    
            // Fenomenoaren leihoa eta scroll-barra sortzen dira
            JFrame ventana = new JFrame(fecha + "-ko Grafika");
            ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            ventana.setLayout(new BorderLayout());
            ventana.add(chartPanel, BorderLayout.CENTER); // Grafikoa eta scroll-barra gehitzen dira
            ventana.add(scrollBar, BorderLayout.SOUTH);
    
            // Leihoa pantaila osoan maximizatzen da
            ventana.setExtendedState(JFrame.MAXIMIZED_BOTH);
            ventana.setVisible(true); // Leihoa bistaratzen da
        } catch (IOException | java.text.ParseException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Errorea datuak kargatzerakoan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JMenuBar crearMenuBar() {
        JMenuBar barra = new JMenuBar();
        barra.add(crearMenuCrear()); // Pantailaren menua gehitzen da
        barra.add(crearMenuFiltrar()); // Filtratu menua gehitzen da
        barra.add(Box.createHorizontalGlue()); // Espazio horizontala gehitzen da
        barra.add(crearMenuSalir()); // Irten menua gehitzen da
        return barra; // Menu barra itzultzen da
    }
    
    private JMenu crearMenuCrear() {
        JMenu menuEditar = new JMenu("Pantaila"); // Pantaila menuaren sorrera
        menuEditar.setMnemonic(KeyEvent.VK_E); // Menua laburdura (E) erabiliz
        menuEditar.add(grafikak); // Grafikak botoia gehitzen da
        menuEditar.addSeparator(); // Hostoaren banatzailea
        menuEditar.add(mapa); // Mapa botoia gehitzen da
        return menuEditar; // Menua itzultzen da
    }
    
    private JMenu crearMenuFiltrar() {
        JMenu menuEditar = new JMenu("Filtratu"); // Filtratu menuaren sorrera
        menuEditar.setMnemonic(KeyEvent.VK_F); // Menua laburdura (F) erabiliz
        menuEditar.add(filtratuEguneka); // Eguneko filtratu botoia gehitzen da
        return menuEditar; // Menua itzultzen da
    }
    
    private JMenu crearMenuSalir() {
        JMenu menuSalir = new JMenu("Irten"); // Irten menuaren sorrera
        menuSalir.setMnemonic(KeyEvent.VK_S); // Menua laburdura (S) erabiliz
        menuSalir.add(irten); // Irten botoia gehitzen da
        return menuSalir; // Menua itzultzen da
    }    

    private Container sortuLeihoPanela() {
        // Panela nagusia sortzen da, BorderLayout erabiliz
        panela = new JPanel(new BorderLayout(0, 20)); 
        panela.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Margen bat ezartzen da panela inguruan
        panela.add(sortuToolBar(), BorderLayout.NORTH); // ToolBar goian gehitzen da
        panela.add(mostrarGrafikak(), BorderLayout.CENTER); // Grafikoak panelaren erdian gehitzen dira
        return panela; // Panela itzultzen da
    }
    
    private Component sortuToolBar() {
        // ToolBar bat sortzen da
        JToolBar toolBar = new JToolBar();
        toolBar.setBorder(BorderFactory.createRaisedBevelBorder()); // ToolBar-ari 3D efektu bat ematen zaio
        toolBar.add(grafikak); // "Grafikak" botoia gehitzen da
        toolBar.add(mapa); // "Mapa" botoia gehitzen da
        toolBar.add(Box.createHorizontalGlue()); // Espazio horizontala gehitzen da
        toolBar.add(irten); // "Irten" botoia gehitzen da
        return toolBar; // ToolBar itzultzen da
    }
    
    private class NireAkzioa extends AbstractAction {
        String texto;
    
        public NireAkzioa(String texto, Icon imagen, String descrip, int nemonic) {
            super(texto, imagen); // Superklaseari argumentuak pasatzen zaizkio
            this.texto = texto; // Testua gordetzen da
            this.putValue(Action.SHORT_DESCRIPTION, descrip); // Deskribapena ezartzen da
            this.putValue(Action.MNEMONIC_KEY, nemonic); // Laburdura tekla ezartzen da
        }
    
        @Override
        public void actionPerformed(ActionEvent e) {
            // Botoiaren arabera ekintza desberdinak egiten dira
            if (texto.equals("Grafikak")) {
                grafikaPanelean = true;
                pantallaAldatu(grafikaPanelean); // Grafiko panela bistaratzen da
            } else if (texto.equals("Mapa")) {
                grafikaPanelean = false;
                pantallaAldatu(grafikaPanelean); // Mapa panela bistaratzen da
            } else if (texto.equals("Irten")) {
                System.exit(0); // Programatik irteten da
            } else if(texto.equals("Filtratu")) {
                // Filtratu botoia sakatzean, erabiltzaileari data bat eskatzen zaio
                String fecha = JOptionPane.showInputDialog(LaborategiaPanela.this, 
                    "Sartu ezazu data (formatua: uuuu-HH-ee):", 
                    "Eguneka filtratu", JOptionPane.QUESTION_MESSAGE);
                if (fecha != null && !fecha.trim().isEmpty()) {
                    generarGraficoPorFecha(fecha.trim()); // Data egokia bada, grafikoa sortzen da
                }
            }
        }
    }
    
    public void pantallaAldatu(boolean grafika) {
        // Panelaren edukia aldatzen da, grafiko edo mapa erakusteko
        panela.removeAll(); // Panela guztiz garbitzen da
        panela.add(sortuToolBar(), BorderLayout.NORTH); // ToolBar berriro gehitzen da
        if (grafika) {
            panela.add(mostrarGrafikak(), BorderLayout.CENTER); // Grafiko panela gehitzen da
        } else {
            panela.add(mostrarMapa(), BorderLayout.CENTER); // Mapa panela gehitzen da
        }
        panela.revalidate(); // Panelaren egitura berriro balioztatzen da
        panela.repaint(); // Panela berriro margotzen da
    }
    
    private Component mostrarMapa() {
        // Mapa erakusteko objektuak prestatzen dira
        dibujables = new ArrayList<>();
        laborategikoObjektuak = new LabObjektuak(dibujables, ip); // Mapa objektuak kargatzen dira
        return laborategikoObjektuak; // Mapa objektuak itzultzen dira
    }
    
    public void startPertsonaMugitzen() {
        // Pertsona mugimenduan hastea, timer baten bidez
        timer = new Timer(1000, this); // 1 segunduro ekintza bat gertatuko da
        timer.start(); // Timer-a hasten da
    }
    
    public void stopPertsonaMugitzen() {
        // Pertsona mugimendua gelditzea
        if (timer != null) {
            timer.stop(); // Timer-a gelditzen da
            timer = null; // Timer-a nulua da
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Jarraian dauden ekintzak propietate aldaketek gertatzean exekutatzen dira
        switch (evt.getPropertyName()) {
            case "aldatuGasa":
                double balioGasa = (double) evt.getNewValue(); // Gasa balioa hartzen da
    
                // Alarmako topic-etik datuak iristen badira, irakurketa baztertzen da
                if (evt.getSource() instanceof MqttSuscribe) {
                    MqttSuscribe source = (MqttSuscribe) evt.getSource();
                    if (source.getTopic().equals("alarma")) {
                        return; // Alarmako topic-a baztertzen da
                    }
                }
    
                if (grafikaPanelean) {
                    pantallaAldatu(grafikaPanelean); // Grafikaren panela eguneratzen da
                } else {
                    // Historia eguneratzen da eta gasa balioaren arabera objektuak aldatu
                    actualizarHistorial(balioGasa);
                    guardarValorEnArchivo(balioGasa);
                    laborategikoObjektuak.setBalioGasa(balioGasa);
                    if (balioGasa >= 3000) {
                        pertsona.setKolorea(Color.RED); // Kolorea gorria izango da4
                    } else {
                        pertsona.setKolorea(Color.GREEN); // Kolorea berdea izango da
                    }
                    laborategikoObjektuak.repaint(); // Mapa edo objektuaren margotzea berriz
                }
                break;
            case "aldatuJendea":
                double balioJendea = (double) evt.getNewValue(); // Jendea balioaren aldaketa
    
                // Mapa panelaren eguneraketa
                if (!grafikaPanelean) {
                    if (balioJendea == 1) {
                        if (!laborategikoObjektuak.dibujables.contains(pertsona)) {
                            laborategikoObjektuak.dibujables.add(pertsona); // Pertsona mapa gehitzen da
                            startPertsonaMugitzen(); // Pertsonaren mugimendua hasten da
                        }
                    } else {
                        if (laborategikoObjektuak.dibujables.contains(pertsona)) {
                            laborategikoObjektuak.dibujables.remove(pertsona); // Pertsona mapa kentzen da
                        }
                    }
                    laborategikoObjektuak.repaint(); // Mapa berriro margotzen da
                }
                break;
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Timer-ak pertsona mugitzeko funtzioa deitzen du
        if (!laborategikoObjektuak.dibujables.contains(pertsona)) {
            stopPertsonaMugitzen(); // Pertsona ez bada mapa gainean, mugimendua gelditzen da
        } else {
            pertsona.mugitu(); // Pertsona mugitu egiten da
        }
    }    
}
