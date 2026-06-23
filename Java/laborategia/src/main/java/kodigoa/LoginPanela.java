package kodigoa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class LoginPanela extends JFrame implements PropertyChangeListener { // JFrame heredatzen da, eta propietate aldaketak entzuteko interfaze bat implementatzen da.

    final LoginKontrolatzailea kontrolagailua; // Kontrolatzailea, erabiltzailearen ekintzak kudeatzeko.
    final LoginModeloa modeloa; // Modeloaren objektua, datuak gordetzeko eta kudeatzeko.

    JFrame leihoa; // Leiho grafiko nagusia definitzen da.
    JTextField tfErabiltzailea; // Erabiltzaile izena idazteko testu-eremua.
    JPasswordField pfKlabea, pfKlabea2; // Pasahitza idazteko bi testu-eremu (konfirmaziorako bigarrena).

    String pasahitzak, sartutakoP, sartutakoP2, sartutakoU; // Pasahitz fitxategiaren bidea eta erabiltzaileak idatzitako balioak gordetzeko.
    FileReader origen = null; // Fitxategiaren irakurketa fluxua gordetzeko.
    Toolkit toolkit = Toolkit.getDefaultToolkit(); // Sistema eragilearen tresna-kit grafikoa.
    Image fondo; // Irudi bat gordetzeko atributua.
    JButton bOk, erabiltzaileSortu, bOk2; // Botoiak: sartzeko, erabiltzailea sortzeko, eta bigarren aukera batentzat.
    JPanel panelBotoiak; // Botoiak antolatzeko panela.
    boolean erabilSortu = false; // Erabiltzailea sortu den ala ez adierazten duen aldagaia.
    File selectedFile; // Hautatutako fitxategia gordetzeko.
    JPanel panela; // Beste panel bat.
    NirePanela panelaNagusia; // Panel nagusia, leihoaren diseinua kudeatzeko.

    public LoginPanela() { // Eraikitzailea: hasierako konfigurazioak egiten ditu.
        modeloa = new LoginModeloa(); // Modeloaren objektua sortzen da.
        pasahitzak = "laborategia/pasahitzak/pasahitza.txt"; // Pasahitzak gordetzen diren fitxategiaren bidea.
        kontrolagailua = new LoginKontrolatzailea(modeloa); // Kontrolatzailearen objektua sortzen da.
        modeloa.addPropertyChangeListener(this); // Klase hau modeloko aldaketak entzuteko erregistratzen da.
    }

    public boolean datuakKonprobatu() throws IOException { // Pasahitzak eta erabiltzailea egiaztatzeko metodoa.
        String rutaArchivo = "laborategia/pasahitzak/pasahitza.txt"; // Pasahitz fitxategiaren bidea gordetzen da.

        sartutakoP = new String(pfKlabea.getPassword()); // Pasahitza eremutik irakurtzen da.
        sartutakoU = tfErabiltzailea.getText(); // Erabiltzaile izena testu-eremutik irakurtzen da.

        BufferedReader lector = new BufferedReader(new FileReader(rutaArchivo)); // Fitxategia irakurtzeko fluxua irekitzen da.
        String linea = lector.readLine(); // Lehenengo lerroa irakurtzen da.

        while (linea != null) { // Fitxategiko lerroak irakurtzen dira amaiera arte.
            String siguienteLinea = lector.readLine(); // Hurrengo lerroa irakurtzen da.
            if (linea.equals(sartutakoU)) { // Erabiltzaile izena bat datorren konprobatzen da.
                if (sartutakoP.equals(siguienteLinea)) { // Pasahitza bat datorren konprobatzen da.
                    return true; // Erabiltzaile eta pasahitza zuzenak badira, true itzultzen da.
                }
            }
            linea = lector.readLine(); // Hurrengo erabiltzailea irakurtzen da.
        }

        return false; // Konprobazioak huts egiten badu, false itzultzen da.
    }

    private Component sortuPanelPrintzipala() { // Panel nagusia sortzen duen metodoa.
        panelaNagusia = new NirePanela(fondo); // Panel nagusia sortzen da, aurrekaldeko irudiarekin.
        panelaNagusia.setOpaque(false); // Panelaren atzeko planoa gardena izatea konfiguratu.
        panelaNagusia.setLayout(new BorderLayout(20, 7)); // BorderLayout diseinua ezartzen da.
        panelaNagusia.setBorder(BorderFactory.createEmptyBorder(75, 200, 60, 200)); // Barruko ertzak konfiguratzen dira.
        ImageIcon imageIcon = new ImageIcon("laborategia/irudiak/perfila.png"); // Irudia kargatzen da.
        JLabel label = new JLabel(imageIcon); // Irudia etiketa batean jartzen da.

        panelaNagusia.add(label, BorderLayout.NORTH); // Irudia goiko aldean gehitzen da.
        panelaNagusia.add(sortuLoginPanela(), BorderLayout.CENTER); // Login panela erdian gehitzen da.
        panelaNagusia.add(sortuBehekoPanela(), BorderLayout.SOUTH); // Beheko panela behealdean gehitzen da.

        return panelaNagusia; // Panel nagusia itzultzen da.
    }

    private Component sortuBehekoPanela() { // Beheko botoien panela sortzen duen metodoa.
        panelBotoiak = new JPanel(new GridLayout(0, 2, 30, 0)); // GridLayout diseinua botoientzat.
        panelBotoiak.setOpaque(false); // Gardentasuna ezartzen da.

        bOk = new JButton("Hasi"); // "Hasi" botoia sortzen da.
        bOk.addActionListener(kontrolagailua); // Ekintza entzulea gehitzen zaio botoiari.
        bOk.setActionCommand("hasi"); // Ekintza komandoa ezartzen da.

        erabiltzaileSortu = new JButton("Erabiltzailea sortu"); // "Erabiltzailea sortu" botoia sortzen da.
        erabiltzaileSortu.addActionListener(kontrolagailua); // Ekintza entzulea gehitzen zaio botoiari.
        erabiltzaileSortu.setActionCommand("erabiltzailea sortu"); // Ekintza komandoa ezartzen da.

		panelBotoiak.add(bOk); // Botoia panelera gehitzen da.
		panelBotoiak.add(erabiltzaileSortu); // Botoia panelera gehitzen da.

        return panelBotoiak; // Panel botoiak itzultzen dira.
    }

	// Erabiltzailearen testu-eremua sortzen duen metodoa
	private Component sortuLoginErabiltzailea() {
		tfErabiltzailea = new JTextField("Erabiltzailea"); // Testu-eremua sortu eta "Erabiltzailea" ezartzen da hasierako balio gisa
		tfErabiltzailea.setFocusable(false); // Hasieran testu-eremua ezin da hautatu
		tfErabiltzailea.addMouseListener(new java.awt.event.MouseAdapter() { // Sagu-ekintzak kontrolatzeko entzulea gehitzen da
			public void mouseClicked(java.awt.event.MouseEvent evt) { // Testu-eremua klik egiten denean
				tfErabiltzailea.setFocusable(true); // Testu-eremua hautagarria bihurtzen da
				tfErabiltzailea.requestFocusInWindow(); // Fokoa testu-eremura eramaten da
			}
		});
		tfErabiltzailea.setForeground(Color.GRAY); // Hasierako testuaren kolorea grisa da
		tfErabiltzailea.addFocusListener(new FocusListener() { // Foko-aldaketak kontrolatzeko entzulea gehitzen da
			@Override
			public void focusGained(FocusEvent e) { // Fokoa lortzen denean
				if (tfErabiltzailea.getText().equals("Erabiltzailea")) { // Testua "Erabiltzailea" bada
					tfErabiltzailea.setText(""); // Testua hustu egiten da
					tfErabiltzailea.setForeground(Color.BLACK); // Testuaren kolorea beltza bihurtzen da
				}
			}

			@Override
			public void focusLost(FocusEvent e) { // Fokoa galtzen denean
				if (tfErabiltzailea.getText().isEmpty()) { // Testua hutsik badago
					tfErabiltzailea.setForeground(Color.GRAY); // Kolorea berriro grisa bihurtzen da
					tfErabiltzailea.setText("Erabiltzailea"); // Testua "Erabiltzailea" ezartzen da berriro
				}
			}
		});

		return tfErabiltzailea; // Sortutako testu-eremua itzultzen da
	}

	// Bigarren pasahitz-eremua sortzen duen metodoa
	private Component sortuLoginPasahitza2() {
		pfKlabea2 = new JPasswordField(); // Pasahitz-eremua sortzen da
		pfKlabea2.setFocusable(false); // Hasieran ezin da hautatu
		pfKlabea2.addMouseListener(new java.awt.event.MouseAdapter() { // Sagu-ekintzak kontrolatzeko entzulea gehitzen da
			public void mouseClicked(java.awt.event.MouseEvent evt) { // Pasahitz-eremua klik egiten denean
				pfKlabea2.setFocusable(true); // Hautagarria bihurtzen da
				pfKlabea2.requestFocusInWindow(); // Fokoa pasahitz-eremura eramaten da
			}
		});
		pfKlabea2.setEchoChar((char) 0); // Karaktereak ez dira ezkutatzen hasieran

		String defaultText = "Pasahitza"; // Hasierako testua
		pfKlabea2.setText(defaultText); // Testua ezartzen da
		pfKlabea2.setForeground(Color.GRAY); // Kolorea grisa da

		pfKlabea2.addFocusListener(new FocusListener() { // Foko-aldaketak kontrolatzeko entzulea
			@Override
			public void focusGained(FocusEvent e) { // Fokoa lortzen denean
				if (String.valueOf(pfKlabea2.getPassword()).equals(defaultText)) { // Testua hasierako testua bada
					pfKlabea2.setText(""); // Testua hustu egiten da
					pfKlabea2.setForeground(Color.BLACK); // Kolorea beltza bihurtzen da
					pfKlabea2.setEchoChar('●'); // Karaktereak ezkutatzen dira
				}
			}

			@Override
			public void focusLost(FocusEvent e) { // Fokoa galtzen denean
				if (String.valueOf(pfKlabea2.getPassword()).isEmpty()) { // Pasahitza hutsik badago
					pfKlabea2.setEchoChar((char) 0); // Karaktereak ez dira ezkutatzen
					pfKlabea2.setForeground(Color.GRAY); // Kolorea grisa bihurtzen da
					pfKlabea2.setText(defaultText); // Testua hasierako testura itzultzen da
				}
			}
		});

		return pfKlabea2; // Sortutako pasahitz-eremua itzultzen da
	}

	private Component sortuLoginPasahitza() { // Pasahitza sartu ahal izateko osagaia sortzen du

        pfKlabea = new JPasswordField(); // Pasahitza sartzeko testu-eremu berezia sortzen du
        pfKlabea.setFocusable(false); // Eremu hau hasieran fokurik gabe uzten du
        pfKlabea.addMouseListener(new java.awt.event.MouseAdapter() { // Saguaren klik-ekintzarako entzulea gehitzen du
            public void mouseClicked(java.awt.event.MouseEvent evt) { // Klik egitean gertatzen dena
                pfKlabea.setFocusable(true); // Eremua fokuarekin uzten du
                pfKlabea.requestFocusInWindow(); // Leihoan fokua eskatzen du
            }
        });
        pfKlabea.setEchoChar((char) 0); // Karaktereak ez dira ezkutatzen (testua ikusgai dago)

        String defaultText = "Pasahitza"; // Testu lehenetsia definitzen da
        pfKlabea.setText(defaultText); // Testu lehenetsia eremuan ezartzen du
        pfKlabea.setForeground(Color.GRAY); // Testuaren kolorea grisa ezartzen du (lehenetsitako testuarentzat)

        pfKlabea.addFocusListener(new FocusListener() { // Fokua irabazi edo galtzen denean gertatzen dena definitzen du
            @Override
            public void focusGained(FocusEvent e) { // Eremuak fokua irabazten duenean
                if (String.valueOf(pfKlabea.getPassword()).equals(defaultText)) { // Lehenetsitako testua badago
                    pfKlabea.setText(""); // Testua garbitzen du
                    pfKlabea.setForeground(Color.BLACK); // Testuaren kolorea beltza bihurtzen du
                    pfKlabea.setEchoChar('●'); // Sartutako karaktereak puntuak bezala erakusten dira
                }
            }

            @Override
            public void focusLost(FocusEvent e) { // Eremuak fokua galtzen duenean
                if (String.valueOf(pfKlabea.getPassword()).isEmpty()) { // Eremua hutsik badago
                    pfKlabea.setEchoChar((char) 0); // Karaktereak ikusgai uzten ditu
                    pfKlabea.setForeground(Color.GRAY); // Testuaren kolorea gris bihurtzen du
                    pfKlabea.setText(defaultText); // Lehenetsitako testua berriro ezartzen du
                }
            }
        });

        return pfKlabea; // Sortutako pasahitz eremua itzultzen du
    }

	private Component sortuLoginPanela() { // Login panela sortzen duen metodoa
		panela = new JPanel(new GridLayout(2, 2, 10, 10)); // 2x2 grid layout-a eta hutsuneekin panela sortzen du
		panela.setOpaque(false); // Panelaren atzealdea gardena bihurtzen du
		panela.add(sortuLoginErabiltzailea()); // Erabiltzailearen testu-eremua gehitzen du
		panela.add(sortuLoginPasahitza()); // Pasahitzaren testu-eremua gehitzen du
		return panela; // Panela itzultzen du
	}

	protected void sortuLeihoa() { // Leihoa sortzeko metodoa
		leihoa = new JFrame("LOGIN"); // "LOGIN" izeneko leihoa sortzen du
		leihoa.setSize(800, 600); // Leihoaren tamaina 800x600 ezartzen du
		leihoa.setLocationRelativeTo(null); // Leihoa pantailaren erdian kokatzen du
		leihoa.setIconImage(new ImageIcon("laborategia/irudiak/perfila.png").getImage()); // Leihoaren ikonoa ezartzen du
		fondo = toolkit.createImage("laborategia/irudiak/fondo.jpg"); // Atzeko planoaren irudia kargatzen du
		leihoa.add(sortuPanelPrintzipala()); // Panel nagusia leihoan gehitzen du
		leihoa.setVisible(true); // Leihoa bistaratzen du
		leihoa.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Leihoa ixteko ekintza definitzen du
		leihoa.setVisible(true); // Leihoa bistaratu egiten da
	}

	public void loginekoBotoiakAktibatu() { // Logineko botoiak aktibatzen ditu
		bOk.setSelected(false); // bOk botoia hautatu gabe uzten du
		panelBotoiak.removeAll(); // Panelaren botoi guztiak kentzen ditu
		bOk.removeActionListener(kontrolagailua); // bOk botoiari esleitutako ekintza-entzulea kentzen dio
		erabiltzaileSortu.removeActionListener(kontrolagailua); // erabiltzaileSortu botoiari esleitutako ekintza-entzulea kentzen dio
	
		panelBotoiak.add(bOk); // bOk botoia panela gehitzen du
		panelBotoiak.add(erabiltzaileSortu); // erabiltzaileSortu botoia panela gehitzen du
		bOk.addActionListener(kontrolagailua); // bOk botoiari ekintza-entzulea esleitzen dio
		bOk.setActionCommand("hasi"); // bOk botoiaren ekintza-komandoa "hasi" ezartzen du
		erabiltzaileSortu.addActionListener(kontrolagailua); // erabiltzaileSortu botoiari ekintza-entzulea esleitzen dio
		erabiltzaileSortu.setActionCommand("erabiltzailea sortu"); // erabiltzaileSortu botoiaren ekintza-komandoa "erabiltzailea sortu" ezartzen du
	
		panelBotoiak.revalidate(); // Panelaren diseinua eguneratzen du
		panelBotoiak.repaint(); // Panelaren bistaratzea berriz margotzen du
	}
	
	public void erabiltzaileBerria() { // Erabiltzaile berria sortzeko botoiak erakusten ditu
		bOk.setSelected(false); // bOk botoia hautatu gabe uzten du
		panelBotoiak.removeAll(); // Panelaren botoi guztiak kentzen ditu
		bOk2 = new JButton("Sortu"); // "Sortu" izeneko botoia sortzen du
		panelBotoiak.add(bOk2); // Sortutako botoia panela gehitzen du
		bOk2.addActionListener(kontrolagailua); // bOk2 botoiari ekintza-entzulea esleitzen dio
		bOk2.setActionCommand("sortu"); // bOk2 botoiaren ekintza-komandoa "sortu" ezartzen du
	
		panelBotoiak.revalidate(); // Panelaren diseinua eguneratzen du
		panelBotoiak.repaint(); // Panelaren bistaratzea berriz margotzen du
	}
	
	public boolean badagoErabiltzailea() throws IOException { // Erabiltzailea fitxategian dagoen egiaztatzen du
		BufferedReader lector = null; // Fitxategia irakurtzeko irakurgailua hasieratzen du
		lector = new BufferedReader(new FileReader(pasahitzak)); // Pasahitzen fitxategia irekitzen du irakurtzeko
	
		String linea = lector.readLine(); // Lehenengo lerroa irakurtzen du
		boolean berdin = false; // Berdintasun aldagaia hasieratzen du
	
		while (linea != null && berdin == false) { // Lerroak irakurri eta berdintasuna aurkitu arte
			if (linea.equals(sartutakoU)) { // Lerroak sartutako erabiltzailea badu
				berdin = true; // Berdintasun aldagaia eguneratzen du
			}
			linea = lector.readLine(); // Hurrengo lerroa irakurtzen du
			linea = lector.readLine(); // Beste lerro bat saltatzen du (pasahitza lerroa izanik)
		}
		linea = lector.readLine(); // Azkeneko lerroa irakurtzen du
		lector.close();// Fitxategia izteko
	
		return berdin; // Berdintasun balioa itzultzen du
	}
	
	public void erabiltzaileaBerriaSortu() throws IOException { // Erabiltzaile berria sortzeko metodoa
		boolean berdin = false; // Berdintasun aldagaia hasieratzen du
	
		sartutakoP = new String(pfKlabea.getPassword()); // Pasahitza eremutik testua irakurtzen du
		sartutakoP2 = new String(pfKlabea2.getPassword()); // Bigarren pasahitza eremutik testua irakurtzen du
		sartutakoU = tfErabiltzailea.getText(); // Erabiltzailearen izena irakurtzen du
	
		berdin = badagoErabiltzailea(); // Erabiltzailea existitzen den egiaztatzen du
		if ((sartutakoP != null && !sartutakoP.equals("Pasahitza"))
				&& (sartutakoP2 != null && !sartutakoP2.equals("Pasahitza"))
				&& (sartutakoU != null && !sartutakoU.equals("Erabiltzailea"))) { // Eremu guztiak baliozkoak direla egiaztatzen du
			if (sartutakoP.equals(sartutakoP2)) { // Bi pasahitzak berdinak direla egiaztatzen du
				if (berdin == false) { // Erabiltzailea ez badago
					try (BufferedWriter escritor = new BufferedWriter(new FileWriter(pasahitzak, true))) { // Fitxategian idazten du
						escritor.write(sartutakoU); // Erabiltzailea idazten du
						escritor.newLine(); // Lerro berri bat idazten du
						escritor.write(sartutakoP); // Pasahitza idazten du
						escritor.newLine(); // Lerro berri bat idazten du
						escritor.close();
	
					} catch (IOException e) { // Akatsen kasuan
						e.printStackTrace(); // Akatsa bistaratzen du
					}
					loginekoBotoiakAktibatu(); // Logineko botoiak berriro aktibatzen ditu
					erabilSortu = false; // Erabiltzailea sortzeko egoera desaktibatzen du
					panela.setLayout(new GridLayout(2, 2, 10, 10)); // Panelaren diseinua leheneratzen du
					panela.remove(2); // Panelaren hirugarren osagaia kentzen du
					panelaNagusia.setBorder(BorderFactory.createEmptyBorder(75, 200, 60, 200)); // Diseinuaren ertzak ezartzen ditu
					JOptionPane.showMessageDialog(leihoa, "Erabiltzailea berria sortu duzu", "Erabiltzailea sortua", JOptionPane.INFORMATION_MESSAGE); // Mezua erakusten du
					fitxategiaSortu(tfErabiltzailea.getText()); // Fitxategi berria sortzen du
				} else {
					JOptionPane.showMessageDialog(leihoa, "Erabiltzaile hori badago!!!", "Errorea", JOptionPane.ERROR_MESSAGE); // Erabiltzailea dagoela ohartarazten du
				}
			} else {
				JOptionPane.showMessageDialog(leihoa, "Pasahitzak ez dira berdinak", "Errorea", JOptionPane.ERROR_MESSAGE); // Pasahitzak ez direla berdinak erakusten du
			}
		} else {
			JOptionPane.showMessageDialog(leihoa, "Datuak falta dira", "Errorea", JOptionPane.ERROR_MESSAGE); // Datuak falta direla ohartarazten du
		}
	}	

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// Erabiltzailea sortzeko egoeran dagoen egiaztatzen du.
		if (erabilSortu == true) {
			// Propietatearen izena "sortu" bada.
			if (arg0.getPropertyName().matches("sortu")) {
				try {
					erabiltzaileaBerriaSortu(); // Erabiltzaile berri bat sortzeko metodoa exekutatzen du.
				} catch (IOException e) {
					// Fitxategiaren sarrera/irteerako erroreak harrapatu eta bistaratzen ditu.
					e.printStackTrace();
				}
			}
		}

		// Erabiltzailea sortzeko egoeran ez dagoela egiaztatzen du.
		if (erabilSortu == false) {
			// Propietatearen izena "erabiltzailea sortu" bada.
			if (arg0.getPropertyName().matches("erabiltzailea sortu")) {
				erabiltzaileBerria(); // Erabiltzaile berri bat sortzeko metodoa exekutatzen du.
				panela.setLayout(new GridLayout(3, 2, 10, 10)); // Panelaren diseinua 3x2 gelaxkako egitura batean ezartzen du.
				panela.add(sortuLoginPasahitza2()); // Bigarren pasahitza sartzeko eremua panelari gehitzen dio.
				// Panel nagusiaren ertzak ezartzen ditu, erdialdean egoki kokatzeko.
				panelaNagusia.setBorder(BorderFactory.createEmptyBorder(60, 200, 45, 200));
				erabilSortu = true; // Egoera eguneratzen du: erabiltzailea sortzen ari da.
			}

			// Propietatearen izena "hasi" bada.
			if (arg0.getPropertyName().matches("hasi")) {
				try {
					// Sartutako datuak egiaztatzen ditu.
					if (datuakKonprobatu()) {
						// Datuak onak badira, ongi etorriko mezua bistaratzen du.
						JOptionPane.showMessageDialog(leihoa, "Ongi Etorri!", "Sarrera Onartuta",
								JOptionPane.INFORMATION_MESSAGE);
						leihoa.dispose(); // Leihoa ixten du.
						// Printzipala izeneko klase berria sortzen du erabiltzailearen izenarekin.
						new Printzipala(tfErabiltzailea.getText());
					} else {
						// Datuak okerrak badira, errore-mezua bistaratzen du.
						JOptionPane.showMessageDialog(leihoa, "Sarrera desegokia!!!", "Errorea",
								JOptionPane.ERROR_MESSAGE);
					}
				} catch (HeadlessException | IOException e) {
					// Salbuespenak harrapatu eta bistaratzen ditu.
					e.printStackTrace();
				} catch (Exception e) {
					// Edozein beste salbuespen harrapatzen du.
					e.printStackTrace();
				}
			}
		}
	}

	private void fitxategiaSortu(String nungoa) {
		// Erabiltzailearen izenarekin fitxategiaren bidea definitzen du.
		String ruta = "laborategia/laborategiak/" + nungoa + ".txt";
		File archivo = new File(ruta);

		try {
			// Fitxategia existitzen ez bada, fitxategi berria sortzen du.
			if (!archivo.exists()) {
				archivo.createNewFile();
			}
		} catch (IOException e) {
			// Fitxategiaren sarrera/irteerako erroreak harrapatu eta bistaratzen ditu.
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			// Sistemaren itxura eta diseinua aplikatzen du.
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			// Itxura ezartzeko erroreak harrapatu eta bistaratzen ditu.
			e.printStackTrace();
		}
		// LoginPanela objektu berria sortzen du eta leihoa marrazten du.
		LoginPanela programa = new LoginPanela();
		programa.sortuLeihoa();
	}
}
