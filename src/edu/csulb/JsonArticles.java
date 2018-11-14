package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import com.google.gson.*;

import java.io.Reader;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

public class JsonArticles {

    ArrayList<JsonArticle> documents;

    private class JsonArticle
    {
        private String body;
        private String title;
        private String url;


    }


    public static void main(String[] args)
    {
        Gson gsonObj = new Gson();
        System.out.println(Paths.get("").toAbsolutePath().toString());
        DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("").toAbsolutePath(), ".json");

        for (Document doc : corpus.getDocuments())
        {
            Reader readDoc = doc.getContent();
            JsonArticles jsonArticles = gsonObj.fromJson(readDoc,JsonArticles.class);

            for (int i = 0; i < jsonArticles.documents.size(); i++)
            {
                try
                {
                    File file = new File(Paths.get("").toAbsolutePath()+"/corpora/all-nps-sites/"+(i+1)+".json");
                    file.createNewFile();
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(gsonObj.toJson(jsonArticles.documents.get(i)));
                    fileWriter.close();
                }
                catch(IOException e)  { e.printStackTrace();}
            }
        }

    }
}
