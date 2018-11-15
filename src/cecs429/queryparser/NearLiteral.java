package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.List;


/**
 * Performs the check to see if the right term is within K terms of the leftTerm.
 */
public class NearLiteral implements QueryComponent {
    /**
     * Represents the leftTerm that was parsed from the NearQuery
     */
    private String leftTerm;
    /**
     * Represents the rightTerm that was parsed from the NearQuery
     */
    private String rightTerm;
    /**
     * Represents the bounds at which the rightTerm is within the leftTerm
     */
    private int k;


    /**
     * @param leftTerm  - Represents the leftTerm that was parsed from the NearQuery
     * @param rightTerm - Represents the rightTerm that was parsed from the NearQuery
     * @param k         - Represents the bounds at which the rightTerm is within the leftTerm
     */
    public NearLiteral(String leftTerm, String rightTerm, int k) {
        this.leftTerm = leftTerm;
        this.rightTerm = rightTerm;
        this.k = k;
    }


    /**
     * @param list_one - the postings list for the termLiteral of leftTerm
     * @param list_two - the postings list for the termLiteral of rightTerm
     * @param nearK    - the bounds at which the rightTerm is within the leftTerm
     * @return returns a List of Postings that meet the criteria for the right term being
     * within the range of nearK of the leftTerm
     */
    private List<Posting> nearMerge(List<Posting> list_one, List<Posting> list_two, int nearK) {
        List<Posting> result = new ArrayList<>();
        int i = 0, j = 0, k, m;

        while (true) {
            if (list_one == null || list_two == null || i == list_one.size() || j == list_two.size())
                return result;
            else if (list_one.get(i).getDocumentId() == list_two.get(j).getDocumentId()) {
                k = 0;
                m = 0;
                while (true) {
                    if (k == list_one.get(i).getPositions().size() || m == list_two.get(j).getPositions().size()) {
                        i++;
                        j++;
                        break;
                    } else if (list_one.get(i).getPositions().get(k) <= list_two.get(j).getPositions().get(m) &&
                            list_one.get(i).getPositions().get(k) + nearK >= list_two.get(j).getPositions().get(m)) {
                        if (result.isEmpty())
                            result.add(new Posting(list_two.get(j).getDocumentId()));
                        else if (result.get(result.size() - 1).getDocumentId() != list_two.get(j).getDocumentId())
                            result.add(new Posting(list_two.get(j).getDocumentId()));
                        k++;
                    } else {
                        if (list_one.get(i).getPositions().get(k) + nearK < list_two.get(j).getPositions().get(m))
                            k++;
                        else
                            m++;
                    }
                }
            } else {
                if (list_one.get(i).getDocumentId() < list_two.get(j).getDocumentId())
                    i++;
                else
                    j++;
            }
        }
    }

    @Override
    public List<Posting> getPostings(Index index) {
        return nearMerge(index.getPostingsWithPositions(index.getProcessor().getStem(leftTerm)), index.getPostingsWithPositions(index.getProcessor().getStem(rightTerm)), k);
    }
}
