package cz.abclinuxu.migrate;

/**
 * Superclass for migration tools.
 * User: literakl
 * Date: 15.2.2009
 */
public class Migration {
    protected static int counter;

    protected static void hash() {
        System.out.print('#');
        if (counter % 50 == 49) {
            System.out.println(" " + (counter + 1));
            System.out.flush();
        }
        counter++;
    }

    protected static void resetCounter() {
        counter = 0;
        System.out.println();
    }

    protected static int getCounter() {
        return counter;
    }
}
