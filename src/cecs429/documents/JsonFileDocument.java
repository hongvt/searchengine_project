package cecs429.documents;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Represents a document that is saved as a simple json file in the local file system.
 */
public class JsonFileDocument implements FileDocument
{
    private int mDocumentId;
    private Path mFilePath;
    private String mTitle;

    /**
     * Constructs a JsonFileDocument with the given document ID representing the file at the given
     * absolute file path.
     */
    public JsonFileDocument(int id, Path absoluteFilePath) {
        mDocumentId = id;
        mFilePath = absoluteFilePath;
        mTitle = "";
    }

    @Override
    public Path getFilePath() {
        return mFilePath;
    }

    @Override
    public int getId() {
        return mDocumentId;
    }

    @Override
    public Reader getContent() {
        try {
            Gson gsonObj = new Gson();
            Reader r = Files.newBufferedReader(mFilePath);
            JsonArticle jArticle = gsonObj.fromJson(r,JsonArticle.class);
            return new StringReader(jArticle.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTitle(String title)
    {
        mTitle = title;
    }

    @Override
    public String getTitle() {
        if (mTitle.equals(""))
        {
            try {
                Gson gsonObj = new Gson();
                Reader r = Files.newBufferedReader(mFilePath);
                JsonArticle jArticle = gsonObj.fromJson(r,JsonArticle.class);
                setTitle(jArticle.getTitle());
                return jArticle.getTitle();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return mTitle;
    }

    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
        return new JsonFileDocument(documentId, absolutePath);
    }

    private class JsonArticle
    {
        private String body;
        private String title;
        private String url;

        @Override
        public String toString()
        {
            return body+"\n"+url;
        }

        public String getTitle()
        {
            return title;
        }
    }
}
