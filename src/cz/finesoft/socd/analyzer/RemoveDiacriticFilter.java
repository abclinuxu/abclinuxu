package cz.finesoft.socd.analyzer;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;

/** Removes all standard european diacritics */

public final class RemoveDiacriticFilter extends TokenFilter {
    private static DiacriticRemover dr = DiacriticRemover.getInstance();

    public RemoveDiacriticFilter(TokenStream in) {
        input = in;
    }

    public final Token next() throws java.io.IOException {
        Token t = input.next();
        if ( t==null )
            return null;

        return (new Token(dr.removeDiacritics(t.termText()), t.startOffset(), t.endOffset(), t.type()));
    }
}
