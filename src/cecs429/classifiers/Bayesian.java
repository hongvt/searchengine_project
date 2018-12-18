package cecs429.classifiers;

import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Posting;


import java.util.*;

public class Bayesian {
    private DiskPositionalIndex hamIndex;
    private DiskPositionalIndex jayIndex;
    private DiskPositionalIndex madIndex;
    private HashSet<String> discriminatingSet;

    public Bayesian(DocumentCorpus idkCorpus,DiskPositionalIndex idkIndex, float numHamDocs, DiskPositionalIndex hamIndex, float numJayDocs, DiskPositionalIndex jayIndex, float numMadDocs, DiskPositionalIndex madIndex)
    {
        this.hamIndex = hamIndex;
        this.jayIndex = jayIndex;
        this.madIndex = madIndex;

        this.discriminatingSet = new HashSet<>();

        HashSet<String> allVocab = new HashSet<>();
        allVocab.addAll(this.hamIndex.getVocabulary());
        allVocab.addAll(this.jayIndex.getVocabulary());
        allVocab.addAll(this.madIndex.getVocabulary());

        HashMap<String, Float> hamITC = new HashMap<>();
        HashMap<String, Float> jayITC = new HashMap<>();
        HashMap<String, Float> madITC = new HashMap<>();

        //System.out.println(allVocab);
        //for each type, calculate I(T,C) for each class
        int i = 0;
        for (String type : allVocab)
        {
            float hamIndexPostings = 0, jayIndexPostings = 0, madIndexPostings = 0;
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
            i++;
            hamITC.put(type,getITC(numHamDocs,hamIndexPostings, (jayIndexPostings+madIndexPostings),(numJayDocs+numMadDocs)-(jayIndexPostings+madIndexPostings)));
            jayITC.put(type,getITC(numJayDocs,jayIndexPostings, (hamIndexPostings+madIndexPostings),(numHamDocs+numMadDocs)-(hamIndexPostings+madIndexPostings)));
            madITC.put(type,getITC(numMadDocs,madIndexPostings, (hamIndexPostings+jayIndexPostings),(numJayDocs+numHamDocs)-(hamIndexPostings+jayIndexPostings)));
        }

        HashMap<String,Float> probHamContainsKeyType = new HashMap<>();
        HashMap<String,Float> probJayContainsKeyType = new HashMap<>();
        HashMap<String,Float> probMadContainsKeyType = new HashMap<>();

        //for each class, get top K for I(T,C)
        Map<String,Float> hamSortedITC = getSortedMap(hamITC,false);
        int k = 0;
        float sumFtHam = 0;
        //System.out.println("Hamilton:");
        for (String type : hamSortedITC.keySet())
        {
            if (k < 50)
            {
                float ftc = 0;
                discriminatingSet.add(type);
                if (k < 10)
                {
                    //System.out.println("type: "+type+"\tITC: "+hamSortedITC.get(type));
                }
                try
                {
                    List<Posting> typeHamPost = hamIndex.getPostingsWithPositions(type);
                    for (Posting p : typeHamPost)
                    {
                        ftc += p.getPositions().size();
                        sumFtHam += p.getPositions().size();
                    }
                }
                catch (NullPointerException e) {}
                probHamContainsKeyType.put(type,ftc);

            }
            k++;
        }

        k = 0;
        Map<String,Float> jaySortedITC = getSortedMap(jayITC,false);
        float sumFtJay = 0;
        //System.out.println("Jay:");
        for (String type : jaySortedITC.keySet())
        {
            if (k < 50)
            {
                float ftc = 0;
                discriminatingSet.add(type);
                if (k < 10)
                {
                    //System.out.println("type: "+type+"\tITC: "+jaySortedITC.get(type));
                }
                try
                {
                    List<Posting> typeJayPost = jayIndex.getPostingsWithPositions(type);
                    for (Posting p : typeJayPost)
                    {
                        ftc += p.getPositions().size();
                        sumFtJay += p.getPositions().size();
                    }
                }
                catch (NullPointerException e) {}
                probJayContainsKeyType.put(type,ftc);
            }
            k++;
        }

        k = 0;
        Map<String,Float> madSortedITC = getSortedMap(madITC,false);
        float sumFtMad = 0;
        //System.out.println("Madison:");
        for (String type : madSortedITC.keySet())
        {
            if (k < 50)
            {
                float ftc = 0;
                discriminatingSet.add(type);
                if (k < 10)
                {
                    //System.out.println("type: "+type+"\tITC: "+madSortedITC.get(type));
                }
                try
                {
                    List<Posting> typeMadPost = madIndex.getPostingsWithPositions(type);
                    for (Posting p : typeMadPost)
                    {
                        ftc += p.getPositions().size();
                        sumFtMad += p.getPositions().size();
                    }
                }
                catch (NullPointerException e) {}
                probMadContainsKeyType.put(type,ftc);
            }
            k++;
        }

        //System.out.println("discriminating set size:"+discriminatingSet.size());

        for (String type : discriminatingSet)
        {
            if (!probHamContainsKeyType.containsKey(type))
            {
                float ftc = 0;
                try
                {
                    List<Posting> typeHamPost = hamIndex.getPostingsWithPositions(type);
                    for (Posting p : typeHamPost)
                    {
                        ftc += p.getPositions().size();
                        sumFtHam += p.getPositions().size();
                    }
                }
                catch (NullPointerException e) {}
                probHamContainsKeyType.put(type,ftc);
            }
            if (!probJayContainsKeyType.containsKey(type))
            {
                float ftc = 0;
                try
                {
                    List<Posting> typeJayPost = jayIndex.getPostingsWithPositions(type);
                    for (Posting p : typeJayPost)
                    {
                        ftc += p.getPositions().size();
                        sumFtJay += p.getPositions().size();
                    }
                }
                catch (NullPointerException e) {}
                probJayContainsKeyType.put(type,ftc);
            }
            if (!probMadContainsKeyType.containsKey(type))
            {
                float ftc = 0;
                try
                {
                    List<Posting> typeMadPost = madIndex.getPostingsWithPositions(type);
                    for (Posting p : typeMadPost)
                    {
                        ftc += p.getPositions().size();
                        sumFtMad += p.getPositions().size();
                    }
                }
                catch (NullPointerException e) {}
                probMadContainsKeyType.put(type,ftc);
            }
        }
        //System.out.println(probHamContainsKeyType);
        //System.out.println("sumFtHam:"+sumFtHam);
        //get probability that a class c contains the type t in discriminating set P(ti,c) - Laplace Smoothing
        for (String type : discriminatingSet)
        {
            probHamContainsKeyType.replace(type,probHamContainsKeyType.get(type),((probHamContainsKeyType.get(type)+1)/(sumFtHam+discriminatingSet.size())));
            probJayContainsKeyType.replace(type,probJayContainsKeyType.get(type),((probJayContainsKeyType.get(type)+1)/(sumFtJay+discriminatingSet.size())));
            probMadContainsKeyType.replace(type,probMadContainsKeyType.get(type),((probMadContainsKeyType.get(type)+1)/(sumFtMad+discriminatingSet.size())));
        }
        //System.out.println(probHamContainsKeyType);

        for (Document doc : idkCorpus.getDocuments())
        {
            int id = doc.getId();
            System.out.println("DOC ID:"+id);
            float sumOfLogPtiHam = 0, sumOfLogPtiJay = 0, sumOfLogPtiMad = 0;
            for (String type :discriminatingSet)
            {
                try
                {
                    List<Posting> posts = idkIndex.getPostingsWithPositions(type);
                    for (Posting post: posts) {
                        if (post.equals(new Posting(id))) {
                            sumOfLogPtiHam += (Math.log(probHamContainsKeyType.get(type)));
                            sumOfLogPtiJay += (Math.log(probJayContainsKeyType.get(type)));
                            sumOfLogPtiMad += (Math.log(probMadContainsKeyType.get(type)));
                        }
                    }
                }
                catch (NullPointerException e) {}
            }

            sumOfLogPtiHam += Math.log((numHamDocs/(numHamDocs+numJayDocs+numMadDocs)));
            sumOfLogPtiJay += Math.log((numJayDocs/(numHamDocs+numJayDocs+numMadDocs)));
            sumOfLogPtiMad += Math.log((numMadDocs/(numHamDocs+numJayDocs+numMadDocs)));

            float max = sumOfLogPtiHam > sumOfLogPtiJay ? sumOfLogPtiHam : sumOfLogPtiJay;
            max = max > sumOfLogPtiMad ? max : sumOfLogPtiMad;
            if (max == sumOfLogPtiHam)
            {
                System.out.println(doc.getTitle() + " class: HAMILTON");
            }
            else if (max == sumOfLogPtiJay)
            {
                System.out.println(doc.getTitle() + " class: JAY");
            }
            else if (max == sumOfLogPtiMad)
            {
                System.out.println(doc.getTitle() +  " class: MAD");
            }
        }
    }


