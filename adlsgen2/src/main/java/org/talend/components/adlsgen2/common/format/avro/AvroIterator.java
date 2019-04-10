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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.configuration.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AvroIterator implements Iterator<Record> {

    private final AvroConverter converter;

    private DataFileStream<GenericRecord> reader;

    private AvroIterator(InputStream inputStream) {
        converter = AvroConverter.of();
        try {
            DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>();
            reader = new DataFileStream<GenericRecord>(inputStream, datumReader);
        } catch (IOException e) {
            log.error("[AvroIterator] {}", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean hasNext() {
        if (reader.hasNext()) {
            return true;
        } else {
            try {
                reader.close();
                return false;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public Record next() {
        GenericRecord current = reader.next();
        return current != null ? converter.toRecord(current) : null;
    }

    /**
     *
     */
    public static class Builder {

        private Builder() {
        }

        public static AvroIterator.Builder of() {
            return new AvroIterator.Builder();
        }

        public AvroIterator.Builder withConfiguration(@Configuration("avroConfiguration") final AvroConfiguration configuration) {
            return this;
        }

        public AvroIterator parse(InputStream in) {
            return new AvroIterator(in);
        }

        public AvroIterator parse(String content) {
            throw new UnsupportedOperationException("#parse(String content)");
        }
    }

}
