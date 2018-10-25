package cecs429.index;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

//to check the .bin files:
//vim <fileName>.bin
//type :
//then copy and paste
//% ! xxd
public class DiskIndexWriter
{
    private DataOutputStream postData, vocabData, vocabTableData, docWeightsData;

    public DiskIndexWriter(Path indexFolder)
    {
        try
        {
            FileOutputStream postingsFile = new FileOutputStream(indexFolder.toString()+"/index/postings.bin");
            postData = new DataOutputStream(postingsFile);

            FileOutputStream vocabFile = new FileOutputStream(indexFolder.toString()+"/index/vocab.bin");
            vocabData = new DataOutputStream(vocabFile);

            FileOutputStream vocabTableFile = new FileOutputStream(indexFolder.toString()+"/index/vocabTable.bin");
            vocabTableData = new DataOutputStream(vocabTableFile);

            FileOutputStream docWeightsFile = new FileOutputStream(indexFolder.toString()+"/index/docWeights.bin");
            docWeightsData = new DataOutputStream(docWeightsFile);
        }
        catch (IOException e) {System.out.println("IO exception"); e.printStackTrace(); }
    }
    public void writeDocWeight(double docWeight)
    {
        try {
            docWeightsData.writeDouble(docWeight);
        }
        catch (IOException e) {System.out.println("IO exception"); e.printStackTrace(); }
    }

    public void closeDocWeights()
    {
        try
        {
            docWeightsData.close();
        }
        catch (IOException e) {System.out.println("IO exception"); e.printStackTrace(); }
    }

    public void writeIndex(Index index)
    {
        try
        {
            long nextVocabPos = 0;
            long nextPostPos = 0;
            List<String> vocabList = index.getVocabulary();
            for (int i = 0; i < vocabList.size(); i++)
            {
                writeVocabTable(nextVocabPos, nextPostPos);
                nextVocabPos += writeVocab(vocabList.get(i));
                nextPostPos += writePostings(vocabList.get(i), index);
            }
            closeVocabPostOutput();
        }
        catch (IOException e) { System.out.println("IO exception"); e.printStackTrace(); }

    }

    private void closeVocabPostOutput()
    {
        try
        {
            vocabData.close();
            vocabTableData.close();
            postData.close();
        }
        catch (IOException e) {System.out.println("IO exception"); e.printStackTrace(); }
    }

    private long writePostings(String term, Index index) throws IOException
    {
        long acc = 0;
        int numPostings = index.getPostings(term).size();
        postData.writeInt(numPostings);
        acc += 4;
        for (int i = 0; i < numPostings; i++)
        {
            Posting posting = index.getPostings(term).get(i);
            int compressedDocId = posting.getDocumentId();
            if (i != 0)
            {
                compressedDocId -= index.getPostings(term).get(i-1).getDocumentId();
            }
            postData.writeInt(compressedDocId);
            acc += 4;
            postData.writeInt(posting.getPositions().size());
            acc += 4;
            for (int j = 0; j < posting.getPositions().size(); j++)
            {
                int compressedPos = posting.getPositions().get(j);
                if (j != 0)
                {
                    compressedPos -= posting.getPositions().get(j-1);
                }
                postData.writeInt(compressedPos);
                acc += 4;
            }
        }
        return acc;
    }

    private long writeVocab(String term) throws IOException
    {
        byte[] termBytes = term.getBytes();
        vocabData.write(termBytes);
        return termBytes.length;
    }

    private void writeVocabTable(long nextVocabPos, long nextPostPos) throws IOException
    {
        vocabTableData.writeLong(nextVocabPos);
        vocabTableData.writeLong(nextPostPos);
    }
}