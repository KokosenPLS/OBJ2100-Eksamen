package sample;

import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * klasse for hver bruker som kobler seg til serveren
 * hvert Bruker objekt kjører på en egen tråd
 */
public class Bruker implements Runnable{

    // Husk å fjern alle unødvendige IDer og annet drit - hauk

    int id;
    String brukernavn;
    Rom iRom;
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket socket;

    public Bruker(int id, String brukernavn, Rom rom, Socket socket, ObjectInputStream in, ObjectOutputStream out){
        this.id = id;
        this.brukernavn = brukernavn;
        iRom = rom;
        this.in = in;
        this.out = out;
        this.socket = socket;
    }

    /**
     * forlatRom - når en bruker forlater et rom
     * @param rom rommet som forlates
     * @throws SQLException
     */
    public void forlatRom(Rom rom) throws SQLException {
        rom.forlatRom(this);
        HandleSession.database.loggRombytte(id, iRom.getId());
    }

    /**
     * enterRom - når bruker går inn i et rom
     * @param rom - rommet som bruker går inn i
     */
    public void enterRom(Rom rom){
        iRom = rom;
        rom.leggTilBruker(this);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Bruker){
            Bruker temp = (Bruker)o;
            return this.id == temp.id;
        }
        return this.id == (int)o;
    }

    @Override
    public String toString() {
        return "Bruker{" +
                "id=" + id +
                ", brukernavn='" + brukernavn + '\'' +
                ", iRom=" + iRom.romNavn +
                '}';
    }

    /**
     * run - blir kjørt av Threaden i handleSession mens bruker er aktiv
     */
    @Override
    public void run() {

        int operasjon;
        boolean loop = true;
        try {
            while (loop) {

                operasjon = (int) in.readObject();

                switch (operasjon) {

                    case 1: // Lage rom

                        int brukerid = (int) in.readObject();

                        String romNavn = (String) in.readObject();
                        Bruker bruker = null;

                        for (Bruker user : HandleSession.aktiveBrukere)
                            if (user.equals(brukerid))
                                bruker = user;


                        if (bruker != null) {
                            int romId = HandleSession.database.opprettRom(romNavn, brukerid);
                            forlatRom(iRom);
                            Rom nyttRom = new Rom(romId, romNavn, bruker);
                            HandleSession.aktiveRom.add(nyttRom);
                            out.writeObject(nyttRom.id);

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    TjenerUI.oppdaterRomdisplay(HandleSession.aktiveRom);
                                    TjenerUI.oppdaterAktivitetdisplay("Bruker " + brukernavn + " opprettet rom '" + nyttRom.romNavn + "'");
                                }
                            });

                        } else {
                            // Finner ikke bruker
                            System.out.println("Bruker ble ikke funnet");
                        }
                        break;

                    case 2: // Bli med i rom

                        brukerid = (int) in.readObject();
                        int romId = (int) in.readObject();

                        bruker = null;

                        for (Bruker user : HandleSession.aktiveBrukere) {
                            if (user.equals(brukerid))
                                bruker = user;
                        }

                        if (bruker != null) {
                            // Finner rom
                            Rom nyttRom = null;
                            for (Rom rom : HandleSession.aktiveRom) {
                                if (rom.equals(romId))
                                    nyttRom = rom;
                            }

                            if (nyttRom != null) {

                                Rom romrom = nyttRom;
                                String forrigeRom = iRom.romNavn;

                                bruker.forlatRom(bruker.iRom);
                                bruker.enterRom(nyttRom);
                                HandleSession.database.loggRombytte(brukerid, romId);

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        TjenerUI.oppdaterRomdisplay(HandleSession.aktiveRom);
                                        TjenerUI.oppdaterAktivitetdisplay("Bruker " + brukernavn + " forlot rommet '" + forrigeRom + "'");
                                        TjenerUI.oppdaterAktivitetdisplay("Bruker " + brukernavn + " gikk inn i rommet '" + romrom.romNavn + "'");
                                    }
                                });

                            } else {
                                // Finner ikke rom
                                System.out.println("Rom ble ikke funnet");
                            }

                        } else {
                            // Finner ikke bruker
                            System.out.println("Bruker ble ikke funnet");
                        }

                        break;
                    case 3: // Be om alle aktive rom
                        out.writeObject(sendRomnavn());
                        out.writeObject(sendRomId());
                        break;
                    case 4: // Send melding
                        String mld = (String)in.readObject();
                        int avsenderId = (int)in.readObject();
                        romId = (int)in.readObject();

                        bruker = null;

                        for (Bruker user : HandleSession.aktiveBrukere) {
                            if (user.equals(id))
                                bruker = user;
                        }

                        if (bruker != null){
                            Rom rom = null;
                            for (Rom tempRom : HandleSession.aktiveRom) {
                                if (tempRom.equals(romId))
                                    rom = tempRom;
                            }

                            if (rom != null){
                                rom.addMelding(new Melding(mld, bruker));
                                HandleSession.database.sendMelding(mld, avsenderId, romId);

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        TjenerUI.oppdaterAktivitetdisplay("Bruker '" + brukernavn + "' sendte en melding i rom '" + iRom.romNavn + "'");
                                    }
                                });

                            }
                            else{
                                System.out.println("Finner ikke rom");
                            }
                        }
                        else{
                            System.out.println("Finner ikke bruker");
                        }
                        break;
                    case 5: // Hente meldinger

                        romId = (int)in.readObject();

                        Rom rom = null;
                        for (Rom tempRom : HandleSession.aktiveRom) {
                            if (tempRom.equals(romId))
                                rom = tempRom;
                        }

                        if(rom != null){
                            int size = rom.getMeldinger().size();
                            System.out.println(size);

                            String[] mldTab = new String[size];
                            String[] navnTab = new String[size];
                            int[] idTab = new int[size];
                            String[] tidTab = new String[size];

                            for(int i = 0; i < size; i++){

                                mldTab[i] = rom.getMeldinger().get(i).getTekst();
                                navnTab[i] = rom.getMeldinger().get(i).getAvsenderNavn();
                                idTab [i] = rom.getMeldinger().get(i).getAvsenderId();
                                tidTab[i] = rom.getMeldinger().get(i).getTidspunkt();

                            }
                            out.writeObject(mldTab);
                            out.writeObject(navnTab);
                            out.writeObject(idTab);
                            out.writeObject(tidTab);

                        }else{
                            System.out.println("Rommet ble ikke funnet");
                        }
                        break;

                    case 6: // sjekke om bruker er siste person i et rom

                        int id = (int) in.readObject();
                        romId = (int) in.readObject();
                        if(romId == 0){
                            out.writeObject(false);
                        }
                        else{
                            rom = null;
                            for (Rom tempRom : HandleSession.aktiveRom) {
                                if (tempRom.equals(romId))
                                    rom = tempRom;
                            }

                            if(rom != null){

                                int antallIrom = rom.brukereTilkoblet.size();
                                if(rom.brukereTilkoblet.size() == 1)
                                    out.writeObject(true);
                                else
                                    out.writeObject(false);


                            }else {
                                System.out.println("Finner ikke rom");
                            }
                        }
                        break;
                    case 7: // Fjerne et aktivt rom

                        romId = (int) in.readObject();

                        rom = null;
                        for (Rom tempRom : HandleSession.aktiveRom) {
                            if (tempRom.equals(romId))
                                rom = tempRom;
                        }
                        if(rom != null){

                            Rom fjernetRom = rom;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    TjenerUI.oppdaterRomdisplay(HandleSession.aktiveRom);
                                    TjenerUI.oppdaterAktivitetdisplay("Rom '" + fjernetRom.romNavn + "' ble fjernet");
                                }
                            });
                            HandleSession.fjernRom(fjernetRom);
                        }

                        break;
                    case 8: // Forlat rom

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                TjenerUI.oppdaterRomdisplay(HandleSession.aktiveRom);
                            }
                        });

                        this.forlatRom(iRom);

                        break;
                    case 9: // Returnere alle aktive brukere i ett spesifikt rom

                        String[] brukere = new String[iRom.brukereTilkoblet.size()];
                        int i = 0;
                        for(Bruker brukerIrom: iRom.brukereTilkoblet){
                            brukere[i++] = brukerIrom.brukernavn;
                        }

                        out.writeObject(brukere);

                        break;
                    case 69: // Avslutter brukersession

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                TjenerUI.oppdaterRomdisplay(HandleSession.aktiveRom);
                                TjenerUI.oppdaterAktivitetdisplay("Bruker '" + brukernavn + "' koblet seg fra");
                            }
                        });

                        loggUt();
                        loop = false;
                        break;
                }
            }
            } catch(SocketException se){
                System.out.println("Ingen input");
            } catch(SQLException throwables){
                throwables.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            } catch(ClassNotFoundException e){
                e.printStackTrace();
            } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String[] sendRomnavn(){
        String[] tab = new String[HandleSession.aktiveRom.size()];
        for(int i = 0; i < tab.length; i++){
            tab[i] = HandleSession.aktiveRom.get(i).getRomNavn();
        }
        return tab;
    }

    private int[] sendRomId(){
        int[] tab = new int[HandleSession.aktiveRom.size()];
        for(int i = 0; i < tab.length; i++){
            tab[i] = HandleSession.aktiveRom.get(i).getId();
        }
        return tab;
    }

    private void loggUt() throws SQLException {
        forlatRom(iRom);
        HandleSession.fjernBruker(this);
    }
}
