package cecs429.classifiers;

import cecs429.index.DiskPositionalIndex;


import java.util.HashMap;
import java.util.HashSet;

public class Bayesian
{
    private DiskPositionalIndex hamIndex;
    private DiskPositionalIndex jayIndex;
    private DiskPositionalIndex madIndex;
    private HashSet<String> discriminatingSet;

    public Bayesian(DiskPositionalIndex hamIndex, DiskPositionalIndex jayIndex, DiskPositionalIndex madIndex)
    {
        this.hamIndex = hamIndex;
        this.jayIndex = jayIndex;
        this.madIndex = madIndex;

        HashSet<String> allVocab = new HashSet<>();
        allVocab.addAll(this.hamIndex.getVocabulary());
        allVocab.addAll(this.jayIndex.getVocabulary());
        allVocab.addAll(this.madIndex.getVocabulary());

        HashMap<String,Double> hamITC = new HashMap<>();
        HashMap<String,Double> jayITC = new HashMap<>();
        HashMap<String,Double> madITC = new HashMap<>();


        //for each type, calculate I(T,C) for each class
        for (String type : allVocab)
        {
            hamITC.put(type,getITC(hamIndex,type));
            jayITC.put(type,getITC(jayIndex,type));
            madITC.put(type,getITC(madIndex,type));
        }

        //for each class, get top K for I(T,C)
    }

    private double getITC(DiskPositionalIndex index, String type)
    {

    }

}
