package sample;

import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Date;

/**
 * Klasse for håndtering av databasen
 * legger inn og logger i databasen
 */
public class Database {

    static Connection connection;
    static Statement statement;

    public Database() throws SQLException, FileNotFoundException {
        connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        statement = connection.createStatement();

    }

    /**
     * opprettBruker - oppretter bruker i databasen
     * @param brukernavn brukernavnet til brukeren
     * @param ipAdresse ipadressen til brukeren
     * @return id til den nye brukeren
     * @throws SQLException
     */
    public int opprettBruker(String brukernavn, String ipAdresse) throws SQLException {
        String tidOpprettet = new Date().toString();
        int id = finnBrukerId();
        statement.executeUpdate("INSERT INTO bruker VALUES(" + id + " , '" + brukernavn + "', '" + ipAdresse +"', '" + tidOpprettet + "');");
        return id;
    }

    private int finnBrukerId() throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT COUNT(brukerId) as nyid FROM bruker");
        int id = rs.getInt("nyid");
        return ++id;
    }

    /**
     * opprettRom - oppretter rom i databasen
     * @param romNavn navnet til rommet
     * @param id - id til bruker som oppretter rommet
     * @return id til det nye rommet
     * @throws SQLException
     */
    public int opprettRom(String romNavn, int id) throws SQLException {

        String tidOpprettet = new Date().toString();
        int romId = finnRomId();
        statement.executeUpdate("INSERT INTO rom (romId, navn, tid_opprettet, brukerId) VALUES(" + romId + " , '" + romNavn + "', '" + tidOpprettet +"', " + id + ");");
        return romId;
    }

    private int finnRomId() throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT COUNT(romId) as nyid FROM rom");
        int id = rs.getInt("nyid");
        return ++id;
    }

    /**
     * loggRombytte - logger i databasen hvem som beveger seg til hvilket rom
     * @param brukerId - bruker som bytter rom
     * @param romId - rommet bruker bytter til
     * @throws SQLException
     */
    public void loggRombytte(int brukerId, int romId) throws SQLException {
        statement.executeUpdate("INSERT INTO bruker_i_rom VALUES (" + finnBytteId() + ", " + brukerId + " , " + romId +");");
    }

    private int finnBytteId() throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT COUNT(bytteId) as nyid FROM bruker_i_rom");
        int id = rs.getInt("nyid");
        return ++id;
    }

    /**
     * senMelding - Når en bruker sender melding blir dette lagret i databasen
     * @param melding - meldingsteksten som blir sendt
     * @param avsenderId - id til bruker som sender
     * @param romId - id til hvilket rom melding ble sendt i
     * @throws SQLException
     */
    public void sendMelding(String melding, int avsenderId, int romId) throws SQLException {
        statement.executeUpdate("INSERT INTO melding VALUES ("+ finnMeldingId() +", " + avsenderId + ", " + romId + " , '" + melding +"', '" + new Date().toString() + "' );");
    }

    private int finnMeldingId() throws SQLException {
        ResultSet rs = statement.executeQuery("SELECT COUNT(meldingId) as nyid FROM melding");
        int id = rs.getInt("nyid");
        return ++id;
    }

    /**
     * fjernRom - når et rom lukkes og blir inaktivt lagres tidspunket på når det blir aktivt i databasen
     * @param romId - id til rommet som skal lukkes
     * @throws SQLException
     */
    public void fjernRom(int romId) throws SQLException {
        statement.executeUpdate("UPDATE rom SET tid_lukket = '" + new Date().toString() + "' WHERE romId = " + romId +";");
    }

}
