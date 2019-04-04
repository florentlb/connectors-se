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
package org.talend.components.adlsgen2.commons.formats.csv;

import org.talend.components.adlsgen2.commons.converter.RecordConverter;
import org.talend.sdk.component.api.record.Record;

public class CsvConverter implements RecordConverter<String> {

    private static final CsvConverter INSTANCE = new CsvConverter();

    private CsvConverter() {
    }

    public static CsvConverter of() {
        return INSTANCE;
    }

    @Override
    public Record toRecord(String value) {
        throw new UnsupportedOperationException("#toRecord()");
    }

    @Override
    public String fromRecord(Record record) {
        throw new UnsupportedOperationException("#fromRecord()");
    }
}
