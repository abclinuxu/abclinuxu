/*
 * User: literakl
 * Date: 19.7.2003
 * Time: 15:08:22
 */
package cz.abclinuxu.utils;

/**
 * Miscalenous tests.
 */
public class VariousTest {

    public static void main(String[] args) {
        testAnd();
    }

    static void testAnd() {
        boolean canContinue = true;
        System.out.println("canContinue = "+canContinue);
        canContinue &= true;
        System.out.println("canContinue = "+canContinue);
        canContinue &= false;
        System.out.println("canContinue = "+canContinue);
        canContinue &= true;
        System.out.println("canContinue = "+canContinue);
    }
}
