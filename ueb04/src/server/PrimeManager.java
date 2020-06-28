package server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import helper.Logger;

/**
 * 
 * Generiert dauerhaft Primzahlen in einem eigenen Thread und bietet auf Basis der generierten
 * Primzahlen die Ermittlung der jeweils nächstgrößeren Primzahl zu einer übergebenen Zahl und die
 * Zerlegung einer übergebenen Zahl in Primfaktoren an.
 * 
 * Sofern die benötigte(n) Primzahl(en) zum Anfragezeitpunkt schon berechnet wurde(n), werden
 * Anfragen sofort beantwortet. Falls dies nicht der Fall ist, warten die Anfragen bis die
 * entsprechende(n) Primzahl(en) berechnet wurde(n).
 * 
 * Der PrimeManager muss jederzeit eine beliebige Anzahl von Anfragen gleichzeitig bearbeiten
 * können.
 * 
 * @author kar, mhe, Lars Sander, Alexander Löffler
 * 
 */
public class PrimeManager implements Logger {

    private List<String> primeLog = new ArrayList<String>();

    /**
     * Konstruktor.
     * 
     * Mittels partitionSize wird die Größe der Partionen für die Primfaktorenzerlegung angegeben
     * (Anzahl der Primzahlen (Faktorkandidaten), die von einem Task maximal getestet werden).
     * 
     * @pre partitionSize ist größer gleich 1
     * 
     * @param partitionSize Größe der Partition für ForkJoin-Primfaktorenzerlegung
     */
    public PrimeManager(int partitionSize) {
        assert partitionSize >= 1 : "Es können nur Intervalle (>= 1) gebildet werden.";

    }

    /**
     * Liefert zu der übergebenen Zahl die nächstgrößere Primzahl. Ist die übergebene Zahl selbst
     * bereits prim, so wird sie als Ergebnis zurückgegeben.
     * 
     * Wenn die Aussage zum Zeitpunkt der Anfrage noch nicht getroffen werden kann, wird so lange
     * gewartet bis dies möglich ist.
     * 
     * @pre Die übergebene Zahl muss eine positive Ganzzahl (inkl. 0) sein
     * @param q Die Zahl für die, die nächstgrößere Primzahl ermittelt werden soll
     * @return die nächstgrößere Primzahl oder die Zahl selbst (falls sie selbst prim ist)
     */
    public long nextPrime(long q) {
        assert (q >= 0) : "nextPrime muss mit einer positiven Ganzzahl aufgerufen werden.";
        return q;

    }

    /**
     * Liefert eine aufsteigend sortierte Liste aller Primfakoren der übergebenen Zahl q.
     * 
     * Wenn die Berechnung zum Zeitpunkt der Anfrage noch nicht stattfinden kann, wird so lange
     * gewartet bis dies möglich ist.
     * 
     * @pre Es dürfen nur positive Ganzzahlen geprüft werden, die größer gleich 2 sind (siehe
     *      Definition Primzahlen)
     * @param q Die zu zerlegende Zahl
     * @return Liste mit denm aufsteigend sortierten Primfaktoren von q
     */
    public List<Long> primeFactors(long q) {
        assert (q >= 2) : "PrimeFactors muss mit einer positiven Ganzzahl >=2 aufgerufen werden.";
        return null;

    }

    /**
     * Liefert eine Kopie aller bis zum aktuellen Zeitpunkt gefundenen Primzahlen.
     * 
     * Diese Methode ist nur zu Test- und Debugzwecken vorgesehen und darf auch nur dafür verwendet
     * werden.
     * 
     * @return Eine Kopie aller bis jetzt gefundenen Primzahlen.
     */
    public Collection<Long> knownPrimes() {
        return null;
    }

    /**
     * Startet den PrimeWorker-Thread und somit die Berechnung der Primzahlen ab der Zahl 2. Das
     * übergebene delay wird verwendet um die Berechnungen jeweils um den übergebenen Wert in ms zu
     * verzögern. Dabei wird nach jeder geprüften / berechneten Zahl das delay durchgeführt.
     * 
     * Sollte die Berechnung unterbrochen worden sein und wieder gestartet werden, so wird sie an
     * der Stelle fortgesetzt, an der sie unterbrochen wurde.
     * 
     * @pre delay ist größer gleich 0
     * @param delay gewünschte Verzögerung zwischen zwei Berechnungen (s.o.)
     */
    public void startWorker(long delay) {
        assert delay >= 0 : "Delay muss >= 0 sein!";
    }

    /**
     * Beendet die Berechnung der Primzahlen. Die bereits berechneten Primzahlen werden dabei nicht
     * verworfen.
     */
    public void stopWorker() {
    }

    @Override
    public List<String> getLog() {
        return primeLog;
    }

    @Override
    public void addEntry(String e) {
        System.out.println("PrimeLog: " + e);
        primeLog.add(e);
    }

}