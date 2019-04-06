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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.configuration.Configuration;

public class CsvIterator implements Iterator<Record> {

    private CSVFormat csv;

    private final Reader reader;

    private CsvConverter converter;

    private CSVParser parser;

    private Iterator<CSVRecord> records;

    private CsvIterator(Reader inReader, CSVFormat format) {
        csv = format;
        reader = inReader;
        converter = CsvConverter.of().withFormat(csv);
        try {
            parser = csv.parse(reader);
            records = parser.iterator();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return records.hasNext();
    }

    @Override
    public Record next() {
        if (hasNext()) {
            return converter.toRecord(records.next());
        } else {
            try {
                parser.close();
                reader.close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return null;
        }
    }

    public static class Builder {

        private CSVFormat csvFormat;

        private Builder() {
            csvFormat = CSVFormat.DEFAULT;
        }

        public static Builder of() {
            return new Builder();
        }

        public Builder withConfiguration(@Configuration("csvConfiguration") final CsvConfiguration configuration) {
            csvFormat = csvFormat //
                    .withDelimiter(configuration.getFieldDelimiter().getDelimiterChar()) //
                    .withRecordSeparator(configuration.getRecordDelimiter().getSeparatorChar()) //
                    .withHeader(configuration.getCsvSchema().split(configuration.getFieldDelimiter().getDelimiter())) //
            // TODO manage other parameters.
            ;
            if (configuration.isHeader()) {
                csvFormat = csvFormat.withFirstRecordAsHeader();
            }

            return this;
        }

        public CsvIterator parse(InputStream in) {
            // TODO manage encoding
            return new CsvIterator(new InputStreamReader(in, StandardCharsets.UTF_8), csvFormat);
        }

        public CsvIterator parse(String content) {
            return new CsvIterator(new StringReader(content), csvFormat);
        }
    }
}
