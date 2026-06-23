package kodigoa;

import java.awt.Color;
import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Random;
import javax.swing.Timer;

public class Pertsona implements Drawable {
    final static String PROPIETATEA = "etxolanSartuDa";
    final static int RADIO = 50;
    private final static int PARED_IZQUIERDA = 470;
    private final static int PARED_DERECHA = 1375;
    private final static int PARED_SUPERIOR = 10;
    private final static int PARED_INFERIOR = 876;
    Color kolorea = Color.GREEN;
    
    int id;
    int x, y;
    double vx;
    double vy;
    boolean bukatuta;
    String izena;

    PropertyChangeSupport conector;
    Timer timer;

    public Pertsona(String izena, int id, double abiadura, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vx = abiadura;
        this.vy = abiadura;
        this.izena = izena;
        this.bukatuta = false;
        conector = new PropertyChangeSupport(this);
    }

    public int getId() {
        return id;
    }

    public String getIzena() {
        return izena;
    }

    public void mugitu() { // Pertsonaren mugimendua
        int dt;
        Random rd = new Random();
        dt = rd.nextInt(5);
        x = (int) (x + dt * vx);
        dt = rd.nextInt(10);
        y = (int) (y + dt * vy);

        // Verificar colisiones con las paredes
        if (vx < 0 && x <= PARED_IZQUIERDA || vx > 0 && x + RADIO >= PARED_DERECHA) {
            vx = -vx;
        }
        if (vy < 0 && y <= PARED_SUPERIOR || vy > 0 && y + RADIO >= PARED_INFERIOR) {
            vy = -vy;
        }
    }

    public boolean bukatuDu() {
        return bukatuta;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        conector.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        conector.removePropertyChangeListener(listener);
    }

    @Override
    public String toString() {
        return izena;
    }

    public void setKolorea(Color kolorea) {
        this.kolorea = kolorea;
    }

    @Override
    public void draw(Graphics2D g) { // Pertsona marraztu
        g.setColor(kolorea);
        g.fillOval(this.x, this.y, RADIO, RADIO);
    }
}