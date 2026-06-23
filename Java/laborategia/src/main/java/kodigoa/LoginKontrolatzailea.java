package kodigoa;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginKontrolatzailea implements ActionListener{
    LoginModeloa modeloa;

    public LoginKontrolatzailea(LoginModeloa modeloa){
        this.modeloa = modeloa;
    }

    public void actionPerformed(ActionEvent e) { // LoginPanelean zapaltzen den botoia zein den jakiteko
		switch(e.getActionCommand()){
		case "hasi":
            modeloa.setEginBeharrekoa("hasi");
            break;
		case "erabiltzailea sortu":
            modeloa.setEginBeharrekoa("erabiltzailea sortu");
            break;
		case "sortu":
            modeloa.setEginBeharrekoa("sortu");
            break;
		}
    }
}