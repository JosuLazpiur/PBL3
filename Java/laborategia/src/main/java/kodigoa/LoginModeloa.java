package kodigoa;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class LoginModeloa {
    PropertyChangeSupport konektorea;

    public LoginModeloa(){
        konektorea = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
		konektorea.addPropertyChangeListener(listener);
	}
	public void removePropertyChangeListener (PropertyChangeListener listener) {
		konektorea.removePropertyChangeListener(listener);
	}

    public void setEginBeharrekoa(String mezua){ // LoginPanelera egin behar duena bidaltzeko
		if(mezua.matches("hasi"))this.konektorea.firePropertyChange("hasi", null, mezua);
		if(mezua.matches("erabiltzailea sortu"))this.konektorea.firePropertyChange("erabiltzailea sortu", null, mezua);
		if(mezua.matches("sortu"))this.konektorea.firePropertyChange("sortu", null, mezua);
    }
}
