package streamops;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementiert einige nützliche Operationen mit Hilfe von Streams.
 * 
 * Die Verarbeitung soll hierbei durchgängig mit Stream-Operationen vorgenommen werden, d.h. Streams
 * dürfen lediglich zur Bildung des Endergebnisses einer Methode mit einer Endoperation ausgewertet
 * werden.
 *
 * In dieser Klasse dürfen nicht-öffentliche Hilfsmethoden und Hilfsklassen / Aufzählungstypen
 * ergänzt werden.
 * 
 * @author kar, mhe, Lars Sander, Alexander Löffler
 *
 */
public class StreamOperations {

    /**
     * Gibt die Collatz-Folge ab der übergebenen Zahl zurück.
     * 
     * @param n Die Startzahl (erstes Element der Folge)
     * @pre n muss eine positive, ganze Zahl sein
     * @return Die Collatz-Folge
     */
    public static IntStream collatz(int n) {
        assert n > 0;
        final int num = 3; // Gegen Magic Number in Checkstyle
        return IntStream.iterate(n, e -> {
            if (e % 2 == 0) {
                return e / 2;
            } else {
                return num * e + 1;
            }
        });
    }

    /**
     * Ver- / bzw. entschlüsselt den übergebenen Text plaintext mit der angegebenen Rotation (gemäß
     * Caesar-Verschlüsselung).
     * 
     * Zeichen in plaintext, die nicht im Bereich PrintableChar liegen, werden im Zuge der
     * Stream-Erzeugung entfernt.
     * 
     * Diese Methode muss auf die gleichnamige Methode, welche auf Streams arbeitet, abgebildet
     * werden.
     * 
     * @param plaintext Text zum Ver- bzw. Entschlüsseln
     * @param rotation Rotation
     * @param encode true: verschlüsseln, false: entschlüsseln
     * @pre plaintext != null
     * @pre rotation &ge; 0 &amp;&amp; rotation &lt; PrintableChar.RANGE
     * @post Der Rückgabewert darf nur Zeichen aus dem Bereich PrintableChar enthalten
     * @return Ver- bzw. entschlüsselter Text.
     */
    public static String caesar(String plaintext, int rotation, boolean encode) {
        assert plaintext != null;
        assert rotation >= 0;
        assert rotation < PrintableChar.RANGE;

        Stream<PrintableChar> resStream =
                caesar(PrintableChar.convertStringToStream(plaintext), rotation, encode);

        String result = resStream.map(Object::toString).collect(Collectors.joining());

        for (char c : result.toCharArray()) {
            assert PrintableChar.isPrintableChar(c);
        }

        return result;
    }

    /**
     * Ver- / bzw. entschlüsselt den als Zeichen-Stream übergebenen Text plaintext mit der
     * angegebenen Rotation (gemäß Caesar-Verschlüsselung).
     * 
     * @param plaintext zu ver- bzw. entschlüsselnder Zeichen-Stream
     * @param rotation Rotation
     * @param encode true: verschlüsseln, false: entschlüsseln
     * @pre plaintext != null
     * @pre rotation &ge; 0 &amp;&amp; rotation &lt; PrintableChar.RANGE
     * @return Ver- bzw. entschlüsselter Text.
     */
    public static Stream<PrintableChar> caesar(Stream<PrintableChar> plaintext, int rotation,
            boolean encode) {
        assert plaintext != null;
        assert rotation >= 0;
        assert rotation < PrintableChar.RANGE;

        Stream<PrintableChar> result;

        if (encode) {
            result = plaintext.map(e -> e.encrypt(rotation));
        } else {
            result = plaintext.map(e -> e.decrypt(rotation));
        }

        return result;
    }

