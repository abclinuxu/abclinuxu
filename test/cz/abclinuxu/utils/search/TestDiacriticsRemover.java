/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
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
