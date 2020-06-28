package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helper.Logger;
import helper.MessageType;

/**
 * Ein Server der per TCP-Verbindung Anfragen an einen PrimeManager ermöglicht. Der Server kann
 * hierbei mit beliebige vielen Clients gleichzeitig kommunzieren. Die Kommunikation zwischen Client
 * und Server verläuft stets synchron. Clients dürfen sich allerdings nicht gegenseitig blockieren.
 * 
 * @author kar, mhe, Lars Sander, Alexander L�ffler
 * 
 */
public class PrimeServer implements Logger {

    private List<String> serverLog = new ArrayList<String>();

    // Map vs Array
    // Es ist einfacher beendete Clients aus der Map zu entfernen. Allerdings kann dadurch auch eine
    // alte ID wiederverwendet werden. Ein Array müsste jedes Mal vergrößert/verkleinert werden.
    private Map<Integer, Thread> clientConnections = new HashMap<Integer, Thread>();

    protected ServerSocket serverSocket;

    private PrimeManager primeManager;

    // TODO Gibt es hier einen Vorteil runnable vs thread zu implementieren?
    // Thread kann einen etwas schöneren Aufruf mit der Map haben.
    // Threads mit Clients vs Clients als Threads
    private class ClientThread implements Runnable {

        private Socket clientSocket;

        private PrintWriter out;
        private BufferedReader in;

        private int ID;

        ClientThread(int ID, Socket client) {
            this.ID = ID;
            this.clientSocket = client;
        }

        @Override
        public void run() {

            System.out.println("ClientThread gestartet ID:" + ID);

            try {

                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String msg;

                // Wenn es in in keine Zeilen mehr gibt sollte der Client sich beendet haben.
                while ((msg = in.readLine()) != null) {

                    // Nachricht kann 1 oder 3 mit , geteilte Strings enthalten
                    // (1): Kommt nur bei HALLO vor
                    // (3): ID, MSG, ZAHL
                    String[] arr_msg = msg.split(",");

                    // TODO Was für Fehler können bei .valueOf auftreten und sollten wir dort was
                    // ausgeben?

                    if (arr_msg.length == 1) {
                        if (MessageType.valueOf(arr_msg[0]) == MessageType.HALLO) {

                            out.println(ID);

                            addEntry("client connected," + ID);
                        }

                    } else if (arr_msg.length == 3) {

                        // Sicherstellen das es die gleiche ID ist
                        if (Integer.valueOf(arr_msg[0]) == ID) {

                            switch (MessageType.valueOf(arr_msg[1])) {
                                case PRIMEFACTORS:

                                    List<Long> prim_list =
                                            primeManager.primeFactors(Long.valueOf(arr_msg[2]));

                                    // TODO aufräumen...
                                    addEntry("requested: " + ID + ",primefactors," + arr_msg[2]
                                            + ",[" + prim_list.toString().replaceAll(" ", ",")
                                            + "]");
                                    break;
                                case NEXTPRIME:

                                    Long prim = primeManager.nextPrime(Long.valueOf(arr_msg[2]));

                                    out.println(prim);

                                    addEntry("requested: " + ID + ",nextprime," + arr_msg[2] + ","
                                            + prim);

                                    break;

                                default:
                                    System.err.println("Ungültiger MSG Type :" + arr_msg[1]);
                                    break;
                            }
                        } else {
                            System.err.println("Falsche ID");
                        }

                    } else {
                        System.err.println("Ungültige Nachricht: " + msg);
                    }

                }

                addEntry("client disconnected," + ID);
                clientConnections.remove(ID);
                System.out.println("Thread beendet ID:" + ID + " CC: " + clientConnections.size());

                // TODO Sollten die Reader/Writer hier beendet werden. Wo ist der Unterschied ob
                // hier oder am Client der Socket geschlossen wird?

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    /**
     * Konstruktor.
     * 
     * @pre partitionSize größer 0
     * 
     * @param port Der zu nutzene TCP-Port
     * @param partitionSize Paritionsgröße für den PrimeGenerator
     * 
     * @throws IOException Netzwerkfehler
     */
    public PrimeServer(int port, int partitionSize) throws IOException {
        assert partitionSize >= 1 : "PartitionSize muss >= 1 sein.";

        serverSocket = new ServerSocket(port);

        primeManager = new PrimeManager(partitionSize);
    }

    public PrimeServer(int port, PrimeManager dummy) throws IOException {
        serverSocket = new ServerSocket(port);

        primeManager = dummy;
    }

    /**
     * Startet den Server und den Worker des PrimeGenerators, welcher die Berechnungen übernimmt,
     * mit dem entsprechenden delay. Diese Methode muss sofort zurückkehren.
     * 
     * Für jede Clientverbindung wird ein eigener Thread gestartet.
     * 
     * @pre delay ist größer gleich 0
     * @param delay Das delay in ms für den PrimeGenerator
     * @throws IOException Netzwerkfehler
     */
    public void startServer(long delay) throws IOException {
        assert delay >= 0 : "Delay muss > 0 sein!";

        System.out.println("Server gestartet");

        Thread listener = new Thread(() -> {

            int nextID = 1;
            Socket clientSocket;

            while (true && !serverSocket.isClosed()) {

                try {
                    clientSocket = serverSocket.accept();
                    ClientThread ct = new ClientThread(nextID, clientSocket);
                    Thread thr = new Thread(ct);

                    clientConnections.put(nextID, thr);

                    thr.start();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                nextID++;

            }
        });

        listener.start();

        // TODO
        // Eine dauerhafte Schleife die immer mehr Threads annehmen kann wird benötigt
        // Und nur wenn ein neuer Client startet sollte ein weiterer Thread erzeugt werden.
        // Ist das so überhaupt möglich?

        // TODO
        // Thread in Array abspeichern

    }

    /**
     * Stoppt den Server. Es werden keine neuen Verbindungen mehr angenommen. Bereits bestehende
     * Verbindungen laufen jedoch normal weiter und Anfragen von bereits verbundenen Clients werden
     * noch abgearbeitet. Die Methode kehrt erst zurück, wenn alle Clients die Verbindung beendet
     * haben.
     * 
     * Erst, wenn alle Clients ihre Verbindung beendet haben, wird auch der PrimeGenerator gestoppt
     * um zu verhindern, dass ein Client bis "in alle Ewigkeit" auf eine Antwort wartet.
     * 
     * @throws IOException Netzwerkfehler
     */
    public void stopServer() throws IOException {

        // TODO Der check mit isEmpty() schlägt fehl, weil diese Methode aufgerufen werden kann
        // bevor der ClientThread sich selbst aus der Map entfernt hat.

        if (clientConnections.isEmpty()) {
            serverSocket.close();
        } else {
            System.err.println(
                    "Server kann nicht geschlossen werden, es existieren noch offene Verbindungen. Count:"
                            + clientConnections.size());
        }

        serverSocket.close();

    }

    @Override
    public void addEntry(String e) {
        System.out.println("ServerLog: " + e);
        serverLog.add(e);
    }

    @Override
    public List<String> getLog() {
        return serverLog;
    }
}