    /**
     * Ver- / bzw. entschlüsselt den übergebenen Text plaintext mit dem angegebenen Schlüsselwort
     * (gemäß Vigenere-Verschlüsselung). Das Schlüsselwort darf beliebige Zeichen enthalten; der
     * ASCII-Wert des Zeichens an der jeweiligen Stelle des Schlüsselwortes entspricht der
     * anzuwendenden Rotation. Die Rotation muss gegebenenfalls in den zulässigen Rotationsbereich
     * abgebildet werden.
     * 
     * Zeichen in plaintext, die nicht im Bereich PrintableChar liegen, werden im Zuge der
     * Stream-Erzeugung entfernt.
     * 
     * Diese Methode muss auf die gleichnamige Methode, welche auf Streams arbeitet, abgebildet
     * werden.
     * 
     * @param plaintext Text zum Ver- bzw. Entschlüsseln
     * @param pwd Schlüsselwort
     * @param encode true: verschlüsseln, false: entschlüsseln
     * @pre plaintext != null
     * @pre pwd != null
     * @pre pwd ist nicht leer
     * @post Der Rückgabewert darf nur Zeichen aus dem Bereich PrintableChar enthalten
     * @return Ver- bzw. entschlüsselter Text.
     */
    public static String vigenere(String plaintext, String pwd, boolean encode) {
        assert plaintext != null;
        assert pwd != null;
        assert pwd.length() > 0;

        Stream<PrintableChar> resultStream =
                vigenere(PrintableChar.convertStringToStream(plaintext), pwd, encode);

        String res = resultStream.map(Object::toString).collect(Collectors.joining());

        for (char c : res.toCharArray()) {
            assert PrintableChar.isPrintableChar(c);
        }

        return res;
    }

    /**
     * Ver- / bzw. entschlüsselt den als Zeichen-Stream übergebenen Text plaintext mit dem
     * angegebenen Schlüsselwort (gemäß Vigenere-Verschlüsselung). Das Schlüsselwort darf beliebige
     * Zeichen enthalten; der ASCII-Wert des Zeichens an der jeweiligen Stelle des Schlüsselwortes
     * entspricht der anzuwendenden Rotation. Die Rotation muss gegebenenfalls in den zulässigen
     * Rotationsbereich abgebildet werden.
     * 
     * Aus dem Schlüsselwort ist ein Strom zu erzeugen, der das Schlüsselwort unendlich häufig
     * wiederholt. Dieser Strom ist für die Verschlüsselung von plaintext zu nutzen.
     * 
     * @param plaintext zu ver- bzw. entschlüsselnder Zeichen-Stream
     * @param pwd Schlüsselwort
     * @param encode true: verschlüsseln, false: entschlüsseln
     * @pre plaintext != null
     * @pre pwd != null
     * @pre pwd ist nicht leer
     * @return Ver- bzw. entschlüsselter Text.
     */
    public static Stream<PrintableChar> vigenere(Stream<PrintableChar> plaintext, String pwd,
            boolean encode) {
        assert plaintext != null;
        assert pwd != null;
        assert pwd.length() > 0;

        Stream<Integer> pwdst =
                Stream.generate(() -> pwd).flatMapToInt(s -> s.toString().chars()).mapToObj(c -> c);

        return oneTimePad(plaintext, pwdst, encode);
    }

    /**
     * Liefert einen unendlichen Schlüssel, der der Folge von Zufallszahlen entspricht, die die
     * ints()-Operation der Klasse Random bei einem Ausgangs-Seed von 42 erzeugt. Zufallszahlen &lt;
     * 0 werden aus dem Strom entfernt.
     * 
     * @return Der unendliche Schlüssel als Strom von Zahlen
     */
    public static Stream<Integer> oneTimePadPassphrase() {
        final int seed = 42;

        return new Random(seed).ints().filter(e -> e >= 0).mapToObj(e -> e);
    }

    /**
     * Ver- / bzw. entschlüsselt den als Zeichen-Stream übergebenen Text plaintext mit dem als Strom
     * von Zahlen angegebenen unendlichen Schlüssel. Die Verknüpfung von plaintext und Schlüssel
     * erfolgt entsprechend der Vigenere-Verschlüsselung per Addition.
     * 
     * Der Schlüssel darf beliebige Zahlen &ge; 0 enthalten und entspricht der anzuwendenden
     * Rotation. Die Rotation muss gegebenenfalls in den zulässigen Rotationsbereich abgebildet
     * werden.
     * 
     * @param plaintext zu ver- bzw. entschlüsselnder Zeichen-Stream
     * @param passphrase Schlüssel
     * @param encode true: verschlüsseln, false: entschlüsseln
     * @pre plaintext != null
     * @pre passphrase != null
     * @pre passphrase ist nicht leer (informelle Vorbedingung)
     * @return Ver- bzw. entschlüsselter Text.
     */
    public static Stream<PrintableChar> oneTimePad(Stream<PrintableChar> plaintext,
            Stream<Integer> passphrase, boolean encode) {
        assert plaintext != null;
        assert passphrase != null;

        Stream<PrintableChar> res = zip(plaintext, passphrase.map(c -> c % PrintableChar.RANGE),
                (a, b) -> encode ? a.encrypt(b) : a.decrypt(b));

        return res;
    }

