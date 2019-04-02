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
package org.talend.components.adlsgen2.input;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ListIterator;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.talend.components.adlsgen2.service.I18n;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Icon.IconType;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.input.Emitter;
import org.talend.sdk.component.api.input.Producer;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

@Slf4j
@Version(1)
@Icon(value = IconType.FILE_CSV_O)
@Emitter(name = "AdlsGen2Input", family = "AdlsGen2")
@Documentation("Azure Data Lake Storage Gen2 Input")
public class AdlsGen2Input implements Serializable {

    private InputConfiguration configuration;

    private final org.talend.components.adlsgen2.service.AdlsGen2Service service;

    private final RecordBuilderFactory recordBuilder;

    private final I18n i18n;

    private ListIterator<Record> records;

    public AdlsGen2Input(@Option("configuration") final InputConfiguration configuration,
            final org.talend.components.adlsgen2.service.AdlsGen2Service service, final RecordBuilderFactory recordBuilderFactory,
            final I18n i18n) {
        this.configuration = configuration;
        this.service = service;
        this.recordBuilder = recordBuilderFactory;
        this.i18n = i18n;
    }

    @PostConstruct
    public void init() {
        records = service.pathRead(configuration).listIterator();
    }

    @Producer
    public Record next() {
        if (records.hasNext()) {
            return records.next();
        }
        return null;
    }

    @PreDestroy
    public void release() {
    }
}
