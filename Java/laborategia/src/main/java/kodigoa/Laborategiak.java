package kodigoa;

import java.util.ArrayList;
import java.util.List;

public class Laborategiak {
    List<Laborategia> laborategiak;

    public Laborategiak() {
        this.laborategiak = new ArrayList<>();
    }

    public void add(Laborategia laborategia){
        laborategiak.add(laborategia);
    }

    public List<Laborategia> getLaborategiak() {
        List<Laborategia> copia = new ArrayList<>();
        for(Laborategia lab: laborategiak){
            copia.add(lab);
        }

		return copia;
    }
}
