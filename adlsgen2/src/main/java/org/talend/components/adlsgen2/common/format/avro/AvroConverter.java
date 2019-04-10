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
package org.talend.components.adlsgen2.common.format.avro;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.talend.components.adlsgen2.common.converter.RecordConverter;
import org.talend.components.adlsgen2.service.I18n;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.record.Schema.Entry;
import org.talend.sdk.component.api.record.Schema.Type;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.toList;
import static org.apache.avro.Schema.Type.ARRAY;

@Slf4j
public class AvroConverter implements RecordConverter<GenericRecord> {

    @Service
    public static RecordBuilderFactory recordBuilderFactory;

    @Service
    public static I18n i18n;

    private Schema schema;

    public static AvroConverter of() {
        return new AvroConverter();
    }

    protected AvroConverter() {
    }

    @Override
    public Schema inferSchema(GenericRecord record) {
        Schema.Builder builder = recordBuilderFactory.newSchemaBuilder(Type.RECORD);
        record.getSchema().getFields().stream().map(this::inferAvroField).forEach(builder::withEntry);
        return builder.build();
    }

    @Override
    public Record toRecord(GenericRecord record) {
        if (schema == null) {
            schema = inferSchema(record);
        }
        return convertAvroToRecord(record, record.getSchema().getFields(), recordBuilderFactory.newRecordBuilder(schema));
    }

    @Override
    public GenericRecord fromRecord(Record record) {
        record.getSchema().getType();
        log.warn("============================="); //
        org.apache.avro.Schema s = inferAvroSchema(record.getSchema());
        GenericRecord avroRecord = new GenericData.Record(s);
        // TODO fill record
                return fillAvroRecord( record, avroRecord);
        //return avroRecord;
    }

    private GenericRecord fillAvroRecord(Record fromRecord, GenericRecord toRecord) {

        for (org.apache.avro.Schema.Field f:  toRecord.getSchema().getFields()){
            String name = f.name();
            switch(f.schema().getType()){
                case RECORD:
                    toRecord.put(name, fromRecord.getRecord(name));
                    break;
                case ENUM:
                    break;
                case ARRAY:
                    break;
                case MAP:
                    break;
                case UNION:
                    break;
                case FIXED:
                    break;
                case STRING:
                    break;
                case BYTES:
                    break;
                case INT:
                    break;
                case LONG:
                    break;
                case FLOAT:
                    break;
                case DOUBLE:
                    break;
                case BOOLEAN:
                    break;
                case NULL:
                    break;
            }


        }


        return toRecord;
    }



    private org.apache.avro.Schema.Type translateToAvroType(Type type) {
        switch (type) {
        case RECORD:
            return org.apache.avro.Schema.Type.RECORD;
        case ARRAY:
            return org.apache.avro.Schema.Type.ARRAY;
        case STRING:
            return org.apache.avro.Schema.Type.STRING;
        case BYTES:
            return org.apache.avro.Schema.Type.BYTES;
        case INT:
            return org.apache.avro.Schema.Type.INT;
        case LONG:
        case DATETIME:
            return org.apache.avro.Schema.Type.LONG;
        case FLOAT:
            return org.apache.avro.Schema.Type.FLOAT;
        case DOUBLE:
            return org.apache.avro.Schema.Type.DOUBLE;
        case BOOLEAN:
            return org.apache.avro.Schema.Type.BOOLEAN;
        }
        throw new IllegalStateException(i18n.undefinedType( type.name()));
    }

    private org.apache.avro.Schema inferAvroSchema(Schema schema) {
        List<org.apache.avro.Schema.Field> fields = new ArrayList<>();
        for (Entry e : schema.getEntries()) {
            String name = e.getName();
            String comment = e.getComment();
            Object defaultValue = e.getDefaultValue();
            Type type = e.getType();
            org.apache.avro.Schema builder;
            switch (type) {
            case RECORD:
                builder = inferAvroSchema(e.getElementSchema());
                break;
            case ARRAY:
                builder = SchemaBuilder.array()
                        .items(org.apache.avro.Schema.create(translateToAvroType(e.getElementSchema().getType())));
                break;
            case STRING:
            case BYTES:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case BOOLEAN:
                builder = org.apache.avro.Schema.create(translateToAvroType(type));
                break;
            case DATETIME:
                builder = org.apache.avro.Schema.create(org.apache.avro.Schema.Type.LONG);
                builder.addProp("talend.field.pattern", ""); //
                builder.addProp("java-class", Date.class.getCanonicalName());
                break;
            default:
                throw new IllegalStateException(i18n.undefinedType( e.getType().name()));
            }
            org.apache.avro.Schema.Field field = new org.apache.avro.Schema.Field(name, builder, comment, defaultValue);
            fields.add(field);
        }

        return org.apache.avro.Schema.createRecord(fields);
    }

