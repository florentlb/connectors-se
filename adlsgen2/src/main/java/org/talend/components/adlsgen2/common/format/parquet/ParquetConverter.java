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
package org.talend.components.adlsgen2.common.format.parquet;

import org.apache.avro.generic.GenericRecord;
import org.talend.components.adlsgen2.common.converter.RecordConverter;
import org.talend.components.adlsgen2.common.format.avro.AvroConverter;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParquetConverter extends AvroConverter implements RecordConverter<GenericRecord> {

    @Service
    public static RecordBuilderFactory recordBuilderFactory;

    public static ParquetConverter of() {
        return new ParquetConverter();
    }

    private ParquetConverter() {
        super();
    }

    @Override
    public GenericRecord fromRecord(org.talend.sdk.component.api.record.Record record) {
        throw new UnsupportedOperationException("#fromRecord()");
    }
}