    /**
     * Gruppiert die Worte des Eingabestroms stream nach ihrer Länge. Als Ergebnis wird eine Map
     * geliefert, die jeder vorkommenden Wortlänge die Menge der Worte dieser Länge im Stream
     * zuordnet. Für Wortlängen, die nicht im Strom vorkommen, wird kein Eintrag in der Ergebnis-Map
     * erzeugt (also insbesondere auch nicht der Wert 0 zugeordnet).
     * 
     * @param stream Eingabestrom von Worten
     * @pre stream != null
     * @return Die Map von Wortlängen auf die Menge der Worte dieser Länge
     */
    public static Map<Integer, Set<String>> groupWordsOfSameLength(Stream<String> stream) {
        assert stream != null;

        Map<Integer, Set<String>> result = new HashMap<Integer, Set<String>>();

        stream.forEach(str -> {
            if (result.containsKey(str.length())) {
                result.get(str.length()).add(str);
            } else {
                Set<String> entry = new HashSet<String>();
                entry.add(str);
                result.put(str.length(), entry);
            }
        });

        return result;
    }

    /**
     * Zählt die Anzahl der Vorkommen von Zeichen im Eingabestrom stream. Als Ergebnis wird eine Map
     * geliefert, die jedem berücksichtigten Zeichen die Anzahl seiner Vorkommen im Stream zuordnet.
     * Die Parameter from und to beschränken den Bereich der berücksichtigten Zeichen. Für Zeichen,
     * die nicht im Strom vorkommen, wird kein Eintrag in der Ergebnis-Map erzeugt (also
     * insbesondere auch nicht der Wert 0 zugeordnet).
     * 
     * @param stream Eingabestrom von Zeichen
     * @param from Anfang des berücksichtigten Zeichenbereichs (inklusiv)
     * @param to Ende des berücksichtigten Zeichenbereichs (inklusiv)
     * @param max maximale Anzahl der Zeichen im Eingabestrom, die berücksichtigt werden (vor
     *            Berücksichtigung von from und to); für den Wert null wird stets der gesamte Strom
     *            berücksichtigt
     * @pre stream != null
     * @return Die Map von Zeichen auf die Anzahl ihrer Vorkommen
     */
    public static Map<Character, Integer> countChars(Stream<Character> stream, char from, char to,
            Long max) {
        assert stream != null;

        Stream<Character> str = stream;

        if (max != null) {
            str = stream.limit(max);
        }

        return str.filter(c -> c >= from && c <= to)
                .collect(Collectors.toConcurrentMap(w -> w, w -> 1, Integer::sum));
    }

    /**
     * Liefert zu einem Strom von Worten einen Strom der in den Worten enthaltenen Zeichen. Die
     * Reihenfolge der Zeichen bleibt hierbei enthalten.
     * 
     * Zum Beispiel: "ab" "cda" -&gt; "a" "b" "c" "d" "a"
     * 
     * @param stream Eingabestrom
     * @pre stream != null
     * @return Ausgabestrom
     */
    public static Stream<Character> stringsToChars(Stream<String> stream) {
        assert stream != null;

        String reString = stream.collect(Collectors.joining());

        return reString.chars().mapToObj(c -> (char) c);
    }

