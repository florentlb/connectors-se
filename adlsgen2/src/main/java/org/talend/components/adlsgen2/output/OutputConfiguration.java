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

import lombok.Data;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.DefaultValue;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

@Data
@GridLayout(value = { //
        @GridLayout.Row({ "dataSet" }), //
        @GridLayout.Row({ "overwrite" }), //
})
@Documentation("ADLS output configuration")
public class OutputConfiguration implements Serializable {

    @Option
    @Documentation("Dataset")
    private org.talend.components.adlsgen2.dataset.AdlsGen2DataSet dataSet;

    @Option
    @DefaultValue("false")
    @Documentation("Overwrite Blob")
    private boolean overwrite;
}
