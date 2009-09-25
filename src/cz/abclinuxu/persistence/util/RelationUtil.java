package cz.abclinuxu.persistence.util;

import java.util.List;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;

public class RelationUtil {
	
	// disable instantiation
	private RelationUtil() {
		throw new AssertionError("Unable to instantiate class");
	}
	
	public static Relation findParent(Item item) {
        Persistence persistence = PersistenceFactory.getPersistence();
        List<Relation> parents = persistence.findRelationsFor(item);

        if (parents.size() == 1)
            return parents.get(0);

        throw new MissingArgumentException("Nepodařilo se najít rodičovskou relaci pro objekt " + item.getTypeString() + "!");
    }
}
