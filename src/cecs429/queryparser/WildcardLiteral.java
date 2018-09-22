package cecs429.queryparser;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.List;

public class WildcardLiteral implements QueryComponent
{

    @Override
    public List<Posting> getPostings(Index index) {
        return null;
    }
}
