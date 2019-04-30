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
 */

package org.talend.components.azure.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.components.azure.BlobTestUtils;
import org.talend.components.azure.common.FileFormat;
import org.talend.components.azure.common.connection.AzureStorageConnectionAccount;
import org.talend.components.azure.dataset.AzureBlobDataset;
import org.talend.components.azure.datastore.AzureCloudConnection;
import org.talend.components.azure.service.AzureBlobComponentServices;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.junit.SimpleComponentRule;
import org.talend.sdk.component.junit5.WithComponents;
import org.talend.sdk.component.maven.MavenDecrypter;
import org.talend.sdk.component.maven.Server;
import org.talend.sdk.component.runtime.manager.chain.Job;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import static org.talend.sdk.component.junit.SimpleFactory.configurationByExample;

@WithComponents("org.talend.components.azure")
public class ParquetInputIT {

    @Service
    private AzureBlobComponentServices componentService;

    @ClassRule
    public static final SimpleComponentRule COMPONENT = new SimpleComponentRule("org.talend.components.azure");

    private static BlobInputProperties blobInputProperties;

    private CloudStorageAccount storageAccount;

    private String containerName;

    @BeforeEach
    public void init() throws Exception {
        containerName = "test-it-" + RandomStringUtils.randomAlphabetic(10).toLowerCase();
        Server account;
        final MavenDecrypter decrypter = new MavenDecrypter();

        AzureCloudConnection dataStore = new AzureCloudConnection();
        dataStore.setUseAzureSharedSignature(false);
        AzureStorageConnectionAccount accountConnection = new AzureStorageConnectionAccount();
        account = decrypter.find("azure.account");
        accountConnection.setAccountName(account.getUsername());
        accountConnection.setAccountKey(account.getPassword());

        dataStore.setAccountConnection(accountConnection);

        AzureBlobDataset dataset = new AzureBlobDataset();
        dataset.setConnection(dataStore);
        dataset.setFileFormat(FileFormat.PARQUET);

        dataset.setContainerName(containerName);
        blobInputProperties = new BlobInputProperties();
        blobInputProperties.setDataset(dataset);

        storageAccount = componentService.createStorageAccount(blobInputProperties.getDataset().getConnection());
        BlobTestUtils.createStorage(blobInputProperties.getDataset().getContainerName(), storageAccount);

    }

    private void uploadTestFile(String resourceName, String targetName) throws URISyntaxException, StorageException, IOException {
        CloudBlobContainer container = storageAccount.createCloudBlobClient()
                .getContainerReference(blobInputProperties.getDataset().getContainerName());
        CloudBlockBlob blockBlob = container
                .getBlockBlobReference(blobInputProperties.getDataset().getDirectory() + "/" + targetName);

        File resourceFile = new File(this.getClass().getClassLoader().getResource(resourceName).toURI());
        try (FileInputStream fileInputStream = new FileInputStream(resourceFile)) {
            blockBlob.upload(fileInputStream, resourceFile.length());
        }
    }

    @Test
    public void testInput1File1Record() throws Exception {
        final int recordSize = 1;
        final int columnSize = 6;
        final boolean booleanValue = true;
        final long longValue = 0L;
        final int intValue = 1;
        final double doubleValue = 2.0;
        final long dateValue = 1556612530082L;
        final byte[] bytesValue = new byte[] { 1, 2, 3 };

        blobInputProperties.getDataset().setDirectory("parquet");
        uploadTestFile("parquet/testParquet1Record.parquet", "testParquet1Record.parquet");

        String inputConfig = configurationByExample().forInstance(blobInputProperties).configured().toQueryString();
        Job.components().component("azureInput", "Azure://Input?" + inputConfig).component("collector", "test://collector")
                .connections().from("azureInput").to("collector").build().run();
        List<Record> records = COMPONENT.getCollectedData(Record.class);

        Assert.assertEquals("Records amount is different", recordSize, records.size());
        Record firstRecord = records.get(0);
        Assert.assertEquals(columnSize, firstRecord.getSchema().getEntries().size());
        Assert.assertEquals(booleanValue, firstRecord.getBoolean("booleanValue"));
        Assert.assertEquals(longValue, firstRecord.getLong("longValue"));
        Assert.assertEquals(intValue, firstRecord.getInt("intValue"));
        Assert.assertEquals(doubleValue, firstRecord.getDouble("doubleValue"), 0.01);
        Assert.assertEquals(ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateValue), ZoneId.of("UTC")),
                firstRecord.getDateTime("dateValue"));
        Assert.assertArrayEquals(bytesValue, firstRecord.getBytes("byteArray"));
    }

    @Test
    public void testInput1FileMultipleRecords() throws StorageException, IOException, URISyntaxException {
        final int recordSize = 6;
        blobInputProperties.getDataset().setDirectory("parquet");
        uploadTestFile("parquet/testParquet6Records.parquet", "testParquet6Records.parquet");

        String inputConfig = configurationByExample().forInstance(blobInputProperties).configured().toQueryString();
        Job.components().component("azureInput", "Azure://Input?" + inputConfig).component("collector", "test://collector")
                .connections().from("azureInput").to("collector").build().run();
        List<Record> records = COMPONENT.getCollectedData(Record.class);

        Assert.assertEquals("Records amount is different", recordSize, records.size());
    }

    @AfterEach
    public void removeContainer() throws URISyntaxException, StorageException {
        BlobTestUtils.deleteStorage(containerName, storageAccount);
    }

}