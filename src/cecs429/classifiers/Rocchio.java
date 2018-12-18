package cecs429.classifiers;

import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskPositionalIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Rocchio {
    private DiskPositionalIndex hamIndex, jayIndex, madIndex, allIndex, disputedIndex;
    private DocumentCorpus corpusDisputed;
    private ArrayList<ArrayList<Double>> componentsVectorHam = new ArrayList<>();
    private ArrayList<ArrayList<Double>> componentsVectorJay = new ArrayList<>();
    private ArrayList<ArrayList<Double>> componentsVectorMad = new ArrayList<>();
    private ArrayList<ArrayList<Double>> componentsVectorDisputed = new ArrayList<>();

    public Rocchio(DiskPositionalIndex hamIndex, DiskPositionalIndex jayIndex, DiskPositionalIndex madIndex, DiskPositionalIndex allIndex, DiskPositionalIndex disputedIndex, DocumentCorpus corpusDisputed) {
        this.hamIndex = hamIndex;
        this.jayIndex = jayIndex;
        this.madIndex = madIndex;
        this.allIndex = allIndex;
        this.disputedIndex = disputedIndex;
        this.corpusDisputed = corpusDisputed;
    }

    public void applyRocchio() {
        ArrayList<String> classVocab = (ArrayList) allIndex.getVocabulary();

        // JAY: Creating vector based on allIndex's corpus (everything)
        for (int i = 0; i < jayIndex.getVector().size(); i++) {

            componentsVectorJay.add(new ArrayList<>());
            Collection<String> unsortedVocab = jayIndex.getVector().get(i).keySet();
            ArrayList<String> sortedVocab = new ArrayList(unsortedVocab);
            Collections.sort(sortedVocab);

            for (String x : classVocab) {
                if (sortedVocab.contains(x))
                    componentsVectorJay.get(i).add(jayIndex.getVector().get(i).get(x));
                else
                    componentsVectorJay.get(i).add(0.0);
            }
        }

        // Normalizing the vector
        for (int i = 0; i < jayIndex.getLd().size(); i++) {
            for (int j = 0; j < componentsVectorJay.get(i).size(); j++) {
                Double newValue = componentsVectorJay.get(i).get(j) / jayIndex.getLd().get(i);
                componentsVectorJay.get(i).set(j, newValue);
            }
        }

        ArrayList<Double> vectorSumJay = new ArrayList<>(componentsVectorJay.get(0));

        // Summing up all the vectors belonging to the class
        for (int i = 1; i < componentsVectorJay.size(); i++) {
            for (int j = 0; j < componentsVectorJay.get(i).size(); j++) {
                double newValue = vectorSumJay.get(j) + componentsVectorJay.get(i).get(j);
                vectorSumJay.set(j, newValue);
            }
        }

        // Dividing each component by the number of documents within the class
        for (int i = 0; i < vectorSumJay.size(); i++)
            vectorSumJay.set(i, vectorSumJay.get(i) / jayIndex.getVector().size());

        System.out.println(vectorSumJay);

        // HAM: Creating vector based on allIndex's corpus (everything)
        for (int i = 0; i < hamIndex.getVector().size(); i++) {
            componentsVectorHam.add(new ArrayList<>());
            Collection<String> unsortedVocab = hamIndex.getVector().get(i).keySet();
            ArrayList<String> sortedVocab = new ArrayList(unsortedVocab);
            Collections.sort(sortedVocab);

            for (String x : classVocab) {
                if (sortedVocab.contains(x))
                    componentsVectorHam.get(i).add(hamIndex.getVector().get(i).get(x));
                else
                    componentsVectorHam.get(i).add(0.0);
            }
        }

        // Normalizing the vector
        for (int i = 0; i < hamIndex.getLd().size(); i++) {
            for (int j = 0; j < componentsVectorHam.get(i).size(); j++) {
                Double newValue = componentsVectorHam.get(i).get(j) / hamIndex.getLd().get(i);
                componentsVectorHam.get(i).set(j, newValue);
            }
        }

        ArrayList<Double> vectorSumHam = new ArrayList<>(componentsVectorHam.get(0));
        // Summing up all the vectors belonging to the class
        for (int i = 1; i < componentsVectorHam.size(); i++) {
            for (int j = 0; j < componentsVectorHam.get(i).size(); j++) {
                double newValue = vectorSumHam.get(j) + componentsVectorHam.get(i).get(j);
                vectorSumHam.set(j, newValue);
            }
        }

        // Dividing each component by the number of documents within the class
        for (int i = 0; i < vectorSumHam.size(); i++)
            vectorSumHam.set(i, vectorSumHam.get(i) / hamIndex.getVector().size());

        System.out.println(vectorSumHam);


        // MAD: Creating vector based on allIndex's corpus (everything)
        for (int i = 0; i < madIndex.getVector().size(); i++) {
            componentsVectorMad.add(new ArrayList<>());
            Collection<String> unsortedVocab = madIndex.getVector().get(i).keySet();
            ArrayList<String> sortedVocab = new ArrayList(unsortedVocab);
            Collections.sort(sortedVocab);

            for (String x : classVocab) {
                if (sortedVocab.contains(x))
                    componentsVectorMad.get(i).add(madIndex.getVector().get(i).get(x));
                else
                    componentsVectorMad.get(i).add(0.0);
            }
        }

        // Normalizing the vector
        for (int i = 0; i < madIndex.getLd().size(); i++) {
            for (int j = 0; j < componentsVectorMad.get(i).size(); j++) {
                Double newValue = componentsVectorMad.get(i).get(j) / madIndex.getLd().get(i);
                componentsVectorMad.get(i).set(j, newValue);
            }
        }

        ArrayList<Double> vectorSumMad = new ArrayList<>(componentsVectorMad.get(0));
        // Summing up all the vectors belonging to the class
        for (int i = 1; i < componentsVectorMad.size(); i++) {
            for (int j = 0; j < componentsVectorMad.get(i).size(); j++) {
                double newValue = vectorSumMad.get(j) + componentsVectorMad.get(i).get(j);
                vectorSumMad.set(j, newValue);
            }
        }

        // Dividing each component by the number of documents within the class
        for (int i = 0; i < vectorSumMad.size(); i++)
            vectorSumMad.set(i, vectorSumMad.get(i) / madIndex.getVector().size());

        System.out.println(vectorSumMad + "\n\n\n");


        // DISPUTED: Creating vector based on allIndex's corpus (everything)
        for (int i = 0; i < disputedIndex.getVector().size(); i++) {
            componentsVectorDisputed.add(new ArrayList<>());
            Collection<String> unsortedVocab = disputedIndex.getVector().get(i).keySet();
            ArrayList<String> sortedVocab = new ArrayList(unsortedVocab);
            Collections.sort(sortedVocab);
            for (String x : classVocab) {
                if (sortedVocab.contains(x))
                    componentsVectorDisputed.get(i).add(disputedIndex.getVector().get(i).get(x));
                else
                    componentsVectorDisputed.get(i).add(0.0);
            }
        }

        // Normalizing the vector
        for (int i = 0; i < disputedIndex.getLd().size(); i++) {
            for (int j = 0; j < componentsVectorDisputed.get(i).size(); j++) {
                Double newValue = componentsVectorDisputed.get(i).get(j) / disputedIndex.getLd().get(i);
                componentsVectorDisputed.get(i).set(j, newValue);
            }
        }

        // First 30 components of document 52
       /* for(int i = 0; i < 30; i++){
            System.out.print(componentsVectorDisputed.get(3).get(i)+ ", ");
        }*/

        // Euclidean Distance for document 52

        //ArrayList<Double> Doc52 = new ArrayList<>(componentsVectorDisputed.get(3));

        /**
         // Paper 52 stuff
         for(int i = 0; i < vectorSumHam.size(); i++)
         hamScore += (Math.pow((vectorSumHam.get(i) - Doc52.get(i)), 2));

         hamScore = Math.sqrt(hamScore);

         for(int i = 0; i < vectorSumMad.size(); i++)
         madScore += (Math.pow((vectorSumMad.get(i) - Doc52.get(i)), 2));

         madScore = Math.sqrt(madScore);

         for(int i = 0; i < vectorSumJay.size(); i++)
         jayScore += (Math.pow((vectorSumJay.get(i) - Doc52.get(i)), 2));

         jayScore = Math.sqrt(jayScore);


         System.out.println("Hamilton Euclidean Distance: "+hamScore);
         System.out.println("Madison Euclidean Distance: "+madScore);
         System.out.println("Jay Euclidean Distance: "+jayScore);
         */
        double hamScore = 0, madScore = 0, jayScore = 0;
        ArrayList<Double> hamResults = new ArrayList<>();
        ArrayList<Double> madResults = new ArrayList<>();
        ArrayList<Double> jayResults = new ArrayList<>();

        for (int i = 0; i < componentsVectorDisputed.size(); i++) {
            hamScore = 0;
            madScore = 0;
            jayScore = 0;
            for (int j = 0; j < vectorSumHam.size(); j++) {
                hamScore += Math.pow((vectorSumHam.get(j) - componentsVectorDisputed.get(i).get(j)), 2);
                madScore += Math.pow((vectorSumMad.get(j) - componentsVectorDisputed.get(i).get(j)), 2);
                jayScore += Math.pow((vectorSumJay.get(j) - componentsVectorDisputed.get(i).get(j)), 2);
            }
            hamResults.add(Math.sqrt(hamScore));
            madResults.add(Math.sqrt(madScore));
            jayResults.add(Math.sqrt(jayScore));
        }

        System.out.println("Rocchio Classification");
        for (int i = 0; i < corpusDisputed.getCorpusSize(); i++) {
            if (hamResults.get(i) < madResults.get(i) && hamResults.get(i) < jayResults.get(i)) {
                System.out.println(corpusDisputed.getDocument(i).getTitle() + " written by Hamilton");
            } else if (madResults.get(i) < hamResults.get(i) && madResults.get(i) < jayResults.get(i)) {
                System.out.println(corpusDisputed.getDocument(i).getTitle() + " written by Madison");
            } else {
                System.out.println(corpusDisputed.getDocument(i).getTitle() + " was written by Jay");
            }
        }
        System.out.println("\n\n");
    }
}