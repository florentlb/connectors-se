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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.talend.components.adlsgen2.AdlsGen2TestBase;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.junit.http.internal.impl.AzureStorageCredentialsRemovalResponseLocator;
import org.talend.sdk.component.junit5.WithComponents;
import org.talend.sdk.component.runtime.manager.chain.Job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.talend.sdk.component.junit.SimpleFactory.configurationByExample;

@org.talend.sdk.component.junit.http.junit5.HttpApi(useSsl = true, responseLocator = AzureStorageCredentialsRemovalResponseLocator.class)
@WithComponents("org.talend.components.adlsgen2")
class AdlsGen2InputTest extends AdlsGen2TestBase {

    @Test
    public void produce() {
        final String config = configurationByExample().forInstance(inputConfiguration).configured().toQueryString();
        Job.components().component("mycomponent", "AdlsGen2://AdlsGen2Input?" + config) //
                .component("collector", "test://collector") //
                .connections() //
                .from("mycomponent") //
                .to("collector") //
                .build() //
                .run();
        final List<Record> records = components.getCollectedData(Record.class);
        assertNotNull(records);
        assertEquals(10000, records.size());
    }

}
