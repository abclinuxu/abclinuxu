package net.eithel;

import java.util.Comparator;

/**
 * Seven times faster competitor for standard Java Collator.
 * http://www.eithel.net/download/algoritmy/abc_sort/
 * @author Jan Dolezel
 * @since 6.1.2008
 */
public class CzechComparator implements Comparator<String> {
    private static final char[] translateTableFrom = {'á', 'ä', 'ď', 'é', 'ě', 'ë', 'í', 'ň', 'ó', 'ö', 'š', 'ß', 'ť', 'ú', 'ů', 'ü', 'ý', 'ÿ'};
    private static final char[] translateTableTo = {'a', 'a', 'd', 'e', 'e', 'e', 'i', 'n', 'o', 'o', 's', 's', 't', 'u', 'u', 'u', 'y', 'y'};
    private static final String abcFirst = "abcčdefghijklmnopqrřsštuvwxyzž0123456789";
    private static final String abcSecond = "aáäbcčdďeéěëfghiíjklmnňoóöpqrřsšßtťuúůüvwxyýÿzž0123456789";
    private static final int chF = 9, chS = 15; // pozice pismena ch v 1. a 2. abecede

    private static final CzechComparator instance = new CzechComparator();

    private CzechComparator() {
    }

    public static final CzechComparator getInstance() {
        return instance;
    }

    public int compare(String str1, String str2) {
        int res = comp(str1, str2, abcFirst, chF);
        return (res == 0) ? comp(str1, str2, abcSecond, chS) : res;
    }

    private int comp(String str1, String str2, String abc, int ch) {
        int len1 = str1.length();
        int len2 = str2.length();
        for (int i = 0; i < len1; i++) {
            if (i >= len2) break;

            char a = Character.toLowerCase(str1.charAt(i));
            char b = Character.toLowerCase(str2.charAt(i));
            // ch v 1. stringu?
            if ((a == 'c') && (i < (len1 - 1)) && (Character.toLowerCase(str1.charAt(i + 1)) == 'h')) {
                if (b == 'c') {
                    if ((i < (len2 - 1)) && (Character.toLowerCase(str2.charAt(i + 1)) == 'h')) {
                        continue;
                    }
                    return 1;
                }
                int pos2 = abc.indexOf(b);
                pos2 = (pos2 < 0) ? abc.indexOf(toChar(b)) : pos2; // konverze pri prvnim pruchodu
                return (pos2 < ch) ? 1 : -1;
            }
            // ch v 2. stringu?
            if ((b == 'c') && (i < (len2 - 1)) && (Character.toLowerCase(str2.charAt(i + 1)) == 'h')) {
                int pos1 = abc.indexOf(a);
                pos1 = (pos1 < 0) ? abc.indexOf(toChar(a)) : pos1; // konverze pri prvnim pruchodu
                return (pos1 < ch) ? -1 : 1;
            }

            int pos1 = abc.indexOf(a);
            int pos2 = abc.indexOf(b);
            pos1 = (pos1 < 0) ? abc.indexOf(toChar(a)) : pos1; // konverze pri prvnim pruchodu
            pos2 = (pos2 < 0) ? abc.indexOf(toChar(b)) : pos2; // konverze pri prvnim pruchodu
            if (pos1 != pos2)
                return (pos1 < pos2) ? -1 : 1;
        }
        return (len1 < len2) ? -1 : ((len1 > len2) ? 1 : 0);
    }

    private char toChar(char c) {
        for (int i = 0; i < translateTableFrom.length; i++) {
            if (c == translateTableFrom[i])
                return translateTableTo[i];
        }
//    System.out.println("chyba: " + c);
        return c;
    }

//    public static void main(String[] args) {
//        List<String> list = new ArrayList<String>();
//        list.add("a");
//        list.add("A");
//        list.add("á");
//        list.add("Á");
//        list.add("b");
//        list.add("B");
//        list.add("c");
//        list.add("Č");
//        list.add("d");
//        list.add("ď");
//        list.add("ď");
//        list.add("Ď");
//        list.add("e");
//        list.add("E");
//        list.add("é");
//        list.add("É");
//        list.add("ě");
//        list.add("Ě");
//        list.add("f");
//        list.add("F");
//        list.add("g");
//        list.add("G");
//        list.add("h");
//        list.add("H");
//        list.add("ch");
//        list.add("CH");
//        list.add("i");
//        list.add("I");
//        list.add("í");
//        list.add("Í");
//        list.add("j");
//        list.add("J");
//        list.add("k");
//        list.add("K");
//        list.add("l");
//        list.add("L");
//        list.add("m");
//        list.add("M");
//        list.add("n");
//        list.add("N");
//        list.add("ň");
//        list.add("Ň");
//        list.add("o");
//        list.add("O");
//        list.add("ó");
//        list.add("Ó");
//        list.add("p");
//        list.add("P");
//        list.add("q");
//        list.add("Q");
//        list.add("r");
//        list.add("R");
//        list.add("ř");
//        list.add("Ř");
//        list.add("s");
//        list.add("S");
//        list.add("š");
//        list.add("Š");
//        list.add("t");
//        list.add("T");
//        list.add("ť");
//        list.add("Ť");
//        list.add("u");
//        list.add("U");
//        list.add("ú");
//        list.add("Ú");
//        list.add("ů");
//        list.add("v");
//        list.add("V");
//        list.add("w");
//        list.add("W");
//        list.add("x");
//        list.add("X");
//        list.add("y");
//        list.add("Ý");
//        list.add("z");
//        list.add("Z");
//        list.add("ž");
//        list.add("Ž");
//
//        int i = 0;
//        for (String s : list) {
//            System.out.print(s);
//        }
//        System.out.println();
//        System.out.println();
//
//        Collections.shuffle(list);
//        i = 0;
//        for (String s : list) {
//            System.out.print(s);
//        }
//        System.out.println();
//        System.out.println();
//
//        CzechComparator comparator = CzechComparator.getInstance();
//        Collections.sort(list, comparator);
//
//        i = 0;
//        for (String s : list) {
//            System.out.print(s);
//        }
//        System.out.println();
//        System.out.println();
//    }
}