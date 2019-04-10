/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.components.adlsgen2.common.format.csv;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.talend.components.adlsgen2.common.converter.RecordConverter;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Record.Builder;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.configuration.Configuration;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CsvConverter implements RecordConverter<CSVRecord> {

    private StringWriter writer;

    private CSVFormat csvFormat;

    private CSVPrinter printer;

    @Service
    public static RecordBuilderFactory recordBuilderFactory;

    private CsvConverter() {
        writer = new StringWriter();
        csvFormat = CSVFormat.DEFAULT;
        try {
            printer = new CSVPrinter(writer, csvFormat);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static CsvConverter of() {
        return new CsvConverter();
    }

    @Override
    public Schema inferSchema(CSVRecord record) {
        throw new UnsupportedOperationException("#inferSchema()");
    }

    @Override
    public Record toRecord(CSVRecord value) {
        Builder record = recordBuilderFactory.newRecordBuilder();
        for (String s : csvFormat.getHeader()) {
            record.withString(s, value.get(s));
        }
        log.debug("record: {}", record.build());
        return record.build();
    }

    @Override
    public CSVRecord fromRecord(Record record) {
        throw new UnsupportedOperationException("#fromRecord()");
    }

    public CsvConverter withConfiguration(@Configuration("csvConfiguration") final CsvConfiguration configuration) {
        csvFormat = csvFormat //
                .withDelimiter(configuration.getFieldDelimiter().getDelimiterChar()) //
                .withRecordSeparator(configuration.getRecordDelimiter().getSeparatorChar()) //
                .withHeader(configuration.getCsvSchema().split(configuration.getFieldDelimiter().getDelimiter())) //
        // etc.
        ;
        return this;
    }

    public CsvConverter withFormat(final CSVFormat format) {
        csvFormat = format;

        return this;
    }

    public CsvConverter withDelimiter(final char delimiter) {
        csvFormat = csvFormat.withDelimiter(delimiter);

        return this;
    }

    public CsvConverter withDSeparator(final char separator) {
        csvFormat = csvFormat.withRecordSeparator(separator);

        return this;
    }

    public CsvConverter withHeader(final String[] headers) {
        csvFormat = csvFormat.withHeader(headers);

        return this;
    }

}
