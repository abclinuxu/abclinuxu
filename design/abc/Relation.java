/*
 * User: literakl
 * Date: Dec 26, 2001
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package abc;

/**
 * This class contains one relation between two GenericObjects.
 */
public final class Relation extends GenericObject {

    /** Upper relation. Similar to .. in filesystem. */
    int upper = 0;
    GenericObject parent;
    GenericObject child;
    /** Name of the relation. Used, when child's name is not descriptive (or unique) enough in parent's context. */
    String name;

}
