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
package org.talend.components.adlsgen2.output;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.talend.components.adlsgen2.service.ADLSGen2Service;
import org.talend.components.adlsgen2.service.I18n;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Icon.IconType;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Processor;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;

import com.csvreader.CsvWriter;

@Slf4j
@Version(1)
@Icon(value = IconType.FILE_CSV_O)
@Processor(name = "ADLSGen2Output", family = "adlsgen2")
@Documentation("Azure Data Lake Storage Gen2 Output")
public class ADLSGen2Output implements Serializable {

    private OutputConfiguration configuration;

    private final ADLSGen2Service service;

    private final I18n i18n;

    private char fieldDelimiter;

    private char recordDelimiter;

    public ADLSGen2Output(@Option("configuration") final OutputConfiguration configuration, final ADLSGen2Service service,
            final I18n i18n) {
        this.configuration = configuration;
        this.service = service;
        this.i18n = i18n;
        fieldDelimiter = configuration.getDataSet().getFieldDelimiter().getDelimiterChar();
        recordDelimiter = configuration.getDataSet().getRecordDelimiter().getDelimiterChar();
    }

    @PostConstruct
    public void init() {
    }

    @PreDestroy
    public void release() {
    }

    private String[] getStringArrayFromRecord(Record record) {
        List<String> values = new ArrayList<>();
        for (Schema.Entry field : record.getSchema().getEntries()) {
            values.add(record.getString(field.getName()));
        }
        return values.toArray(new String[0]);
    }

    private String toCsvFormat(final Record record) {
        StringWriter writer = new StringWriter();
        CsvWriter csvWriter = new CsvWriter(writer, fieldDelimiter);
        csvWriter.setRecordDelimiter(recordDelimiter);
        try {
            csvWriter.writeRecord(getStringArrayFromRecord(record));
            log.warn("[toCsvFormat] return content: {}", writer.toString());
            return writer.toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @ElementListener
    public void onElement(final Record record) {
        log.warn("[onElement] record: {}", record);
        String content = null;
        switch (configuration.getDataSet().getFormat()) {
        case CSV:
            content = toCsvFormat(record);
            break;
        case AVRO:
        case JSON:
        case PARQUET:
            throw new IllegalStateException("format not implemented");
        }
        log.warn("[onElement] writing {}", content);
        service.pathUpdate(configuration, content);
    }

}
