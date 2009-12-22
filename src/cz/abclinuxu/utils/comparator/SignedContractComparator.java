package cz.abclinuxu.utils.comparator;

import cz.abclinuxu.data.view.SignedContract;

import java.util.Comparator;
import java.text.Collator;

/**
 * Compares two SignedContract by their author's surname.
 * User: literakl
 * Date: 15.12.2009
 */
public class SignedContractComparator implements Comparator {
    Collator collator = Collator.getInstance();

    public int compare(Object o1, Object o2) {
        String s1 = ((SignedContract) o1).getAuthor().getSurname();
        String s2 = ((SignedContract) o2).getAuthor().getSurname();
        return collator.compare(s1, s2);
    }
}