    /**
     * Wertet Ausdrücke in schriftlicher Notation, z.B. "3 + 5 - 1", von links nach rechts zu einem
     * ganzzahligen Ergebnis aus. Die Ausdrücke werden als Eingabestrom ihrer Elemente geliefert,
     * für das oben genannte Beispiel ist dies: "3" "+" "5" "-" "1". Der Beispielstrom wertet zum
     * Ergebnis 7 aus.
     * 
     * Der Eingabestrom darf nicht leer sein und muss abwechselnd aus Ganzzahlen und Operatoren
     * bestehen, jeweils beginnend und endend mit einer Ganzzahl. Die Ganzzahl-Strings müssen für
     * die Operation Integer.parseInt() interpretierbar sein. Als Operatoren kommen "+" (Addition),
     * "-" (Subtraktion), "*" (Multiplikation), "/" (ganzzahlige Division) und "%" (ganzzahliger
     * Divisionsrest) in Frage.
     * 
     * Im Falle eines ungültigen Streams (leer, fehlerhafte Ganzzahlen, fehlerhafte Operatoren,
     * Reihenfolge nicht eingehalten) wird im Zuge der Auswertung eine IllegalArgumentException
     * ausgelöst.
     * 
     * Über den Parameter replace kann eine Abbildung von Zeichenfolgen auf Zeichenfolgen angegeben
     * werden. Jeder String im Stream, dem in replace ein Wert zugeordnet ist, wird vor der
     * Auswertung des Streams durch den zugeordneten Wert ersetzt. So etwa würde der Stream "Fünf"
     * "plus" "5" mit replace = { "Fünf" -&gt; "5", "plus" -&gt; "+" } zunächst in den Stream "5"
     * "+" "5" überführt und dann ausgewertet.
     * 
     * @param stream Eingabestrom
     * @param replace Map mit Ersetzungen von Strings durch Strings
     * 
     * @pre stream != null
     * @pre replace != null
     * 
     * @throws IllegalArgumentException wenn der Eingabestrom im Zuge der Auswertung nicht den
     *             Anforderungen entspricht
     * @return Auswertungsergebnis
     */
    public static Integer evaluate(Stream<String> stream, Map<String, String> replace) {
        assert stream != null;
        assert replace != null;

        StreamOperations so = new StreamOperations();
        Expression exp = so.new Expression();

        stream.map(e -> replace.containsKey(e) ? replace.get(e) : e).forEach(e -> exp.addNext(e));

        if (exp.nextIsNumber) {
            throw new IllegalArgumentException("Input Stream endete nicht mit einer Number");
        }

        return exp.result;
    }

    /**
     * Ein Operator stellt die erlaubten Operationen in evaluate() dar
     *
     */
    private enum Operator {
        addition, substraction, mulitplikation, division, modulo
    }

    /**
     * Expression dient zur Akkumulation und Interpretierung des Streams aus Strings in evaluate()
     * 
     */
    private class Expression {
        private Integer result = 0;
        private Integer currNumber = 0;
        private boolean nextIsNumber = true;
        private Operator currentOperator = Operator.addition;

        /**
         * Übernimmt einen String und überprüft ob es sich um eine gültige Number oder eine Operator
         * handelt Ruft außerdem evalCurr() auf und verarbeitet den Input somit sofort
         * 
         * @param str Der zu interpretierende String
         */
        public void addNext(String str) {

            if (nextIsNumber) {
                try {
                    currNumber = Integer.valueOf(str);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            "Ungültige Number beim umwandeln. String enthielt: " + str);
                }

                evalCurr();
            } else {

                if (str.length() == 1) {
                    switch (str.charAt(0)) {
                        case '+':
                            currentOperator = Operator.addition;
                            break;
                        case '-':
                            currentOperator = Operator.substraction;
                            break;
                        case '*':
                            currentOperator = Operator.mulitplikation;
                            break;
                        case '/':
                            currentOperator = Operator.division;
                            break;
                        case '%':
                            currentOperator = Operator.modulo;
                            break;

                        default:
                            throw new IllegalArgumentException(
                                    "Ungültiger Operator beim unwandeln. String enthielt: " + str);

                    }
                } else {
                    throw new IllegalArgumentException(
                            "Ungültiger länge von Operator beim unwandeln. String enthielt: "
                                    + str);
                }
            }
            // Switch für nächsten Input
            nextIsNumber = !nextIsNumber;
        }

