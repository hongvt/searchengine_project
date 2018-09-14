package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.index.Index;
import cecs429.index.InvertedIndex;
import cecs429.index.Posting;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Milestone1UsingPosInvertIndex
{
    public static void main(String[] args) throws IOException
    {
        Scanner keyboard = new Scanner(System.in);
        TokenProcessor processor = new BasicTokenProcessor();

        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path corpusFolder = Paths.get(currentPath.toString(), "corpora");

        System.out.println("Directories");
        System.out.println("------------------------------");
        File[] files = new File(corpusFolder.toString()).listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isDirectory())
            {
                System.out.println(i+1 + ": "+ files[i].getName());
            }
        }

        System.out.print("\nEnter the number of the corpus to index (or \"quit\" to exit): ");
        String dir = processor.processToken(keyboard.next());

        while (!dir.equals("quit"))
        {
            try
            {
                if (Integer.parseInt(dir) <= files.length && Integer.parseInt(dir) > 0)
                {
                    currentPath = Paths.get(corpusFolder.toString(), files[Integer.parseInt(dir)-1].getName());
                    DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(currentPath, ".txt");
                    ((DirectoryCorpus) corpus).registerFileDocumentFactory(".json", JsonFileDocument::loadJsonFileDocument);
                    Index index = indexCorpus(corpus);

                    System.out.print("Enter term to search (or \"quit\" to exit): ");
                    String word = processor.processToken(keyboard.next());

                    while (!word.equals("quit"))
                    {
                        for (Posting p : index.getPostings(word))
                        {
                            //System.out.println("Document ID " + p.getDocumentId());
                            System.out.println("Document Title: " + corpus.getDocument(p.getDocumentId()).getTitle() + " " + p.getPositions);
                        }
                        System.out.print("Enter term to search (or \"quit\" to exit): ");
                        word = processor.processToken(keyboard.next());
                    }
                    System.out.println("\nDirectories");
                    System.out.println("------------------------------");
                    for (int i = 0; i < files.length; i++)
                    {
                        if (files[i].isDirectory())
                        {
                            System.out.println(i+1 + ": "+ files[i].getName());
                        }
                    }

                    System.out.print("\nEnter the number of the corpus to index (or \"quit\" to exit): ");
                    dir = processor.processToken(keyboard.next());
                }
            }
            catch (NumberFormatException e)
            {
                System.out.println("Please enter a number from 1-"+files.length+" (or \"quit\" to exit): ");
                dir = processor.processToken(keyboard.next());
            }
        }


    }

    private static Index indexCorpus(DocumentCorpus corpus) throws IOException
    {
        BasicTokenProcessor processor = new BasicTokenProcessor();
        Iterable<Document> itrDoc = corpus.getDocuments();
        // TODO: @Michael I'm going to use InvertedIndex for now, when you're ready, uncomment the line below or change to whatever you want to call it??
        PositionalInvertedIndex posInvertIndex = new PositionalInvertedIndex();
        //InvertedIndex invertIndex = new InvertedIndex();
        for (Document doc : itrDoc)
        {
            Reader readDoc = doc.getContent();
            EnglishTokenStream ets = new EnglishTokenStream(readDoc);
            Iterable<String> engTokens = ets.getTokens();
            for(String engTok : engTokens)
            {
                String word = processor.processToken(engTok);
                invertIndex.addTerm(word,doc.getId(), doc.getContent());
            }
            ets.close();
        }

        return invertIndex;
    }
}
