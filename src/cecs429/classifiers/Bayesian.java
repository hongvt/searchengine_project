package cecs429.classifiers;

import cecs429.index.DiskPositionalIndex;


import java.util.*;

public class Bayesian
{
    private DiskPositionalIndex hamIndex;
    private DiskPositionalIndex jayIndex;
    private DiskPositionalIndex madIndex;
    private HashSet<String> discriminatingSet;

    public Bayesian(int numHamDocs,DiskPositionalIndex hamIndex, int numJayDocs,DiskPositionalIndex jayIndex, int numMadDocs,DiskPositionalIndex madIndex)
    {
        this.hamIndex = hamIndex;
        this.jayIndex = jayIndex;
        this.madIndex = madIndex;

        HashSet<String> allVocab = new HashSet<>();
        allVocab.addAll(this.hamIndex.getVocabulary());
        System.out.println("hamIndexGetVocabLength="+this.hamIndex.getVocabulary().size());
        allVocab.addAll(this.jayIndex.getVocabulary());
        allVocab.addAll(this.madIndex.getVocabulary());

        HashMap<String,Double> hamITC = new HashMap<>();
        HashMap<String,Double> jayITC = new HashMap<>();
        HashMap<String,Double> madITC = new HashMap<>();

        System.out.println(allVocab);
        //for each type, calculate I(T,C) for each class
        int i = 0;
        for (String type : allVocab)
        {
            double hamIndexPostings = 0, jayIndexPostings = 0, madIndexPostings = 0;
            try
            {
                hamIndexPostings = hamIndex.getPostingsNoPositions(type).length;
            }
            catch(NullPointerException e) { }
            try
            {
                jayIndexPostings = jayIndex.getPostingsNoPositions(type).length;
            }
            catch(NullPointerException e) { }
            try
            {
                madIndexPostings = madIndex.getPostingsNoPositions(type).length;
            }
            catch(NullPointerException e) { }
            if (i == 0)
            {
                System.out.println("type="+type);
                System.out.println("hamIndexPostings="+hamIndexPostings);
                System.out.println("jayIndexPostings="+jayIndexPostings);
                System.out.println("madIndexPostings="+madIndexPostings);
                System.out.println("hamITC="+getITC(numHamDocs,hamIndexPostings,(jayIndexPostings+madIndexPostings),((numJayDocs+numMadDocs)-(jayIndexPostings+madIndexPostings))));
                System.out.println("jayITC="+getITC(numJayDocs,jayIndexPostings,(hamIndexPostings+madIndexPostings),((numHamDocs+numMadDocs)-(hamIndexPostings+madIndexPostings))));
                System.out.println("madITC="+getITC(numMadDocs,madIndexPostings,(hamIndexPostings+jayIndexPostings),((numJayDocs+numHamDocs)-(hamIndexPostings+jayIndexPostings))));
                double N10 = jayIndexPostings+madIndexPostings;
                System.out.println("N10="+(jayIndexPostings+madIndexPostings));
                double N00 = (numJayDocs+numMadDocs)-(jayIndexPostings+madIndexPostings);
                System.out.println("N00="+((numJayDocs+numMadDocs)-(jayIndexPostings+madIndexPostings)));
                double N11 = hamIndexPostings;
                System.out.println("N11="+hamIndexPostings);
                double N01 = (numHamDocs-hamIndexPostings);
                System.out.println("N01="+(numHamDocs-hamIndexPostings));
                double N = N10 + N00+N11+N01;
                System.out.println("N="+(hamIndexPostings+(numJayDocs+numMadDocs-(jayIndexPostings+madIndexPostings))+jayIndexPostings+madIndexPostings+numHamDocs-hamIndexPostings));
                System.out.println("((N10/N)*(Math.log(((N*N10)/((N10+N11)*(N00+N10))))/Math.log(2)))="+((N10/N)*(Math.log(((N*N10)/((N10+N11)*(N00+N10))))/Math.log(2))));
                System.out.println("((N11/N)*((Math.log(((N*N11)/((N10+N11)*(N01+N11)))))/(Math.log(2))))="+((N11/N)*((Math.log(((N*N11)/((N10+N11)*(N01+N11)))))/(Math.log(2)))));
                System.out.println("(N11/N)="+(N11/N));
                System.out.println("((N*N11)/((N10+N11)*(N01+N11))="+(N*N11)/((N10+N11)*(N01+N11)));
                System.out.println();
                System.out.println("((N01/N)*((Math.log(((N*N01)/((N00+N01)*(N01+N11)))))/(Math.log(2))))="+((N01/N)*((Math.log(((N*N01)/((N00+N01)*(N01+N11)))))/(Math.log(2)))));
                System.out.println("((N00/N)*((Math.log(((N*N00)/((N00+N01)*(N00+N10)))))/(Math.log(2))))="+((N00/N)*((Math.log(((N*N00)/((N00+N01)*(N00+N10)))))/(Math.log(2)))));
            }
            i++;
            hamITC.put(type,getITC(numHamDocs,hamIndexPostings, (jayIndexPostings+madIndexPostings),(numJayDocs+numMadDocs)-(jayIndexPostings+madIndexPostings)));
            jayITC.put(type,getITC(numJayDocs,jayIndexPostings, (hamIndexPostings+madIndexPostings),(numHamDocs+numMadDocs)-(hamIndexPostings+madIndexPostings)));
            madITC.put(type,getITC(numMadDocs,madIndexPostings, (hamIndexPostings+jayIndexPostings),(numJayDocs+numHamDocs)-(hamIndexPostings+jayIndexPostings)));
        }

        //for each class, get top K for I(T,C)
        /*Map<String,Double> hamSortedITC = getSortedMap(hamITC,true);
        for (String type : hamSortedITC.keySet())
        {
            System.out.println(type+":"+hamSortedITC.get(type));
        }*/

        /*Map<String,Double> hamSortedITC = getSortedMap(hamITC,false);
        for (String type : hamSortedITC.keySet())
        {
            System.out.println(type+":"+hamSortedITC.get(type));
        }

        Map<String,Double> hamSortedITC = getSortedMap(hamITC,false);
        for (String type : hamSortedITC.keySet())
        {
            System.out.println(type+":"+hamSortedITC.get(type));
        }*/

    }



