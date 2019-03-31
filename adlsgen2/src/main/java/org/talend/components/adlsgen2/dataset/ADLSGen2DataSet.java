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
package org.talend.components.adlsgen2.dataset;

import lombok.Data;

import static org.talend.components.adlsgen2.service.UIActionService.ACTION_FILESYSTEMS;

import java.io.Serializable;

import org.talend.components.adlsgen2.datastore.ADLSGen2Connection;
import org.talend.components.adlsgen2.service.CSVFormat;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.DefaultValue;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

@Data
@DataSet("ADLSGen2DataSet")
@GridLayout({ //
        @GridLayout.Row("connection"), //
        @GridLayout.Row("filesystem"), //
        @GridLayout.Row("blobPath"), //
        @GridLayout.Row("format"), //
        @GridLayout.Row({ "recordDelimiter", "fieldDelimiter" }), //
        @GridLayout.Row("header"), //
        @GridLayout.Row("csvSchema"), //
})
@Documentation("ADLS DataSet")
public class ADLSGen2DataSet implements Serializable {

    @Option
    @Required
    @Documentation("ADLS Gen2 Connection")
    private ADLSGen2Connection connection;

    @Option
    @Required
    @Suggestable(value = ACTION_FILESYSTEMS, parameters = { "connection" })
    @Documentation("FileSystem")
    private String filesystem;

    @Option
    @Required
    @Documentation("Path to Blob Object")
    private String blobPath;

    @Option
    @Required
    @DefaultValue("CSV")
    @Documentation("Format of Blob content")
    private BlobFormat format;

    @Option
    @Required
    @ActiveIf(target = "format", value = "CSV")
    @DefaultValue("SEMICOLON")
    @Documentation("CSV Field Delimiter")
    private CSVFormat.FieldDelimiter fieldDelimiter;

    @Option
    @Required
    @ActiveIf(target = "format", value = "CSV")
    @DefaultValue("LF")
    @Documentation("Record Delimiter")
    private CSVFormat.RecordDelimiter recordDelimiter;

    @Option
    @Required
    @ActiveIf(target = "format", value = "CSV")
    @Documentation("Schema")
    private String csvSchema;

    @Option
    @Required
    @ActiveIf(target = "format", value = "CSV")
    @Documentation("Has header line")
    private boolean header;

    public enum BlobFormat {
        CSV,
        AVRO,
        JSON,
        PARQUET
    }
}
