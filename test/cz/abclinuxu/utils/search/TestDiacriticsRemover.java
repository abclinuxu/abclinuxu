/*
 * User: literakl
 * Date: 26.8.2003
 * Time: 21:49:36
 */
package cz.abclinuxu.utils.search;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import cz.finesoft.socd.analyzer.DiacriticRemover;
import cz.finesoft.socd.analyzer.RemoveDiacriticsReader;

import java.io.Reader;
import java.io.StringReader;

/**
 * Tests Lukas Zaplatal's filter for eastern european languages.
 */
public class TestDiacriticsRemover extends TestCase {
    String sentenceA = "ì¹èø¾ýáíéúù»óïòÌ©ÈØ®ÝÁÍÉÚÙ«ÓÏÒ";
    String sentenceB = "escrzyaieuutodnESCRZYAIEUUTODN";
    DiacriticRemover remover = DiacriticRemover.getInstance();

    /**
     * tests DiacriticRemover for correctness.
     * @throws Exception
     */
    public void testRemover() throws Exception {
        String sentenceC = remover.removeDiacritics(sentenceA);
        assertEquals(sentenceB,sentenceC);
    }

    /**
     * Tests RemoveDiacriticsReader correctness.
     * @throws Exception
     */
    public void testRemoveDiacriticsReader() throws Exception {
        Reader reader = new RemoveDiacriticsReader(new StringReader(sentenceA));
        StringBuffer sb = new StringBuffer();
        char[] cbuf = new char[9];
        int count = reader.read(cbuf, 0, cbuf.length);
        while (count!=-1) {
            for ( int i = 0; i<count; i++ )
                sb.append(cbuf[i]);
            count = reader.read(cbuf,0,cbuf.length);
        }
        String sentenceC = sb.toString();
        assertEquals(sentenceB, sentenceC);
    }

    public TestDiacriticsRemover(String s) {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestDiacriticsRemover.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