    private double getITC(double numDocs, double N11, double N10, double N00)
    {
        /**
         *              in C: Nt1   not in C: Nt0
         *  has T:  N1c
         *  no T:   N0c
         *
         *  N = N11 + N01, + N10 + N00
         */
        double N01 = numDocs - N11;
        double N = N10 + N00 + N11 + N01;

        return ((N11/N)*((Math.log(((N*N11)/((N10+N11)*(N01+N11)))))/(Math.log(2)))) +
                ((N10/N)*((Math.log(((N*N10)/((N10+N11)*(N00+N10)))))/(Math.log(2)))) +
                ((N01/N)*((Math.log(((N*N01)/((N00+N01)*(N01+N11)))))/(Math.log(2)))) +
                ((N00/N)*((Math.log(((N*N00)/((N00+N01)*(N00+N10)))))/(Math.log(2))));
    }

    /**
     * Sorts the HashMap by value which is a Double and returns a new Map with the sorted values
     *
     * @param unsorted - the unsorted HashMap with the wildcard key type
     * @param isAscending boolean - true to get smallest to largest, false to get largest to smallest
     * @return Map that is sorted <Object,Double>
     */
    private static Map<String, Double> getSortedMap(HashMap<String, Double> unsorted, boolean isAscending) {
        List<Map.Entry<String, Double>> list = new LinkedList<>(unsorted.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2) {
                if (!isAscending) {
                    return (e2.getValue()).compareTo(e1.getValue());
                } else {
                    return (e1.getValue()).compareTo(e2.getValue());
                }

            }
        });

        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
