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
public class JsonFileDocument implements FileDocument {
    /**
     * The unique doc ID that is used to identify a JSON file
     */
    private int mDocumentId;
    /**
     * The path of where the JSON file is located
     */
    private Path mFilePath;
    /**
     * The value of the JSON attribute "title"
     */
    private String mTitle;

    /**
     * Constructs a JsonFileDocument with the given document ID representing the file at the given absolute file path.
     * Sets mTitle to an empty String
     *
     * @param id               int - mDocumentId is set to this parameter value
     * @param absoluteFilePath Path - mFilePath is set to this parameter value
     */
    public JsonFileDocument(int id, Path absoluteFilePath) {
        mDocumentId = id;
        mFilePath = absoluteFilePath;
        mTitle = "";
    }

    /**
     * mTitle is set to the parameter value
     *
     * @param title String - mTitle is set to this parameter value
     */
    public void setTitle(String title) {
        mTitle = title;
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
            JsonArticle jArticle = gsonObj.fromJson(r, JsonArticle.class);
            r.close();
            return new StringReader(jArticle.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTitle() {
        if (mTitle.equals("")) {
            try {
                Gson gsonObj = new Gson();
                Reader r = Files.newBufferedReader(mFilePath);
                JsonArticle jArticle = gsonObj.fromJson(r, JsonArticle.class);
                r.close();
                setTitle(jArticle.getTitle());
                return jArticle.getTitle();
                //return mFilePath.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return mTitle;
        //return mFilePath.toString();
    }

    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
        return new JsonFileDocument(documentId, absolutePath);
    }

    /**
     * Represents the JSON object parsed from a JSON file
     * ex. all-nps-sites.json has 3 JSON attributes "body", "title", "url"
     */
    private class JsonArticle {
        /**
         * The value of the "body" attribute of the JSON
         */
        private String body;
        /**
         * The value of the "title" attribute of the JSON
         */
        private String title;
        /**
         * The value of the "url" attribute of the JSON
         */
        private String url;

        /**
         * @return String - body
         */
        @Override
        public String toString() {
            return body;
        }

        /**
         * @return String - title
         */
        public String getTitle() { return title; }
    }
}
