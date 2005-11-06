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
package cz.finesoft.socd.analyzer;

import java.io.FilterReader;
import java.io.Reader;
import java.io.IOException;

/**
 * This Reader acts as filter: it takes Reader
 * as an argument and it tries to strip accents
 * from its data.
 */
public class RemoveDiacriticsReader extends FilterReader {
    static final DiacriticRemover remover = DiacriticRemover.getInstance();

    /**
     * Constructs new RemoveDiacriticsReader.
     * @param in
     */
    public RemoveDiacriticsReader(Reader in) {
        super(in);
    }

    /**
     * Read single character. If neccessary, it converts it to version without diacritics.
     * @return The character read as integer in the range 0 to 65535, or -1 if the end of the stream has been reached
     * @throws IOException If an I/O error occurs
     */
    public int read() throws IOException {
        synchronized (lock) {
            int c = super.read();
            if ( c==-1)
                return -1;
            return remover.removeDiacritics((char)c);
        }
    }

    /**
     * Read characters into a portion of an array. If neccessary, it converts it to version without diacritics.
     * @param cbuf Destination buffer
     * @param off Offset at which to start storing characters
     * @param len Maximum number of characters to read
     * @return The number of characters read, or -1 if the end of the stream has been reached
     * @throws IOException If an I/O error occurs
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            if ( len<0 )
                throw new IndexOutOfBoundsException("Length = "+len);
            if ( off<0 || off>cbuf.length )
                throw new IndexOutOfBoundsException("Offset = "+len);
            int copied = 0, c;
            while ( len>0 ) {
                c = super.read();
                if ( c==-1 )
                    return ( copied==0 )? -1 : copied;

                cbuf[off+copied] = remover.removeDiacritics((char) c);
                copied++; len--;
            }
            return copied;
        }
    }

    public String toString() {
        return this.getClass().getName();
    }
}
