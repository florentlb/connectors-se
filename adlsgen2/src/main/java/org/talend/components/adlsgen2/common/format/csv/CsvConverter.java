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
