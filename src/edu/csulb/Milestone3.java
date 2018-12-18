package edu.csulb;

import cecs429.classifiers.Bayesian;
import cecs429.classifiers.Rocchio;
import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.index.*;
import cecs429.text.EnglishTokenStream;
import cecs429.text.Milestone1TokenProcessor;
import cecs429.text.TokenProcessor;

import javax.swing.text.Position;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Milestone3 {
    public static void main(String[] args) {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        TokenProcessor tokenProcessor = new Milestone1TokenProcessor();
        KGramIndex kgi = null;

        Path corpusFolderHam = Paths.get(currentPath.toString(), "corpora/federalist-papers/HAMILTON");
        DocumentCorpus corpusHAM = DirectoryCorpus.loadTextDirectory(corpusFolderHam, ".txt");
        DiskPositionalIndex diskPosIndexHAM = new DiskPositionalIndex(corpusFolderHam, tokenProcessor, kgi);
        try {
            buildIndex(corpusFolderHam, tokenProcessor, corpusHAM, diskPosIndexHAM);
        } catch (IOException e) {
            System.out.println("Need some fixing in buildIndex()");
        }


        Path corpusFolderJay = Paths.get(currentPath.toString(), "corpora/federalist-papers/JAY");
        DocumentCorpus corpusJAY = DirectoryCorpus.loadTextDirectory(corpusFolderJay, ".txt");
        DiskPositionalIndex diskPosIndexJAY = new DiskPositionalIndex(corpusFolderJay, tokenProcessor, kgi);
        try {
            buildIndex(corpusFolderJay, tokenProcessor, corpusJAY, diskPosIndexJAY);
        } catch (IOException e) {
            System.out.println("Need some fixing in buildIndex()");
        }


        Path corpusFolderMad = Paths.get(currentPath.toString(), "corpora/federalist-papers/MADISON");
        DocumentCorpus corpusMAD = DirectoryCorpus.loadTextDirectory(corpusFolderMad, ".txt");
        DiskPositionalIndex diskPosIndexMAD = new DiskPositionalIndex(corpusFolderMad, tokenProcessor, kgi);
        try {
            buildIndex(corpusFolderMad, tokenProcessor, corpusMAD, diskPosIndexMAD);
        } catch (IOException e) {
            System.out.println("Need some fixing in buildIndex()");
        }

        Path corpusFolderAll = Paths.get(currentPath.toString(), "corpora/federalist-papers/ALL");
        DocumentCorpus corpusAll = DirectoryCorpus.loadTextDirectory(corpusFolderAll, ".txt");
        DiskPositionalIndex diskPosIndexAll = new DiskPositionalIndex(corpusFolderMad, tokenProcessor, kgi);

        try {
            buildIndex(corpusFolderAll, tokenProcessor, corpusAll, diskPosIndexAll);
        } catch (IOException e) {
            System.out.println("Need some fixing in buildIndex()");
        }
        Path corpusFolderDisputed = Paths.get(currentPath.toString(), "corpora/federalist-papers/DISPUTED");
        DocumentCorpus corpusDisputed = DirectoryCorpus.loadTextDirectory(corpusFolderDisputed, ".txt");
        DiskPositionalIndex diskPosIndexDisputed = new DiskPositionalIndex(corpusFolderDisputed, tokenProcessor, kgi);

        try {
            buildIndex(corpusFolderDisputed, tokenProcessor, corpusDisputed, diskPosIndexDisputed);
        } catch (IOException e) {
            System.out.println("Need some fixing in buildIndex()");
        }


        Rocchio rocchio = new Rocchio(diskPosIndexHAM, diskPosIndexJAY, diskPosIndexMAD, diskPosIndexAll, diskPosIndexDisputed, corpusDisputed);
        rocchio.applyRocchio();
        Bayesian bayesian = new Bayesian(corpusDisputed,diskPosIndexDisputed,corpusHAM.getCorpusSize(), diskPosIndexHAM, corpusJAY.getCorpusSize(), diskPosIndexJAY, corpusMAD.getCorpusSize(), diskPosIndexMAD);
    }

    /**
     * Indexes the corpus, writes the corpus to disk (builds the index), serializes KGramIndex to disk
     *
     * @param currentPath    - the path of the corpus to index and build
     * @param tokenProcessor - the token processor used for Milestone 2
     * @param corpus         - the corpus that contains all the documents
     * @throws IOException
     */
    private static void buildIndex(Path currentPath, TokenProcessor tokenProcessor, DocumentCorpus corpus, DiskPositionalIndex diskPos) throws IOException {
        //index the corpus - into PositionalInvertedIndex, writes docWeights.bin
        long startTime = System.currentTimeMillis();
        DiskIndexWriter indexWriter = new DiskIndexWriter(currentPath);
        PositionalInvertedIndex index = indexCorpus(tokenProcessor, corpus, indexWriter, currentPath);

        long endTime = System.currentTimeMillis();
        int numSeconds = ((int) ((endTime - startTime) / 1000));
        System.out.println("Indexing took " + numSeconds + " seconds");
        //building the corpus (writing to disk)
        long startTime1 = System.currentTimeMillis();
        indexWriter.writeIndex(index);
        indexWriter.closeVocabPostOutput();
        diskPos.setVector(index.getVector());
        diskPos.setLd(index.getLd());
        long endTime1 = System.currentTimeMillis();
        int numSeconds1 = ((int) ((endTime1 - startTime1) / 1000));
        System.out.println("Building took " + numSeconds1 + " seconds");
    }

    /**
     * Indexes the selected corpus
     *
     * @param corpus      - the corpus to index
     * @param indexWriter - the disk writer is used to calculate docWeights in this method
     * @return Index contains info on every term in every document within selected corpus
     * @throws IOException
     */
    private static PositionalInvertedIndex indexCorpus(TokenProcessor processor, DocumentCorpus corpus, DiskIndexWriter indexWriter, Path corpusFolder) throws IOException {
        HashSet<String> KGI = new HashSet<>();
        Iterable<Document> itrDoc = corpus.getDocuments();
        PositionalInvertedIndex posInvertedIndex = new PositionalInvertedIndex(processor);

        int positionCounter;
        int numWritten = 0;

        for (Document doc : itrDoc) {
            posInvertedIndex.getVector().add(new HashMap<>());
            positionCounter = 0;
            Reader readDoc = doc.getContent();
            EnglishTokenStream ets = new EnglishTokenStream(readDoc);
            Iterable<String> engTokens = ets.getTokens();
            //key=term, value=num of times the term appears in the doc, term freq
            HashMap<String, Integer> typeTermFreq = new HashMap<>();
            for (String engTok : engTokens) {
                String[] types = posInvertedIndex.getProcessor().processButDontStemTokensAKAGetType(engTok);
                //adding TYPES
                for (String x : types)
                    KGI.add(x);

                String[] stems = posInvertedIndex.getProcessor().getStems(types);
                for (int i = 0; i < stems.length; i++) {
                    if (stems[i].length() > 1) {
                        if (stems.length > 1) {
                            if (i == 0) {
                                positionCounter++;
                            }
                            posInvertedIndex.addTerm(stems[i], doc.getId(), positionCounter);
                        } else {
                            positionCounter++;
                            posInvertedIndex.addTerm(stems[i], doc.getId(), positionCounter);
                        }

                        if (!typeTermFreq.isEmpty() && typeTermFreq.containsKey(stems[i])) {
                            typeTermFreq.replace(stems[i], typeTermFreq.get(stems[i]), typeTermFreq.get(stems[i]) + 1);
                        } else {
                            typeTermFreq.put(stems[i], 1);
                        }
                    }
                }
            }
            double sumWDT = 0;
            for (String type : typeTermFreq.keySet()) {
                double wdt = (1 + Math.log((double) typeTermFreq.get(type)));
                posInvertedIndex.getVector().get(numWritten).put(type, wdt);
                wdt *= wdt;
                sumWDT += wdt;
            }
            double ld = Math.sqrt(sumWDT);
            posInvertedIndex.getLd().add(ld);
            indexWriter.writeDocWeight(ld);
            ets.close();
            numWritten++;
        }
        indexWriter.closeDocWeights();
        posInvertedIndex.addToKGI(KGI);
        return posInvertedIndex;
    }
}