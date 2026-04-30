package edu.ucd.comp2013j.lab;

import edu.ucd.comp2013j.lab.db.Database;
import edu.ucd.comp2013j.lab.web.LabWebServer;

public class App {
    public static void main(String[] args) {
        Database database = new Database();
        database.init();
        new LabWebServer(database, 8080).start();
    }
}
