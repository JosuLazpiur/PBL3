package kodigoa;

public class Laborategia {
    String izena;
    String ip_helbidea;
    boolean jendea;
    boolean gasak;

    public Laborategia(String izena, String ip_helbidea) {
        this.izena = izena;
        if(!ip_helbidea.contains("tcp://")) {
            this.ip_helbidea = "tcp://" + ip_helbidea + ":1883";
        } else {
            this.ip_helbidea = ip_helbidea;
        }
    }

    public String getIzena() {
        return izena;
    }

    public String getIp_helbidea() {
        return ip_helbidea;
    }

    public boolean isJendea() {
        return jendea;
    }

    public boolean isGasak() {
        return gasak;
    }

    public void setJendea(boolean jendea) {
        this.jendea = jendea;
    }

    public void setGasak(boolean gasak) {
        this.gasak = gasak;
    }
}