    private GenericRecord convertRecordToAvro(Record record) {
        return null;
    }

    private Record convertAvroToRecord(GenericRecord genericRecord, List<org.apache.avro.Schema.Field> fields) {
        return convertAvroToRecord(genericRecord, fields, null);
    }

    private Record convertAvroToRecord(GenericRecord genericRecord, List<org.apache.avro.Schema.Field> fields,
            Record.Builder recordBuilder) {
        if (recordBuilder == null) {
            recordBuilder = recordBuilderFactory.newRecordBuilder();
        }
        for (org.apache.avro.Schema.Field field : fields) {
            Object value = genericRecord.get(field.name());
            if (value == null) {
                continue;
            }
            Entry entry = inferAvroField(field);
            if (field.schema().getType().equals(ARRAY)) {
                processArrayField(field, value, recordBuilder, entry);

            } else {
                processField(field, value, recordBuilder, entry);
            }
        }
        return recordBuilder.build();
    }

    private Entry inferAvroField(org.apache.avro.Schema.Field field) {
        Entry.Builder builder = recordBuilderFactory.newEntryBuilder();
        builder.withName(field.name());
        org.apache.avro.Schema.Type type = field.schema().getType();
        switch (type) {
        case RECORD:
            builder.withType(Type.RECORD);
            builder.withElementSchema(createNestedSchema(field));
            break;
        case ENUM:
        case ARRAY:
            builder.withType(Type.ARRAY);
            builder.withElementSchema(createArraySchema(field));
            break;
        // case MAP:
        // break;
        // case UNION:
        // break;
        // case FIXED:
        // break;
        case STRING:
        case BYTES:
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
        case BOOLEAN:
        case NULL:
            builder.withType(translateToRecordType(type));
            break;
        }
        return builder.build();
    }

    private Schema createNestedSchema(org.apache.avro.Schema.Field field) {
        Schema.Builder builder = recordBuilderFactory.newSchemaBuilder(Type.RECORD);
        field.schema().getFields().stream().map(this::inferAvroField).forEach(builder::withEntry);
        return builder.build();
    }

    private Schema createArraySchema(org.apache.avro.Schema.Field field) {
        Schema.Builder schemaBuilder = recordBuilderFactory.newSchemaBuilder(Type.RECORD);
        Entry.Builder entryBuilder = recordBuilderFactory.newEntryBuilder();
        entryBuilder.withName(field.name());
        entryBuilder.withType(translateToRecordType(field.schema().getElementType().getType()));
        schemaBuilder.withEntry(entryBuilder.build());
        return schemaBuilder.build();
    }

