package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.queryparser.BooleanQueryParser;
import cecs429.queryparser.QueryComponent;
import cecs429.text.EnglishTokenStream;
import cecs429.text.Milestone1TokenProcessor;
import cecs429.text.TokenProcessor;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Milestone1UsingPosInvertIndex {
    private static void printDirectoryList(Path corpusFolder) {
        System.out.println("Directories");
        System.out.println("------------------------------");
        File[] files = new File(corpusFolder.toString()).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                System.out.println(i + 1 + ": " + files[i].getName());
            }
        }
    }

    private static String getDirectoryName(Scanner keyboard, Path corpusFolder) {
        printDirectoryList(corpusFolder);
        System.out.print("\nEnter the name of the corpus to index (or \"quit\" to exit): ");
        String dir = keyboard.nextLine();

        while (!dir.equals("quit") && !dir.equals(":q")) {
            if (hasDirectory(corpusFolder, dir)) {
                return dir;
            } else {
                System.out.println("Please recheck spelling of directory");
            }
            System.out.print("Enter the name of the corpus to index (or \"quit\" to exit): ");
            dir = keyboard.nextLine();
        }
        return "quit";
    }

    private static boolean hasDirectory(Path corpusFolder, String dir) {
        File[] files = new File(corpusFolder.toString()).listFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                if (files[i].getName().equals(dir)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        Scanner keyboard = new Scanner(System.in);
        TokenProcessor processor = new Milestone1TokenProcessor();
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path corpusFolder = Paths.get(currentPath.toString(), "corpora");

        String dir = getDirectoryName(keyboard, corpusFolder);
        while (!dir.equals("quit") && !dir.equals(":q")) {
            boolean changeDirectory = false;
            currentPath = Paths.get(corpusFolder.toString(), dir);
            DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(currentPath, ".txt");
            ((DirectoryCorpus) corpus).registerFileDocumentFactory(".json", JsonFileDocument::loadJsonFileDocument);

            //index the corpus
            long startTime = System.currentTimeMillis();
            Index index = indexCorpus(corpus);
            long endTime = System.currentTimeMillis();
            int numSeconds = ((int) ((endTime - startTime) / 1000));
            System.out.println("Indexing took " + numSeconds + " seconds");

            System.out.print("Enter term to search (or \"quit\" to exit): ");
            String word = keyboard.nextLine();
            while (!word.equals("quit") && !word.equals(":q")) {
                String[] words = word.split(" ");
                if (words[0].equals(":index")) {
                    if (hasDirectory(corpusFolder, words[1])) {
                        word = "quit";
                        dir = words[1];
                        changeDirectory = true;
                    } else {
                        System.out.println("Please recheck spelling of directory");
                    }
                } else {
                    if (words[0].equals(":stem")) {
                        String[] stems = processor.processTokens(words[1]);
                        for (int i = 0; i < stems.length; i++) {
                            System.out.println(i + ":" + stems[i]);
                        }
                    } else if (words[0].equals(":vocab")) {
                        for (int i = 0; i < 1000; i++) {
                            System.out.println(index.getVocabulary().get(i));
                        }
                    } else //TODO:: ADD BOOLEAN QUERY PARSER HERE
                    {

                        BooleanQueryParser pa = new BooleanQueryParser();
                        QueryComponent c = pa.parseQuery(word);
                        System.out.println("posting size: " + c.getPostings(index, processor).size());
                        for (Posting x : c.getPostings(index, processor)) {
                            System.out.println(x.getDocumentId() + " " + x.getPositions());
                        }
                        /*
                        String[] stems = processor.processTokens(word);
                        int j = 0;
                        for (int i = 0; i < stems.length; i++)
                        {
                            for (Posting p : index.getPostings(stems[i]))
                            {
                                System.out.println("Document Title: " + corpus.getDocument(p.getDocumentId()).getTitle() + " " + p.getPositions());
                                j++;
                            }
                        }
                        System.out.println(j+" documents were found");*/
                    }
                }
                System.out.print("Enter term to search (or \"quit\" to exit): ");
                word = keyboard.nextLine();
            }
            if (!word.equals(":q")) { //word must equal "quit" to go in here
                if (!changeDirectory) {
                    dir = getDirectoryName(keyboard, corpusFolder);
                }
            } else if (word.equals(":q"))// word == :q so quit program
            {
                dir = "quit";
            }
        }
        keyboard.close();
    }

    private static Index indexCorpus(DocumentCorpus corpus) throws IOException {
        TokenProcessor processor = new Milestone1TokenProcessor();
        Iterable<Document> itrDoc = corpus.getDocuments();
        PositionalInvertedIndex posInvertIndex = new PositionalInvertedIndex();
        int positionCounter;

        for (Document doc : itrDoc) {
            positionCounter = 0;
            Reader readDoc = doc.getContent();
            EnglishTokenStream ets = new EnglishTokenStream(readDoc);
            Iterable<String> engTokens = ets.getTokens();
            for (String engTok : engTokens) {
                String[] stems = processor.processTokens(engTok);
                for (int i = 0; i < stems.length; i++) {
                    if (stems.length > 1) {
                        if (i == 0) {
                            positionCounter++;
                        }
                        posInvertIndex.addTerm(stems[i], doc.getId(), positionCounter);
                    } else {
                        positionCounter++;
                        posInvertIndex.addTerm(stems[i], doc.getId(), positionCounter);
                    }
                }
            }
            ets.close();
        }
        return posInvertIndex;
    }
}