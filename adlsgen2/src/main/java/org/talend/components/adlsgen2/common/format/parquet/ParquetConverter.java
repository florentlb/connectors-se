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

import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericRecord;
import org.talend.components.adlsgen2.common.converter.RecordConverter;
import org.talend.components.adlsgen2.common.format.avro.AvroConverter;
import org.talend.sdk.component.api.record.Record.Builder;
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
        log.warn("[ParquetConverter]");
    }

    public org.talend.sdk.component.api.record.Record toRcord(GenericRecord value) {
        log.warn("[toRecord] record: {}\nschema: {}", value, value.getSchema());
        log.warn("[toRecord] record: {}\nfields: {}", value, value.getSchema().getFields());
        Builder record = recordBuilderFactory.newRecordBuilder();
        for (Field f : value.getSchema().getFields()) {
            String fieldName = f.name();
            String fieldValue = String.valueOf(value.get(fieldName));
            log.warn("[toRecord] K:{} V:{} ({}).", fieldName, fieldValue, f.schema().getType());
            switch (f.schema().getType()) {
            case RECORD:
                break;
            case ENUM:
                break;
            case ARRAY:
                log.warn("[toRecord] ARRAY {}", value.getSchema().getTypes());
                log.warn("[toRecord] ARRAY {}", value.getSchema().getValueType());
                log.warn("[toRecord] ARRAY {}", value.getSchema().getFields());
                break;
            case MAP:
                break;
            case UNION:
                break;
            case FIXED:
                break;
            case STRING:
                record.withString(fieldName, fieldValue);
                break;
            case BYTES:
                record.withBytes(fieldName, fieldValue.getBytes());
                break;
            case INT:
                record.withInt(fieldName, Integer.parseInt(fieldValue));
                break;
            case LONG:
                record.withLong(fieldName, Long.parseLong(fieldValue));
                break;
            case FLOAT:
                record.withFloat(f.name(), Float.parseFloat(fieldValue));
                break;
            case DOUBLE:
                record.withDouble(f.name(), Double.parseDouble(fieldValue));
                break;
            case BOOLEAN:
                record.withBoolean(f.name(), Boolean.parseBoolean(fieldValue));
                break;
            case NULL:
                record.withString(f.name(), fieldValue);
                break;
            }
        }
        return record.build();
    }

    @Override
    public GenericRecord fromRecord(org.talend.sdk.component.api.record.Record record) {

        throw new UnsupportedOperationException("#fromRecord()");
    }
}
