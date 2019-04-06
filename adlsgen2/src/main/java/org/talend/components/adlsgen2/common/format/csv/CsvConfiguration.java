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

import java.io.Serializable;

import org.talend.components.adlsgen2.common.format.FileEncoding;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.DefaultValue;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

import static org.talend.sdk.component.api.configuration.ui.layout.GridLayout.FormType.ADVANCED;

@Data
@GridLayout({ //
        @GridLayout.Row({ "recordDelimiter", "fieldDelimiter" }), //
        @GridLayout.Row("header"), //
        @GridLayout.Row("csvSchema"), //
})
@GridLayout(names = ADVANCED, value = { @GridLayout.Row("encoding") })
@Documentation("CSV Configuration")
public class CsvConfiguration implements Serializable {

    @Option
    @DefaultValue("SEMICOLON")
    @Documentation("CSV Field Delimiter")
    private CsvFieldDelimiter fieldDelimiter;

    @Option
    @DefaultValue("LF")
    @Documentation("Record Delimiter")
    private CsvRecordSeparator recordDelimiter;

    @Option
    @Documentation("Schema")
    private String csvSchema;

    @Option
    @Documentation("Has header line")
    private boolean header;

    @Option
    @Documentation("File Encoding")
    private FileEncoding encoding;

}