        /**
         * Ändert das aktuelle Ergebniss basierend auf der zu letzt hinzugefügten Operation
         */
        private void evalCurr() {
            switch (currentOperator) {
                case addition:
                    result = result + currNumber;
                    break;
                case substraction:
                    result = result - currNumber;
                    break;
                case mulitplikation:
                    result = result * currNumber;
                    break;
                case division:
                    if (currNumber == 0) {
                        throw new IllegalArgumentException("Division durch 0");
                    }
                    result = result / currNumber;
                    break;
                case modulo:
                    result = result % currNumber;
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Nicht implementierter Operator. currenOperator ist: "
                                    + currentOperator);
            }
        }
    }

    /**
     * Gruppiert Personen des Eingabestroms persons nach ihrem Geschlecht und liefert als Ergebnis
     * eine Map, die dem jeweiligen Geschlecht die Menge der Personen dieses Geschlechts zuordnet.
     * Hierbei werden nur Personen berücksichtigt, deren Postleitzahl in der Menge zipcodes
     * enthalten ist und deren Einkommen sich in einem angegebenen Bereich (minIncome bis maxIncome,
     * jeweils inklusive) befindet.
     * 
     * Jede Person im Strom wird nur einmalig berücksichtigt (ausgehend von der equals()-Methode von
     * Person).
     * 
     * Existieren zu einem Geschlecht keine Personen, so wird dieses Geschlecht in der Ergebnis-Map
     * nicht berücksichtigt (also keine Zuordnung einer leeren Menge).
     * 
     * @param persons Eingabestrom von Personen
     * @param zipcodes Menge der zu berücksichtigenden Postleitzahlen; wenn null oder leer, dann
     *            werden alle Personen aus persons berücksichtigt
     * @param minIncome kleinstes zu berücksichtigendes Einkommen (inklusiv); wenn null, dann ohne
     *            untere Beschränkung
     * @param maxIncome größtes zu berücksichtigendes Einkommen (inklusiv); wenn null, dann ohne
     *            obere Beschränkung
     * @pre persons != null
     * @return Auswertungsergebnis
     */
    public static Map<Person.Gender, Set<Person>> groupPersonsByGender(Stream<Person> persons,
            Set<Integer> zipcodes, Integer minIncome, Integer maxIncome) {
        assert persons != null;

        Map<Person.Gender, Set<Person>> resultMap = new HashMap<Person.Gender, Set<Person>>();

        persons.distinct().filter(p -> {
            return ((zipcodes == null || zipcodes.isEmpty()) ? true
                    : zipcodes.contains(p.getZipcode()))
                    && ((minIncome == null) ? true : p.getIncome() >= minIncome)
                    && ((maxIncome == null) ? true : p.getIncome() <= maxIncome);
        }).forEach(p -> {
            if (!resultMap.containsKey(p.getGender())) {
                resultMap.put(p.getGender(), new HashSet<Person>());
            }
            resultMap.get(p.getGender()).add(p);
        });

        return resultMap;
    }

    /**
     * Methode, welche die Funktionalität bereitstellt um zwei Streams miteinander sequentiell
     * verknüpfen zu können. Die Länge des Ausgabestroms entspricht der Länge des kürzeren der
     * beiden Eingabeströme.
     * 
     * Beispiel:
     * 
     * <pre>
     * Stream&lt;Integer&gt; s1 = Arrays.stream(new Integer[] { 1, 2, 3, 4 });
     * Stream&lt;Integer&gt; s2 = Arrays.stream(new Integer[] { 5, 6, 7, 8 });
     * Stream&lt;Integer&gt; s = zip(s1, s2, (l,r) -&gt; l + r);
     * Ergebnis: 6, 8, 10, 12
     * </pre>
     * 
     * 
     * Diese Methode darf nicht verändert werden.
     * 
     * @param <A> Typ der Stream-Elemente von a
     * @param <B> Typ der Stream-Elemente von b
     * @param <C> Typ der Elemente des Ergebnis-Streams
     * @param a Eingangsstrom a
     * @param b Eingangsstrom b
     * @param zipper Funktion zur Verknüpfung der jeweils nächsten Elemente der beiden
     *            Eingangsströme zu einem Element des Ausgangsstroms
     * @return Ausgangsstrom
     */
    public static <A, B, C> Stream<C> zip(Stream<? extends A> a, Stream<? extends B> b,
            BiFunction<? super A, ? super B, ? extends C> zipper) {
        Objects.requireNonNull(zipper);
        Spliterator<? extends A> aSpliterator = Objects.requireNonNull(a).spliterator();
        Spliterator<? extends B> bSpliterator = Objects.requireNonNull(b).spliterator();

        // Zipping looses DISTINCT and SORTED characteristics
        int characteristics = aSpliterator.characteristics() & bSpliterator.characteristics()
                & ~(Spliterator.DISTINCT | Spliterator.SORTED);

        long zipSize = ((characteristics & Spliterator.SIZED) != 0)
                ? Math.min(aSpliterator.getExactSizeIfKnown(), bSpliterator.getExactSizeIfKnown())
                : -1;

        Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
        Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
        Iterator<C> cIterator = new Iterator<C>() {
            @Override
            public boolean hasNext() {
                return aIterator.hasNext() && bIterator.hasNext();
            }

            @Override
            public C next() {
                return zipper.apply(aIterator.next(), bIterator.next());
            }
        };

        Spliterator<C> split = Spliterators.spliterator(cIterator, zipSize, characteristics);
        return StreamSupport.stream(split, a.isParallel() || b.isParallel());
    }

}