    /**
     *
     */
    private Type translateToRecordType(org.apache.avro.Schema.Type type) {
        switch (type) {
        // TODO manage these cases?
        // case ENUM:
        // break;
        // case MAP:
        // break;
        // case UNION:
        // break;
        // case FIXED:
        // break;
        // case NULL:
        // break;
        case RECORD:
            return Type.RECORD;
        case ARRAY:
            return Type.ARRAY;
        case STRING:
            return Type.STRING;
        case BYTES:
            return Type.BYTES;
        case INT:
            return Type.INT;
        case LONG:
            return Type.LONG;
        case FLOAT:
            return Type.FLOAT;
        case DOUBLE:
            return Type.DOUBLE;
        case BOOLEAN:
            return Type.BOOLEAN;
        default:
            throw new new IllegalStateException(i18n.undefinedType(type.name()));
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void processArrayField(org.apache.avro.Schema.Field field, Object value, Record.Builder recordBuilder, Entry entry) {
        log.warn("[processArrayField] f:{} v:{} e:{}", field, value, entry);
        switch (field.schema().getElementType().getType()) {
        case RECORD:
            recordBuilder.withArray(entry, ((GenericData.Array<GenericRecord>) value).stream()
                    .map(record -> convertAvroToRecord(record, field.schema().getFields())).collect(toList()));
            break;
        case STRING:
            recordBuilder.withArray(entry, (ArrayList<String>) value);
            break;
        case BYTES:
            recordBuilder.withArray(entry,
                    ((GenericData.Array<ByteBuffer>) value).stream().map(ByteBuffer::array).collect(toList()));
            break;
        case INT:
            recordBuilder.withArray(entry, (GenericData.Array<Long>) value);
            break;
        case FLOAT:
            recordBuilder.withArray(entry, (GenericData.Array<Double>) value);
            break;
        case BOOLEAN:
            recordBuilder.withArray(entry, (GenericData.Array<Boolean>) value);
            break;
        case LONG:
            recordBuilder.withArray(entry, (GenericData.Array<Long>) value);
            break;
        // case T_TIMESTAMP:
        // recordBuilder.withArray(entry, convertStringDateArrayToLongArray(value, this::getTimeStamp));
        // break;
        // case T_DATE:
        // recordBuilder.withArray(entry, convertStringDateArrayToLongArray(value, this::getDate));
        // break;
        // case T_TIME:
        // recordBuilder.withArray(entry, convertStringDateArrayToLongArray(value, this::getTime));
        // break;
        // case T_DATETIME:
        // recordBuilder.withArray(entry, convertStringDateArrayToLongArray(value, this::getDateTime));
        // break;
        default:
            throw new new IllegalStateException(i18n.undefinedType(entry.getType().name()));
        }
    }

    private void processField(org.apache.avro.Schema.Field field, Object value, Record.Builder recordBuilder, Entry entry) {
        switch (field.schema().getType()) {
        case RECORD:
            recordBuilder.withRecord(entry,
                    convertAvroToRecord(GenericData.Record.class.cast(value), field.schema().getFields()));
            break;
        case STRING:
            recordBuilder.withString(entry, value.toString());
            break;
        case BYTES:
            recordBuilder.withBytes(entry, ((java.nio.ByteBuffer) value).array());
            break;
        case INT:
            recordBuilder.withInt(entry, (Integer) value);
            break;
        case FLOAT:
            recordBuilder.withFloat(entry, (Float) value);
            break;
        case DOUBLE:
            recordBuilder.withDouble(entry, (Double) value);
            break;
        case BOOLEAN:
            recordBuilder.withBoolean(entry, (Boolean) value);
            break;
        case LONG:
            recordBuilder.withLong(entry, (Long) value);
            break;
        // case T_TIMESTAMP:
        // recordBuilder.withTimestamp(entry, getTimeStamp(value));
        // break;
        // case T_DATE:
        // recordBuilder.withTimestamp(entry, getDate(value));
        // break;
        // case T_TIME:
        // recordBuilder.withTimestamp(entry, getTime(value));
        // break;
        // case T_DATETIME:
        // recordBuilder.withTimestamp(entry, getDateTime(value));
        // break;
        case NULL:
            break;
        default:
            throw new new IllegalStateException(i18n.undefinedType(entry.getType().name()));
        }
    }

    private Long getTimeStamp(Object obj) {
        return Double.valueOf(obj.toString()).longValue() * 1000;
    }

    private Long getDate(Object obj) {
        return LocalDate.parse(obj.toString()).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private Long getTime(Object obj) {
        return Instant.ofEpochMilli(LocalTime.parse(obj.toString(), DateTimeFormatter.ISO_TIME).toSecondOfDay() * 1000L)
                .toEpochMilli();
    }

    private Long getDateTime(Object obj) {
        return LocalDateTime.parse(obj.toString(), DateTimeFormatter.ISO_DATE_TIME).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Collection<Long> convertStringDateArrayToLongArray(Object value, Function<String, Long> function) {
        return (Collection<Long>) ((GenericData.Array) value).stream().map(Object::toString).map(function).collect(toList());
    }

}
