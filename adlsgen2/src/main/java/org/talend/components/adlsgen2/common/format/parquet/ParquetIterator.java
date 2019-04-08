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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.InputFile;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.configuration.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParquetIterator implements Iterator<Record> {

    private final ParquetConverter converter;

    InputFile input;

    ParquetReader<GenericRecord> reader;

    private GenericRecord current;

    private ParquetIterator(InputStream inputStream) {
        converter = ParquetConverter.of();
        log.warn("[ParquetIterator]");
        try {
            File targetFile = new File("/home/egallois/tmp/targetFile.tmp");
            java.nio.file.Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            IOUtils.closeQuietly(inputStream);
            HadoopInputFile hdpIn = HadoopInputFile.fromPath(new Path(targetFile.getPath()),
                    new org.apache.hadoop.conf.Configuration());
            reader = AvroParquetReader.<GenericRecord> builder(hdpIn).build();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void readFromParquet() throws IOException {
        try (ParquetReader<GenericData.Record> reader2 = AvroParquetReader.<GenericData.Record> builder(input).build()) {
            GenericData.Record record;
            while ((record = reader2.read()) != null) {
                System.out.println(record);
            }
        }
    }

    @Override
    public boolean hasNext() {
        try {
            current = reader.read();
            if (current == null) {
                reader.close();
                return false;
            }
            return true;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Record next() {
        if ((current != null) || hasNext()) {
            Record r = converter.toRecord(current);
            current = null;
            return r;
        } else {
            return null;
        }
    }

    /**
     *
     */
    public static class Builder {

        private Builder() {
        }

        public static ParquetIterator.Builder of() {
            return new ParquetIterator.Builder();
        }

        public ParquetIterator.Builder withConfiguration(
                @Configuration("parquetConfiguration") final ParquetConfiguration configuration) {
            return this;
        }

        public ParquetIterator parse(InputStream in) {
            return new ParquetIterator(in);
        }

        public ParquetIterator parse(String content) {
            throw new UnsupportedOperationException("#parse(String content)");
        }
    }
}
