/*
 * User: literakl
 * Date: Dec 26, 2001
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.data;

/**
 * This class contains one relation between two GenericObjects.
 * @todo change String name to Document data. XML may contain
 * not only name, but icon too. Or anything else.
 */
public final class Relation extends GenericObject {

    /** Upper relation. Similar to .. in filesystem. */
    int upper = 0;
    GenericObject parent;
    GenericObject child;
    /** Name of the relation. Used, when child's name is not descriptive (or unique) enough in parent's context. */
    String name;


    public Relation() {
    }

    public Relation(int id) {
        super(id);
    }

    /**
     * Constructs new Relation between <code>parent</code> and <code>child</code> (in this order).
     * If this relation is not top-level (e.g. it has its parent), you shall set <code>upper</code>
     * to <code>id</code> of this upper-level relation, otherwise set it to 0.
     */
    public Relation(GenericObject parent, GenericObject child, int upper) {
        this.parent = parent;
        this.child = child;
        this.upper = upper;
    }

    /**
     * @return Name of the relation. Used, when child's name is not descriptive (or unique) enough in parent's context.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of the relation. Used, when child's name is not descriptive (or unique) enough
     * in parent's context. Imagine Category USB from Modems, when linked to Category USB devices.
     * It is better to name this new relation as Modems.
     */
    public void setName(String name) {
        this.name = name;
    }

    public GenericObject getChild() {
        return child;
    }

    public void setChild(GenericObject child) {
        this.child = child;
    }

    public GenericObject getParent() {
        return parent;
    }

    public void setParent(GenericObject parent) {
        this.parent = parent;
    }

    /**
     * @return Upper relation. Similar to .. in filesystem.
     */
    public int getUpper() {
        return upper;
    }

    /**
     * Sets upper relation. Similar to .. in filesystem.
     */
    public void setUpper(int upper) {
        this.upper = upper;
    }

    public void synchronizeWith(GenericObject obj) {
        if ( !(obj instanceof Relation) ) return;
        if ( obj==this ) return;
        super.synchronizeWith(obj);
        Relation r = (Relation) obj;
        upper = r.getUpper();
        name = r.getName();
        parent = r.getParent();
        child = r.getChild();
    }

    public String toString() {
        return "Relation " +id+",upper="+upper;
    }

    public boolean preciseEquals(Object obj) {
        if ( ! (obj instanceof Relation ) ) return false;
        Relation o = (Relation) obj;
        if ( upper==o.getUpper() && parent==o.getParent() && child==o.getChild() ) return true;
        return false;
    }

    public int hashCode() {
        String tmp = "Relation"+id;
        return tmp.hashCode();
    }
}
