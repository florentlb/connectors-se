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
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.record.Schema.Type;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import static java.util.stream.Collectors.joining;

public class UnknownConverter implements RecordConverter<String> {

    public static final String FIELD_CONTENT = "content";

    @Service
    public static RecordBuilderFactory recordBuilderFactory;

    private UnknownConverter() {
    }

    public static UnknownConverter of() {
        return new UnknownConverter();
    }

    @Override
    public Schema inferSchema(String record) {
        return recordBuilderFactory.newSchemaBuilder(Type.RECORD)
                .withEntry(recordBuilderFactory.newEntryBuilder().withName(FIELD_CONTENT).withType(Type.STRING).build()).build();
    }

    @Override
    public Record toRecord(String value) {
        return recordBuilderFactory.newRecordBuilder().withString(FIELD_CONTENT, value).build();
    }

    @Override
    public String fromRecord(Record record) {
        return record.getSchema().getEntries().stream()
                .map(e -> String.format("%s:{%s}", e.getName(), String.valueOf(record.get(Object.class, e.getName()))))
                .collect(joining(","));
    }
}
