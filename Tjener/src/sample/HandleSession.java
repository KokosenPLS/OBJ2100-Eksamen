package sample;

import javafx.application.Platform;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Klasse som starter serveren og venter på at brukere kobler seg til
 */
public class HandleSession implements Runnable{

    static Database database;
    static ArrayList<Rom> aktiveRom = new ArrayList<>();
    static ArrayList<Bruker> aktiveBrukere = new ArrayList<>();

    static int port = 8000;
    static ObjectInputStream in;
    static ObjectOutputStream out;
    static ServerSocket server;
    static Socket socket;

    static Rom venterom = new Rom(0, "Venterom", new Bruker(0, "Admin", null, null,null,null));

    public HandleSession(){

    }

    private static void leggTilNyBruker(Bruker bruker){
        aktiveBrukere.add(bruker);
    }

    /**
     * fjernBruker - Skal fjerne bruker fra aktive brukere i programmet
     * @param bruker bruker som skal fjernes
     */
    public static void fjernBruker(Bruker bruker){
        aktiveBrukere.remove(bruker);
    }

    /**
     * fjernRom - fjerner et rom som er blitt inaktivt (tomt)
     * @param rom rom som skal fjernes
     * @throws SQLException
     */
    public static void fjernRom(Rom rom) throws SQLException {
        aktiveRom.remove(rom);
        database.fjernRom(rom.getId());
    }

    /**
     *  run - Starter serveren og venter på at brukere kobler seg til
     */
    @Override
    public void run() {
        try {
            database = new Database();

            ArrayList<Rom> aktiveRom = new ArrayList<>();
            ArrayList<Bruker> aktiveBrukere = new ArrayList<>();

            server = new ServerSocket(port);

            while (true){
                socket = server.accept();
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                System.out.println("Bruker koblet til");

                String brukernavn = (String) in.readObject();
                InetAddress ipAdresse = socket.getInetAddress();
                int id = database.opprettBruker(brukernavn, ipAdresse.toString());
                out.writeObject(id);
                Bruker bruker = new Bruker(id, brukernavn, venterom, socket, in, out);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        TjenerUI.oppdaterAktivitetdisplay("Bruker '" + brukernavn + "' koblet seg til");
                    }
                });

                Thread t = new Thread(bruker);

                leggTilNyBruker(bruker);

                t.start();
                System.out.println(aktiveBrukere.size());

            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
