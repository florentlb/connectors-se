// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.adlsgen2.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class CSVFormat {

    @Getter
    @AllArgsConstructor
    public enum RecordDelimiter {
        LF("\n"),
        CR("\r"),
        CRLF("\r\n");

        private final String delimiter;

        public char getDelimiterChar() {
            return delimiter.charAt(0);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum FieldDelimiter {
        SEMICOLON(";"),
        COMMA(","),
        TABULATION("\t"),
        SPACE(" ");

        private final String delimiter;

        public char getDelimiterChar() {
            return delimiter.charAt(0);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Encoding {
        UTF8("UTF-8"),
        ISO_8859_15("ISO-8859-15"),
        OTHER("Other");

        private final String encoding;
    }

}
