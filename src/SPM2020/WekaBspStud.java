package SPM2020;

import java.io.*;
import java.util.*;

import weka.associations.*;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.converters.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.*;
import weka.classifiers.rules.*;

/**
 * Beispielprogramm, um WeKa in eclipse zu verwenden. <br>
 * <br>
 * <b>Bislang keinerlei Fehlerbehandlung, selbst drum k�mmern! </b><br>
 * Weitere Einstellungen (falls n�tig) selbst recherchieren.<br>
 * R�ckgabestrings der Methoden ggf. nach den eigenen Bed�rfnissen anpassen.<br>
 * 
 * <br>
 * Die Rohdaten liegen im CSV-Format vor und enthalten die folgenden 26
 * Attribute: <br>
 * 0..9 Kundendaten und Einkaufsverhalten <br>
 * 10 Einkaufssumme <br>
 * 11..25 gekaufte Waren (Warengruppen)
 * 
 * <br>
 * <br>
 * fertige Analysen: <br>
 * � Top � Daten (h�ufigsten Wert eines Attributs) <br>
 * // String findMaximum (Instances daten, int index) <br>
 * <br>
 * � Darstellung von Waren (-gruppen), die zusammen gekauft werden <br>
 * // String [] makeApriori(Instances daten) <br>
 * <br>
 * � Kundengruppen finden (Clusteranalyse) <br>
 * // String findCluster (Instances daten, int number) <br>
 * <br>
 * 
 * @author Hilke Fasse
 */

public class WekaBspStud {

	/**
	 * ermittelt die angegebene Anzahl der Cluster
	 * 
	 * @param daten  alleDaten, nurKunden, nurWaren - je nach Analyse
	 * @param number Anzahl der Cluster, die ermittelt werden sollen
	 * @return Die einzelnen Cluster in einem String, getrennt durch \n
	 * @throws Exception Fehlerbehandlung muss noch erledigt werden
	 */
	String findCluster(Instances daten, int number) throws Exception {
		String[] result;

		SimpleKMeans model = new SimpleKMeans();
		model.setNumClusters(number);

		model.buildClusterer(daten);

		// Final cluster centroids holen
		result = model.getClusterCentroids().toString().split("@data\n");
		return (result[1].toString() + "\n");
	}

	// Hilfsmethode, um f�r die Auswertung unn�tige Angaben rauszul�schen
	private String clearAprioriList(String oneRule) {
		String temp = "";

		// Weka-blabla raus l�schen
		for (int i = 0; i < oneRule.length(); i++) {
			Character a = oneRule.charAt(i);
			if ((Character.isLetter(a)) || (a == ',')) {
				temp = temp + a;
			}
		}
		return temp;
	}

	/**
	 * Ermittelt aus den Kundendaten die Warengruppen, die h�ufig zusammen gekauft
	 * werden Die Regeln werden �ber den Apriori-Algorithmus ermittelt
	 * 
	 * @param daten nurWaren - f�r die Analyse, der zusammen gekauften Waren <br>
	 *              je nach Analyse auch alleDaten oder nurKunden als daten
	 * @return Waren, die zusammen gekauft werden, als Stringarray, dessen Dimension
	 *         sich aus der Anzahl der gefundenen Regeln ergibt
	 * @throws Exception Fehlerbehandlung muss noch erledigt werden
	 */
	String[] makeApriori(Instances daten) throws Exception {

		// umwandeln in gekauft / nicht gekauft (0/1)
		NumericCleaner nc = new NumericCleaner();
		nc.setMaxThreshold(1.0); // Schwellwert auf 1 setzen
		nc.setMaxDefault(1.0); // alles ueber Schwellwert durch 1 ersetzen
		nc.setInputFormat(daten);
		daten = Filter.useFilter(daten, nc); // Filter anwenden.

		// Die Daten als nominale und nicht als numerische Daten setzen
		NumericToNominal num2nom = new NumericToNominal();
		num2nom.setAttributeIndices("first-last");
		num2nom.setInputFormat(daten);
		daten = Filter.useFilter(daten, num2nom);

		Apriori model = new Apriori();
		model.buildAssociations(daten);

		List<AssociationRule> rulesA = model.getAssociationRules().getRules();
		int anzRules = rulesA.size();

		String[] tmp = new String[anzRules];

		// Ergebnis h�bsch zusammensetzen
		for (int i = 0; i < anzRules; i++) {
			tmp[i] = clearAprioriList(rulesA.get(i).getPremise().toString()) + " ==> "
					+ clearAprioriList(rulesA.get(i).getConsequence().toString());
		}
		return tmp;
	}

