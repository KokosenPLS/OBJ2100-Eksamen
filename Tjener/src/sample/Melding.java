package sample;

import java.util.Date;

/**
 * Klasse for oversikt over relevant informasjon om meldinger som er sendt
 */
public class Melding {

    String tekst;
    Bruker avsender;
    String tidspunkt;

    public Melding(String tekst, Bruker bruker){
        this.tekst = tekst;
        this.avsender = bruker;
        tidspunkt = new Date().toString();
    }

    public String getTekst() {
        return tekst;
    }

    public String getAvsenderNavn(){
        return avsender.brukernavn;
    }

    public int getAvsenderId(){
        return avsender.id;
    }

    public String getTidspunkt() {
        return tidspunkt;
    }
}
