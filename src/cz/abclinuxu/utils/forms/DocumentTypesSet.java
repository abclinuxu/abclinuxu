/*
 *  Copyright (C) 2008 Karel Piwko
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
package cz.abclinuxu.utils.forms;

import cz.abclinuxu.data.view.DocumentType;
import cz.abclinuxu.data.view.DocumentTypes;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.List;
import java.util.Map;

/**
 * @author kapy
 */
public class DocumentTypesSet extends MapMultipleChoice<String, DocumentTypesSet.SelectedDocumentType> {
    /**
     * All possible values of document types
     */
    protected Map<String, DocumentType> allTypes;

    /**
     * Constructs DocumentTypeSet using values retrieved from parameter in
     * request/session.
     * @param param Object, from which values are retrieved
     * @param noneIsAll Flag, if set to <code>true</code> and nothing was selected,
     * it is considered as everything was selected
     */
    public DocumentTypesSet(Object param, boolean noneIsAll, DocumentTypes.Types type) {
        this.noneIsAll = noneIsAll;
        List<String> checked = (List<String>) Tools.asList(param);
        allTypes = DocumentTypes.getInstance(type).get();

        for (DocumentType dt : allTypes.values()) {
            String key = dt.getKey();
            SelectedDocumentType sdt;
            // checkbox was checked
            if (checked.contains(key)) {
                sdt = new SelectedDocumentType(dt, true);
                selected++;
            }
            // checkbox was not checked
            else {
                sdt = new SelectedDocumentType(dt, false);
            }
            choices.put(key, sdt);
        }
    }

    public boolean isEverythingSelected() {
        return selected == allTypes.size();
    }

    /**
     * Flag of selection added to DocumentType
     */
    public static class SelectedDocumentType extends DocumentType implements Selectable {

        /**
         * selection flag
         */
        private boolean set;

        /**
         * Creates object of DocumentType which allows to store document flag inside
         * @param type Template of document type
         * @param set Initial value of selection flag
         */
        public SelectedDocumentType(DocumentType type, boolean set) {
            super(type.getKey(), type.getLabel(), type.getType(), type.getSubtype());
            this.set = set;
        }

        /**
         * Returs selection flag for given object
         * @return <code>true</code> if item is selected, <code>false</code> elsewhere
         */
        public boolean isSet() {
            return set;
        }

        /**
         * Sets value of selection flag
         * @param set New value of selection flag
         */
        public void setSet(boolean set) {
            this.set = set;
        }

        public String toString() {
            return getKey() + ((set) ? " (selected)" : " (not selected)");
        }
    }
}