    private float getITC(float numDocs, float N11, float N10, float N00)
    {
        /**
         *              in C: Nt1   not in C: Nt0
         *  has T:  N1c     N11         N10
         *  no T:   N0c     N01         N00
         *
         *  N = N11 + N01, + N10 + Float
         **/
        float N01 = numDocs - N11;
        float N = N10 + N00 + N11 + N01;

        double N11Calc = Double.isNaN(((N11/N)*((Math.log(((N*N11)/((N10+N11)*(N01+N11)))))/(Math.log(2))))) ? 0 : (((N11/N)*((Math.log(((N*N11)/((N10+N11)*(N01+N11)))))/(Math.log(2)))));
        double N10Calc = Double.isNaN((N10/N)*((Math.log(((N*N10)/((N10+N11)*(N00+N10)))))/(Math.log(2)))) ? 0 : (N10/N)*((Math.log(((N*N10)/((N10+N11)*(N00+N10)))))/(Math.log(2)));
        double N01Calc = Double.isNaN((N01/N)*((Math.log(((N*N01)/((N00+N01)*(N01+N11)))))/(Math.log(2)))) ? 0 : ((N01/N)*((Math.log(((N*N01)/((N00+N01)*(N01+N11)))))/(Math.log(2))));
        double N00Calc = Double.isNaN(((N00/N)*((Math.log(((N*N00)/((N00+N01)*(N00+N10)))))/(Math.log(2))))) ? 0 : ((N00/N)*((Math.log(((N*N00)/((N00+N01)*(N00+N10)))))/(Math.log(2))));

        return (float)(N11Calc + N10Calc + N01Calc + N00Calc);
    }

    /**
     * Sorts the HashMap by value which is a Float and returns a new Map with the sorted values
     *
     * @param unsorted - the unsorted HashMap with the wildcard key type
     * @param isAscending boolean - true to get smallest to largest, false to get largest to smallest
     * @return Map that is sorted <Object,Float>
     */
    private static Map<String, Float> getSortedMap(HashMap<String, Float> unsorted, boolean isAscending) {
        List<Map.Entry<String, Float>> list = new LinkedList<>(unsorted.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> e1, Map.Entry<String, Float> e2) {
                if (!isAscending) {
                    return (e2.getValue()).compareTo(e1.getValue());
                } else {
                    return (e1.getValue()).compareTo(e2.getValue());
                }
            }
        });

        Map<String, Float> result = new LinkedHashMap<>();
        for (Map.Entry<String, Float> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
