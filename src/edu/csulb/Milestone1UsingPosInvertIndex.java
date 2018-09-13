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

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Milestone1UsingPosInvertIndex
{
    public static void main(String[] args) throws IOException {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path corpusPath = Paths.get(currentPath.toString(), "corpora", "all-nps-sites");

        DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(corpusPath, ".txt");
        ((DirectoryCorpus) corpus).registerFileDocumentFactory(".json", JsonFileDocument::loadJsonFileDocument);
        Index index = indexCorpus(corpus);


        TokenProcessor processor = new BasicTokenProcessor();
        Scanner keyboard = new Scanner(System.in);

        System.out.print("Enter term to search (or \"quit\" to exit): ");
        String word = processor.processToken(keyboard.next());

        while (!word.equals("quit"))
        {
            for (Posting p : index.getPostings(word)) {
                //System.out.println("Document ID " + p.getDocumentId());
                System.out.println("Document Title: " + corpus.getDocument(p.getDocumentId()).getTitle());
            }
            System.out.print("Enter term to search (or \"quit\" to exit): ");
            word = processor.processToken(keyboard.next());
        }
    }

    private static Index indexCorpus(DocumentCorpus corpus) throws IOException
    {
        BasicTokenProcessor processor = new BasicTokenProcessor();
        Iterable<Document> itrDoc = corpus.getDocuments();
        // TODO: @Michael I'm going to use InvertedIndex for now, when you're ready, uncomment the line below or change to whatever you want to call it??
        //PositionalInvertedIndex posInvertIndex = new PositionalInvertedIndex();
        InvertedIndex invertIndex = new InvertedIndex();
        for (Document doc : itrDoc)
        {
            Reader readDoc = doc.getContent();
            EnglishTokenStream ets = new EnglishTokenStream(readDoc);
            Iterable<String> engTokens = ets.getTokens();
            for(String engTok : engTokens)
            {
                String word = processor.processToken(engTok);
                invertIndex.addTerm(word,doc.getId());
            }
            ets.close();
        }

        return invertIndex;
    }
}
