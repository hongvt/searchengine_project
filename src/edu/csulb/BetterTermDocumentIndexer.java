package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.index.TermDocumentIndex;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;

public class BetterTermDocumentIndexer {
	public static void main(String[] args) throws IOException {
		Path currentPath = Paths.get(System.getProperty("user.dir"));
		Path corpusPath = Paths.get(currentPath.toString(), "corpora", "all-nps-sites");

		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(corpusPath, ".txt");
		((DirectoryCorpus) corpus).registerFileDocumentFactory(".json", JsonFileDocument::loadJsonFileDocument);
		Index index = indexCorpus(corpus) ;

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
		HashSet<String> vocabulary = new HashSet<>();
		BasicTokenProcessor processor = new BasicTokenProcessor();
		
		// First, build the vocabulary hash set.
		
		// TODO:
		// Get all the documents in the corpus by calling GetDocuments().
		Iterable<Document> itrDoc = corpus.getDocuments();
		// Iterate through the documents, and:
		for (Document doc : itrDoc)
		{
			Reader readDoc = doc.getContent();
			EnglishTokenStream ets = new EnglishTokenStream(readDoc);
			Iterable<String> engTokens = ets.getTokens();
			for(String engTok : engTokens)
			{
				String word = processor.processToken(engTok);
				vocabulary.add(word);
			}
			ets.close();
		}
		// Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
		// Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
		//		and adding them to the HashSet vocabulary.
		
		// TODO:
		// Constuct a TermDocumentMatrix once you know the size of the vocabulary.
		// THEN, do the loop again! But instead of inserting into the HashSet, add terms to the index with addPosting.
		TermDocumentIndex termDocIndex = new TermDocumentIndex(vocabulary,corpus.getCorpusSize());
		for (Document doc : itrDoc)
		{
			Reader readDoc = doc.getContent();
			EnglishTokenStream ets = new EnglishTokenStream(readDoc);
			Iterable<String> engTokens = ets.getTokens();
			for(String engTok : engTokens)
			{
				String word = processor.processToken(engTok);
				termDocIndex.addTerm(word,doc.getId());
			}
			ets.close();
		}
		
		return termDocIndex;
	}
}