	/**
	 * liefert den h�ufigsten Wert eines Attributs zur�ck benutzt ZeroR, eine
	 * Wekafunktion f�r das h�ufigste Element der nominalen Attribute, bei
	 * numerischen Werten wird der Mittelwert geliefert!
	 * 
	 * @param daten Hier wichtig: Daten im <b>arffFormat!</b>
	 * 
	 * @param index - F�r welches Attribut soll das Maximum bestimmt werden (0..9
	 *              hier sinnvoll, da nur diese Daten nominal sind)
	 * @return h�ufigstes Element als String
	 * @throws Exception Fehlerbehandlung muss noch erledigt werden
	 */
	String findMaximum(Instances daten, int index) throws Exception {
		String[] max;

		ZeroR za = new ZeroR(); // wekafunktion

		daten.setClass(daten.attribute(index)); // Attribut dessen Maximum
												// ermittelt werden soll
		za.buildClassifier(daten);

		max = za.toString().split(": "); // weka -blabla wegnehmen

		return (max[1]);
	}



	public static void main(String[] args) throws Exception {

		// Eigenen Dateipfad eintragen, nicht meinen nehmen ;-)
		String path = "D:\\temp\\SPM\\KundenDaten";
		String roh = path + "kd1000.csv";
		String arffDat = path + "kd_2020.arff";

		Instances alleDaten, nurWaren, nurKunden, arffDaten;

		WekaBspStud dt = new WekaBspStud();

		// CSV-Datei laden
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(roh));
		alleDaten = loader.getDataSet();

		// 0 durch ? ersetzen, um f�r die Auswertung nur die Waren zu
		// ber�cksichtigen, die gekauft wurden
		NumericCleaner nc = new NumericCleaner();
		nc.setMinThreshold(1.0); // Schwellwert auf 1 setzen
		nc.setMinDefault(Double.NaN); // alles unter 1 durch ? ersetzen
		nc.setInputFormat(alleDaten);
		alleDaten = Filter.useFilter(alleDaten, nc); // Filter anwenden

		/*
		 * ARFF - Format der Daten f�r Weka erzeugen Das ist zwar komisch (erst
		 * speichern und dann wieder einlesen), geht sicher auch anders. Dr�ber
		 * nachdenken .. irgendwann ;-)
		 */
		// als ARFF speichern
		ArffSaver saver = new ArffSaver();
		saver.setInstances(alleDaten);
		saver.setFile(new File(arffDat));
		saver.writeBatch();

		// Arff-Datei laden
		ArffLoader aLoader = new ArffLoader();
		aLoader.setSource(new File(arffDat));
		arffDaten = aLoader.getDataSet();

		/*
		 * ******************* Start der Auswertungen ***********************
		 */

		// Top-Werte ermitteln
		System.out.println(">>>>>--- Top-Wert ermitteln ----\n");
		System.out.println("Haeufigste Altersgruppe: " + dt.findMaximum(arffDaten, 1) + " Jahre\n");


		// Clusteranalyse mit 5 Clustern �ber alle Daten
		System.out.println(">>>>>--- Clusteranalyse �ber alle Daten, f�nf Cluster ---\n");
		System.out.println(dt.findCluster(alleDaten, 5));

		// Waren rausnehmen, nur Kundendaten stehen lassen
		nurKunden = new Instances(alleDaten);
		for (int i = 0; i < 16; i++) {
			nurKunden.deleteAttributeAt(10); // einzelnes Attribut rausnehmen
		}

		// Clusteranalyse mit 3 Clustern �ber die Kundendaten
		System.out.println(">>>>>--- Clusteranalyse �ber die Kundendaten, drei Cluster ---\n");
		System.out.println(dt.findCluster(nurKunden, 3));

		// Kundendaten rausnehmen, nur Warenk�rbe stehen lassen
		nurWaren = new Instances(alleDaten);
		for (int i = 0; i < 11; i++) {
			nurWaren.deleteAttributeAt(0); // ein einzelnes Attribut rausnehmen
		}

		// Assoziationsanalyse der gekauften Waren
		System.out.println(">>>>>--- Apriori-Analyse (Waren die zusammen gekauft wurden) ---\n");
		String[] aprioriResult = dt.makeApriori(nurWaren);
		for (int i = 0; i < aprioriResult.length; i++) {
			System.out.println(aprioriResult[i]);
		}

	}
}
