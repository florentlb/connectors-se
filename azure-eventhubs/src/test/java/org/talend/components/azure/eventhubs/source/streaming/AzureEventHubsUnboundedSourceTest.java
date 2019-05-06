/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package org.talend.components.azure.eventhubs.source.streaming;

import static org.talend.sdk.component.junit.SimpleFactory.configurationByExample;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.talend.components.azure.common.Protocol;
import org.talend.components.azure.common.connection.AzureStorageConnectionAccount;
import org.talend.components.azure.eventhubs.AzureEventHubsTestBase;
import org.talend.components.azure.eventhubs.dataset.AzureEventHubsStreamDataSet;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.junit5.WithComponents;
import org.talend.sdk.component.maven.MavenDecrypter;
import org.talend.sdk.component.maven.Server;
import org.talend.sdk.component.runtime.manager.chain.Job;

@Disabled("not ready")
@WithComponents("org.talend.components.azure.eventhubs")
class AzureEventHubsUnboundedSourceTest extends AzureEventHubsTestBase {

    private static final String ACCOUNT_NAME;

    private static final String ACCOUNT_KEY;

    static {
        final MavenDecrypter decrypter = new MavenDecrypter();
        final Server storageAccount = decrypter.find("azure-storage-account");
        ACCOUNT_NAME = storageAccount.getUsername();
        ACCOUNT_KEY = storageAccount.getPassword();
    }

    @Test
    void testStreamingInput() {
        AzureEventHubsStreamInputConfiguration inputConfiguration = new AzureEventHubsStreamInputConfiguration();
        final AzureEventHubsStreamDataSet dataSet = new AzureEventHubsStreamDataSet();
        dataSet.setDatastore(getDataStore());
        dataSet.setEventHubName(EVENTHUB_NAME);
        dataSet.setConsumerGroupName("consumer-group-1");

        AzureStorageConnectionAccount connectionAccount = new AzureStorageConnectionAccount();
        connectionAccount.setAccountName(ACCOUNT_NAME);
        connectionAccount.setProtocol(Protocol.HTTPS);
        connectionAccount.setAccountKey(ACCOUNT_KEY);
        dataSet.setStorageConn(connectionAccount);
        dataSet.setContainerName("eventhub-test");

        inputConfiguration.setDataset(dataSet);

        final String config = configurationByExample().forInstance(inputConfiguration).configured().toQueryString();
        Job.components().component("azureeventhubs-input", "AzureEventHubs://AzureEventHubsInputStream?" + config)
                .component("collector", "test://collector").connections().from("azureeventhubs-input").to("collector").build()
                .run();
        final List<Record> records = getComponentsHandler().getCollectedData(Record.class);
    }

}