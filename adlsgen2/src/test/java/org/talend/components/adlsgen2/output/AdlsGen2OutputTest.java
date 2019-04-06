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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.talend.components.adlsgen2.AdlsGen2TestBase;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.configuration.LocalConfiguration;
import org.talend.sdk.component.junit.http.internal.impl.AzureStorageCredentialsRemovalResponseLocator;
import org.talend.sdk.component.junit.http.junit5.HttpApi;
import org.talend.sdk.component.junit5.WithComponents;
import org.talend.sdk.component.runtime.manager.chain.Job;

import static java.util.Arrays.asList;
import static org.talend.sdk.component.junit.SimpleFactory.configurationByExample;

@HttpApi(useSsl = true, responseLocator = AzureStorageCredentialsRemovalResponseLocator.class)
@WithComponents("org.talend.components.adlsgen2")
class AdlsGen2OutputTest extends AdlsGen2TestBase {

    @Service
    private LocalConfiguration configuration;

    @Test
    public void produce() {
        outputConfiguration.setOverwrite(true);
        outputConfiguration.getDataSet().setBlobPath("customers_test_produce.csv");
        components.setInputData(asList(createData(), createData(), createData()));
        final String config = configurationByExample().forInstance(outputConfiguration).configured().toQueryString();
        Job.components() //
                .component("emitter", "test://emitter") //
                .component("out", "AdlsGen2://AdlsGen2Output?" + config) //
                .connections() //
                .from("emitter") //
                .to("out") //
                .build() //
                .run();
        final List<Record> records = components.getCollectedData(Record.class);
    }

}
