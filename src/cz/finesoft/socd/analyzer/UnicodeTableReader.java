package cz.finesoft.socd.analyzer;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Reads STANDARD UNICODE TABLE (UnicodeData.txt -- see http://www.unicode.org).
 * This is
 *
 * @author Lukas Zapletal [lzap at root.cz]
 */
public class UnicodeTableReader {
    // private members
    private BufferedReader reader;

    // the record (one line in the file)
    char codePoint;         // the characters UTF-16 code value
    String name;            // the ASCII name
    byte generalCategory;   // general category, available via Characte.getType()
    int combiningClass;     // not used (in Character)
    byte bidiCategory;      // available via Character.getBidiType()
    String decomposition;   // not used (in Character)
    private char relative;  // the most important character from decomposition field (it is not a part of standard)
    int decimalValue;       // decimal digit value
    int digitValue;         // not all digits are decimal
    String numericValue;    // numeric value if digit or non-digit
    boolean mirrored;       // others are minor fields
    String oldName;
    String comment;
    char upperMap;          // this is used when changing case of the character
    char lowerMap;
    char titleMap;

    // this is the number of fields in one line of the UnicodeData.txt file
    // each field is separated by a semicolon (a token)
    static final int REQUIRED_FIELDS = 15;

    /**
     * General category types
     * To preserve compatibility, these values cannot be changed
     */
    public static final byte UNASSIGNED = 0, // Cn normative
    UPPERCASE_LETTER = 1, // Lu normative
    LOWERCASE_LETTER = 2, // Ll normative
    TITLECASE_LETTER = 3, // Lt normative
    MODIFIER_LETTER = 4, // Lm normative
    OTHER_LETTER = 5, // Lo normative
    NON_SPACING_MARK = 6, // Mn informative
    ENCLOSING_MARK = 7, // Me informative
    COMBINING_SPACING_MARK = 8, // Mc normative
    DECIMAL_DIGIT_NUMBER = 9, // Nd normative
    LETTER_NUMBER = 10, // Nl normative
    OTHER_NUMBER = 11, // No normative
    SPACE_SEPARATOR = 12, // Zs normative
    LINE_SEPARATOR = 13, // Zl normative
    PARAGRAPH_SEPARATOR = 14, // Zp normative
    CONTROL = 15, // Cc normative
    FORMAT = 16, // Cf normative
    // 17 is unused for no apparent reason,
    // but must preserve forward compatibility
    PRIVATE_USE = 18, // Co normative
    SURROGATE = 19, // Cs normative
    DASH_PUNCTUATION = 20, // Pd informative
    START_PUNCTUATION = 21, // Ps informative
    END_PUNCTUATION = 22, // Pe informative
    CONNECTOR_PUNCTUATION = 23, // Pc informative
    OTHER_PUNCTUATION = 24, // Po informative
    MATH_SYMBOL = 25, // Sm informative
    CURRENCY_SYMBOL = 26, // Sc informative
    MODIFIER_SYMBOL = 27, // Sk informative
    OTHER_SYMBOL = 28, // So informative
    INITIAL_QUOTE_PUNCTUATION = 29, // Pi informative
    FINAL_QUOTE_PUNCTUATION = 30, // Pf informative

    // this value is only used in the character generation tool
    // it can change to accommodate the addition of new categories.
    GENERAL_CATEGORY_COUNT = 31; // sentinel value

    static final byte SHORT = 0, LONG = 1;
    // general category type strings
    // NOTE: The order of this category array is dependent on the assignment of
    // category constants above. We want to access this array using constants above.
    // [][SHORT] is the SHORT name, [][LONG] is the LONG name
    static final String[][] generalCategoryList = {{"Cn", "UNASSIGNED"}, {"Lu", "UPPERCASE_LETTER"}, {"Ll", "LOWERCASE_LETTER"}, {"Lt", "TITLECASE_LETTER"}, {"Lm", "MODIFIER_LETTER"}, {"Lo", "OTHER_LETTER"}, {"Mn", "NON_SPACING_MARK"}, {"Me", "ENCLOSING_MARK"}, {"Mc", "COMBINING_SPACING_MARK"}, {"Nd", "DECIMAL_DIGIT_NUMBER"}, {"Nl", "LETTER_NUMBER"}, {"No", "OTHER_NUMBER"}, {"Zs", "SPACE_SEPARATOR"}, {"Zl", "LINE_SEPARATOR"}, {"Zp", "PARAGRAPH_SEPARATOR"}, {"Cc", "CONTROL"}, {"Cf", "FORMAT"}, {"xx", "unused"}, {"Co", "PRIVATE_USE"}, {"Cs", "SURROGATE"}, {"Pd", "DASH_PUNCTUATION"}, {"Ps", "START_PUNCTUATION"}, {"Pe", "END_PUNCTUATION"}, {"Pc", "CONNECTOR_PUNCTUATION"}, {"Po", "OTHER_PUNCTUATION"}, {"Sm", "MATH_SYMBOL"}, {"Sc", "CURRENCY_SYMBOL"}, {"Sk", "MODIFIER_SYMBOL"}, {"So", "OTHER_SYMBOL"}, {"Pi", "INITIAL_QUOTE_PUNCTUATION"}, {"Pf", "FINAL_QUOTE_PUNCTUATION"}};

    /**
     * Bidirectional categories
     */
    public static final byte
            // Strong category
            DIRECTIONALITY_LEFT_TO_RIGHT = 0, // L
    DIRECTIONALITY_RIGHT_TO_LEFT = 1, // R
    DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = 2, // AL
    // Weak category
    DIRECTIONALITY_EUROPEAN_NUMBER = 3, // EN
    DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = 4, // ES
    DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = 5, // ET
    DIRECTIONALITY_ARABIC_NUMBER = 6, // AN
    DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = 7, // CS
    DIRECTIONALITY_NONSPACING_MARK = 8, // NSM
    DIRECTIONALITY_BOUNDARY_NEUTRAL = 9, // BN
    // Neutral category
    DIRECTIONALITY_PARAGRAPH_SEPARATOR = 10, // B
    DIRECTIONALITY_SEGMENT_SEPARATOR = 11, // S
    DIRECTIONALITY_WHITESPACE = 12, // WS
    DIRECTIONALITY_OTHER_NEUTRALS = 13, // ON

    DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 14, // LRE
    DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 15, // LRO
    DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 16, // RLE
    DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 17, // RLO
    DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 18, // PDF

    DIRECTIONALITY_CATEGORY_COUNT = 19; // sentinel value

    // If changes are made to the above bidi category assignments, this
    // list of bidi category names must be changed to keep their order in synch.
    // Access this list using the bidi category constants above.
    static final String[][] bidiCategoryList = {{"L", "DIRECTIONALITY_LEFT_TO_RIGHT"}, {"R", "DIRECTIONALITY_RIGHT_TO_LEFT"}, {"AL", "DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC"}, {"EN", "DIRECTIONALITY_EUROPEAN_NUMBER"}, {"ES", "DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR"}, {"ET", "DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR"}, {"AN", "DIRECTIONALITY_ARABIC_NUMBER"}, {"CS", "DIRECTIONALITY_COMMON_NUMBER_SEPARATOR"}, {"NSM", "DIRECTIONALITY_NONSPACING_MARK"}, {"BN", "DIRECTIONALITY_BOUNDARY_NEUTRAL"}, {"B", "DIRECTIONALITY_PARAGRAPH_SEPARATOR"}, {"S", "DIRECTIONALITY_SEGMENT_SEPARATOR"}, {"WS", "DIRECTIONALITY_WHITESPACE"}, {"ON", "DIRECTIONALITY_OTHER_NEUTRALS"}, {"LRE", "DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING"}, {"LRO", "DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE"}, {"RLE", "DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING"}, {"RLO", "DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE"}, {"PDF", "DIRECTIONALITY_POP_DIRECTIONAL_FORMAT"},

    };

    // Unicode specification lines have fields in this order.
    static final byte FIELD_VALUE = 0,
    FIELD_NAME = 1,
    FIELD_CATEGORY = 2,
    FIELD_CLASS = 3,
    FIELD_BIDI = 4,
    FIELD_DECOMPOSITION = 5,
    FIELD_DECIMAL = 6,
    FIELD_DIGIT = 7,
    FIELD_NUMERIC = 8,
    FIELD_MIRRORED = 9,
    FIELD_OLDNAME = 10,
    FIELD_COMMENT = 11,
    FIELD_UPPERCASE = 12,
    FIELD_LOWERCASE = 13,
    FIELD_TITLECASE = 14;

    // constructors
    public UnicodeTableReader(InputStream is) {
        this(new InputStreamReader(is));
    }

    public UnicodeTableReader(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    // accessors
    void setCodePoint(char value) {
        codePoint = value;
    }

    /**
     * Return the code point in this Unicode specification
     * @return the char code point representing by the specification
     */
    public char getCodePoint() {
        return codePoint;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void setGeneralCategory(byte category) {
        generalCategory = category;
    }

    public byte getGeneralCategory() {
        return generalCategory;
    }

    void setBidiCategory(byte category) {
        bidiCategory = category;
    }

    public byte getBidiCategory() {
        return bidiCategory;
    }

    void setCombiningClass(int combiningClass) {
        this.combiningClass = combiningClass;
    }

    public int getCombiningClass() {
        return combiningClass;
    }

    void setDecomposition(String decomposition) {
        this.decomposition = decomposition;
    }

    public String getDecomposition() {
        return decomposition;
    }

    void setDecimalValue(int value) {
        decimalValue = value;
    }

    public int getDecimalValue() {
        return decimalValue;
    }

    public boolean isDecimalValue() {
        return decimalValue!=-1;
    }

    void setDigitValue(int value) {
        digitValue = value;
    }

    public int getDigitValue() {
        return digitValue;
    }

    public boolean isDigitValue() {
        return digitValue!=-1;
    }

    void setNumericValue(String value) {
        numericValue = value;
    }

    public String getNumericValue() {
        return numericValue;
    }

    public boolean isNumericValue() {
        return numericValue.length()>0;
    }

    void setMirrored(boolean value) {
        mirrored = value;
    }

    public boolean isMirrored() {
        return mirrored;
    }

    void setOldName(String name) {
        oldName = name;
    }

    public String getOldName() {
        return oldName;
    }

    void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    void setUpperMap(char ch) {
        upperMap = ch;
    };

    public char getUpperMap() {
        return upperMap;
    }

    public boolean hasUpperMap() {
        return upperMap!='\uffff';
    }

    void setLowerMap(char ch) {
        lowerMap = ch;
    }

    public char getLowerMap() {
        return lowerMap;
    }

    public boolean hasLowerMap() {
        return lowerMap!='\uffff';
    }

    void setTitleMap(char ch) {
        titleMap = ch;
    }

    public char getTitleMap() {
        return titleMap;
    }

    public boolean hasTitleMap() {
        return titleMap!='\uffff';
    }

    // methods
    public String toString() {
        StringBuffer result = new StringBuffer(hex4(codePoint));
        if ( getUpperMap()!='\uffff' ) {
            result.append(", upper=").append(hex4(upperMap));
        }
        if ( getLowerMap()!='\uffff' ) {
            result.append(", lower=").append(hex4(lowerMap));
        }
        if ( getTitleMap()!='\uffff' ) {
            result.append(", title=").append(hex4(titleMap));
        }
        return result.toString();
    }

    static String hex4(long n) {
        String q = Long.toHexString(n & 0xFFFF).toUpperCase();
        return "0000".substring(Math.min(4, q.length()))+q;
    }


    public void parse(String s) throws Exception {
        String[] tokens = new String[REQUIRED_FIELDS];

        int x = 0, tokenStart = 0, tokenEnd = 0;
        for ( x = 0; x<REQUIRED_FIELDS-1; x++ ) {
            tokenEnd = s.indexOf(';', tokenStart);
            tokens[x] = s.substring(tokenStart, tokenEnd);
            tokenStart = tokenEnd+1;
        }
        tokens[x] = s.substring(tokenStart);
        setCodePoint(parseCodePoint(tokens[FIELD_VALUE]));
        setName(parseName(tokens[FIELD_NAME]));
        setGeneralCategory(parseGeneralCategory(tokens[FIELD_CATEGORY]));
        setBidiCategory(parseBidiCategory(tokens[FIELD_BIDI]));
        setCombiningClass(parseCombiningClass(tokens[FIELD_CLASS]));
        setDecomposition(parseDecomposition(tokens[FIELD_DECOMPOSITION]));
        setRelative(parseRelative(tokens[FIELD_DECOMPOSITION]));
        setDecimalValue(parseDecimalValue(tokens[FIELD_DECIMAL]));
        setDigitValue(parseDigitValue(tokens[FIELD_DIGIT]));
        setNumericValue(parseNumericValue(tokens[FIELD_NUMERIC]));
        setMirrored(parseMirrored(tokens[FIELD_MIRRORED]));
        setOldName(parseOldName(tokens[FIELD_OLDNAME]));
        setComment(parseComment(tokens[FIELD_COMMENT]));
        setUpperMap(parseUpperMap(tokens[FIELD_UPPERCASE]));
        setLowerMap(parseLowerMap(tokens[FIELD_LOWERCASE]));
        setTitleMap(parseTitleMap(tokens[FIELD_TITLECASE]));
    }

    /**
     * Parse the codePoint attribute for a Unicode character.  If the parse succeeds,
     * the codePoint field of this UnicodeSpec object is updated and false is returned.
     *
     * The codePoint attribute should be a four-digit hexadecimal integer.
     *
     * @param s   the codePoint attribute extracted from a line of the Unicode data file
     * @return 	code point if successful
     * @exception java.lang.NumberFormatException if unable to parse argument
     */
    public static char parseCodePoint(String s) throws NumberFormatException {
        return (char) Integer.parseInt(s, 16);
    }

    public static String parseName(String s) throws Exception {
        if ( s==null ) throw new Exception("Cannot parse name.");
        return s;
    }

    public static byte parseGeneralCategory(String s) throws Exception {
        byte category = GENERAL_CATEGORY_COUNT;

        for ( byte x = 0; x<generalCategoryList.length; x++ ) {
            if ( s.equals(generalCategoryList[x][SHORT]) ) {
                category = x;
                break;
            }
        }
        if ( category>=GENERAL_CATEGORY_COUNT ) {
            throw new Exception("Could not parse general category.");
        }
        return category;
    }

    public static byte parseBidiCategory(String s) throws Exception {
        byte category = DIRECTIONALITY_CATEGORY_COUNT;

        for ( byte x = 0; x<bidiCategoryList.length; x++ ) {
            if ( s.equals(bidiCategoryList[x][SHORT]) ) {
                category = x;
                break;
            }
        }
        if ( category>=DIRECTIONALITY_CATEGORY_COUNT ) {
            throw new Exception("Could not parse bidi category.");
        }
        return category;
    }


    /**
     * Parse the combining attribute for a Unicode character.  If there is a combining
     * attribute and the parse succeeds, then the hasCombining field is set to true,
     * the combining field of this UnicodeSpec object is updated, and false is returned.
     * If the combining attribute is an empty string, the parse succeeds but the
     * hasCombining field is set to false. (and false is returned).
     *
     * The combining attribute, if any, should be a nonnegative decimal integer.
     *
     * @param s   the combining attribute extracted from a line of the Unicode data file
     * @return 	the combining class value if any, -1 if property not defined
     * @exception java.lang.Exception if can't parse the combining class
     */

    public static int parseCombiningClass(String s) throws Exception {
        int combining = -1;
        if ( s.length()>0 ) {
            combining = Integer.parseInt(s, 10);
        }
        return combining;
    }

    /**
     * Parse the decomposition attribute for a Unicode character.  If the parse succeeds,
     * the decomposition field of this UnicodeSpec object is updated and false is returned.
     *
     * The decomposition attribute is complicated; for now, it is treated as a string.
     *
     * @param s   the decomposition attribute extracted from a line of the Unicode data file
     * @return 	true if the parse failed; otherwise false
     */

    public static String parseDecomposition(String s) throws Exception {
        if ( s==null ) throw new Exception("Cannot parse decomposition.");
        return s;
    }

    /**
     * Returns the most relative character (e.g. in 'A' with dot above is 'A')
     */

    public static char parseRelative(String s) throws Exception {
        if ( s==null ) throw new Exception("Cannot parse decomposition.");
        char result = '\uFFFF';
        StringTokenizer st = new StringTokenizer(s, " ");
        while ( st.hasMoreElements() ) {
            try {
                result = (char) Integer.parseInt(st.nextToken(), 16);
            } catch (NumberFormatException e) {
                // we skip all non parseable tokens
            }
            if ( result!='\uFFFF' )
                break;
        }
        return result;
    }

    /**
     * Parse the decimal value attribute for a Unicode character.  If there is a decimal value
     * attribute and the parse succeeds, then the hasDecimalValue field is set to true,
     * the decimalValue field of this UnicodeSpec object is updated, and false is returned.
     * If the decimal value attribute is an empty string, the parse succeeds but the
     * hasDecimalValue field is set to false. (and false is returned).
     *
     * The decimal value attribute, if any, should be a nonnegative decimal integer.
     *
     * @param s   the decimal value attribute extracted from a line of the Unicode data file
     * @return 	the decimal value as an int, -1 if no decimal value defined
     * @exception java.lang.NumberFormatException if the parse fails
     */
    public static int parseDecimalValue(String s) throws NumberFormatException {
        int value = -1;

        if ( s.length()>0 ) {
            value = Integer.parseInt(s, 10);
        }
        return value;
    }

    /**
     * Parse the digit value attribute for a Unicode character.  If there is a digit value
     * attribute and the parse succeeds, then the hasDigitValue field is set to true,
     * the digitValue field of this UnicodeSpec object is updated, and false is returned.
     * If the digit value attribute is an empty string, the parse succeeds but the
     * hasDigitValue field is set to false. (and false is returned).
     *
     * The digit value attribute, if any, should be a nonnegative decimal integer.
     *
     * @param s   the digit value attribute extracted from a line of the Unicode data file
     * @return 	the digit value as an non-negative int, or -1 if no digit property defined
     * @exception java.lang.NumberFormatException if the parse fails
     */
    public static int parseDigitValue(String s) throws NumberFormatException {
        int value = -1;

        if ( s.length()>0 ) {
            value = Integer.parseInt(s, 10);
        }
        return value;
    }

    public static String parseNumericValue(String s) throws Exception {
        if ( s==null ) throw new Exception("Cannot parse numeric value.");
        return s;
    }

    public static String parseComment(String s) throws Exception {
        if ( s==null ) throw new Exception("Cannot parse comment.");
        return s;
    }

    public static boolean parseMirrored(String s) throws Exception {
        boolean mirrored;
        if ( s.length()==1 ) {
            if ( s.charAt(0)=='Y' ) {
                mirrored = true;
            } else if ( s.charAt(0)=='N' ) {
                mirrored = false;
            } else {
                throw new Exception("Cannot parse mirrored property.");
            }
        } else {
            throw new Exception("Cannot parse mirrored property.");
        }
        return mirrored;
    }

    public static String parseOldName(String s) throws Exception {
        if ( s==null ) throw new Exception("Cannot parse old name");
        return s;
    }

    /**
     * Parse the uppercase mapping attribute for a Unicode character.  If there is a uppercase
     * mapping attribute and the parse succeeds, then the hasUpperMap field is set to true,
     * the upperMap field of this UnicodeSpec object is updated, and false is returned.
     * If the uppercase mapping attribute is an empty string, the parse succeeds but the
     * hasUpperMap field is set to false. (and false is returned).
     *
     * The uppercase mapping attribute should be a four-digit hexadecimal integer.
     *
     * @param s   the uppercase mapping attribute extracted from a line of the Unicode data file
     * @return 	uppercase char if defined, \uffff otherwise
     * @exception java.lang.NumberFormatException if parse fails
     */
    public static char parseUpperMap(String s) throws NumberFormatException {
        char upperCase = '\uFFFF';

        if ( s.length()==4 ) {
            upperCase = (char) Integer.parseInt(s, 16);
        } else if ( s.length()!=0 ) {
            throw new NumberFormatException();
        }
        return upperCase;
    }

    /**
     * Parse the lowercase mapping attribute for a Unicode character.  If there is a lowercase
     * mapping attribute and the parse succeeds, then the hasLowerMap field is set to true,
     * the lowerMap field of this UnicodeSpec object is updated, and false is returned.
     * If the lowercase mapping attribute is an empty string, the parse succeeds but the
     * hasLowerMap field is set to false. (and false is returned).
     *
     * The lowercase mapping attribute should be a four-digit hexadecimal integer.
     *
     * @param s   the lowercase mapping attribute extracted from a line of the Unicode data file
     * @return 	lowercase char mapping if defined, \uFFFF otherwise
     * @exception java.lang.NumberFormatException if parse fails
     */
    public static char parseLowerMap(String s) throws NumberFormatException {
        char lowerCase = '\uFFFF';

        if ( s.length()==4 ) {
            lowerCase = (char) Integer.parseInt(s, 16);
        } else if ( s.length()!=0 ) {
            throw new NumberFormatException();
        }
        return lowerCase;
    }

    /**
     * Parse the titlecase mapping attribute for a Unicode character.  If there is a titlecase
     * mapping attribute and the parse succeeds, then the hasTitleMap field is set to true,
     * the titleMap field of this UnicodeSpec object is updated, and false is returned.
     * If the titlecase mapping attribute is an empty string, the parse succeeds but the
     * hasTitleMap field is set to false. (and false is returned).
     *
     * The titlecase mapping attribute should be a four-digit hexadecimal integer.
     *
     * @param s   the titlecase mapping attribute extracted from a line of the Unicode data file
     * @return 	title case char mapping if defined, \uFFFF otherwise
     * @exception java.lang.NumberFormatException if parse fails
     */
    public static char parseTitleMap(String s) throws NumberFormatException {
        char titleCase = '\uFFFF';

        if ( s.length()==4 ) {
            titleCase = (char) Integer.parseInt(s, 16);
        } else if ( s.length()!=0 ) {
            throw new NumberFormatException();
        }
        return titleCase;
    }

    public void reset() throws IOException {
        reader.reset();
    }

    public boolean next() throws Exception {
        String record = reader.readLine();

        if ( record==null )
            return false;

        parse(record);

        return true;
    }

    public char getRelative() {
        return relative;
    }

    public void setRelative(char relative) {
        this.relative = relative;
    }
}
