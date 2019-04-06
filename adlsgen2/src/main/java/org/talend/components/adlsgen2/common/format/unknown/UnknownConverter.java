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
package org.talend.components.adlsgen2.common.format.unknown;

import org.talend.components.adlsgen2.common.converter.RecordConverter;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

public class UnknownConverter implements RecordConverter<String> {

    @Service
    RecordBuilderFactory recordBuilder;

    private static final UnknownConverter INSTANCE = new UnknownConverter();

    private UnknownConverter() {
    }

    public static UnknownConverter of() {
        return INSTANCE;
    }

    @Override
    public Record toRecord(String value) {
        return recordBuilder.newRecordBuilder().withString("content", value).build();
    }

    @Override
    public String fromRecord(Record record) {
        throw new UnsupportedOperationException("#fromRecord()");
    }
}
