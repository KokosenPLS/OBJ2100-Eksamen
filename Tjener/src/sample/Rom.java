package sample;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * klasse som holderstyr på hvem som er i et spesifikt rom
 * og hvilke meldinger som er blitt sendt i det rommet
 */
public class Rom{

    ArrayList<Melding> meldinger = new ArrayList<>();
    ArrayList<Bruker> brukereTilkoblet = new ArrayList<>();
    String romNavn;
    int id;

    public Rom(int id, String navn, Bruker bruker){
        this. id = id;
        romNavn = navn;
        bruker.enterRom(this);
        meldinger.add(new Melding("Rom opprettet", bruker));
    }

    public void leggTilBruker(Bruker bruker){
        brukereTilkoblet.add(bruker);
    }

    /**
     * forlatRom - når en bruker forlater rom
     * @param bruker - bruker som forlater rommet
     * @throws SQLException
     */
    public void forlatRom(Bruker bruker) throws SQLException {
        brukereTilkoblet.remove(bruker);
        bruker.enterRom(HandleSession.venterom);
        if(brukereTilkoblet.isEmpty())
            HandleSession.fjernRom(this);
    }

    public ArrayList<Melding> getMeldinger(){
        return meldinger;
    }

    /**
     * addMelding - legger til melding i samling av meldinger i dette rommet
     * @param melding - melding som legges til
     * @throws SQLException
     */
    public void addMelding(Melding melding) throws SQLException {
        meldinger.add(melding);
        HandleSession.database.sendMelding(melding.tekst, melding.avsender.id, melding.avsender.iRom.id);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Rom){
            Rom rom = (Rom)o;
            return this.id == rom.id;
        }
        return id == (int) o;
    }

    @Override
    public String toString() {
        return "Rom{" +
                "meldinger=" + meldinger +
                ", brukereTilkoblet=" + brukereTilkoblet +
                ", romNavn='" + romNavn + '\'' +
                ", id=" + id +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getRomNavn() {
        return romNavn;
    }


}
