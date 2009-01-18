package cz.abclinuxu.utils.comparator;

import java.util.Comparator;
import java.text.Collator;

import cz.abclinuxu.data.User;

/**
 * Compares users by their nickname if defined, or by name otherwise.
 * User: literakl
 * Date: 18.1.2009
 */
public class UserNameComparator implements Comparator<User> {
    
    public int compare(User u1, User u2) {
        String name1 = u1.getNick();
        if (name1 == null)
            name1 = u1.getName();
        String name2 = u2.getNick();
        if (name2 == null)
            name2 = u2.getName();
        return Collator.getInstance().compare(name1, name2);
    }
}
