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
package org.talend.components.adlsgen2.common.format;

import java.io.Serializable;

import org.talend.components.adlsgen2.common.format.csv.CsvConfiguration;
import org.talend.components.adlsgen2.common.format.unknown.UnknownConfiguration;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.ui.DefaultValue;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;

@Data
public class FileFormatConfiguration implements Serializable {

    @Option
    @Required
    @DefaultValue("CSV")
    @Documentation("File Format")
    private FileFormat fileFormat;

    @Option
    @ActiveIf(target = "fileFormat", value = "CSV")
    @Documentation("CSV Configuration")
    private CsvConfiguration csvConfiguration;

    @Option
    @ActiveIf(target = "fileFormat", value = "AVRO")
    @Documentation("AVRO Configuration")
    private CsvConfiguration avroConfiguration;

    @Option
    @ActiveIf(target = "fileFormat", value = "JSON")
    @Documentation("JSON Configuration")
    private CsvConfiguration jsonConfiguration;

    @Option
    @ActiveIf(target = "fileFormat", value = "PARQUET")
    @Documentation("Parquet Configuration")
    private CsvConfiguration parquetConfiguration;

    @Option
    @ActiveIf(target = "fileFormat", value = "UNKNOWN")
    @Documentation("Unknown File Configuration (plain text)")
    private UnknownConfiguration unknownConfiguration;

}
