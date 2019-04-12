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

package org.talend.components.azure.runtime.input;

import java.net.URISyntaxException;
import java.util.Iterator;

import org.talend.components.azure.common.excel.ExcelFormat;
import org.talend.components.azure.dataset.AzureBlobDataset;
import org.talend.components.azure.service.AzureComponentServices;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ListBlobItem;
import lombok.AccessLevel;
import lombok.Getter;

public abstract class BlobFileReader {

    private RecordBuilderFactory recordBuilderFactory;

    private ItemRecordIterator iterator;

    private final AzureBlobDataset config;

    public BlobFileReader(AzureBlobDataset config, RecordBuilderFactory recordBuilderFactory,
            AzureComponentServices connectionServices) throws URISyntaxException, StorageException {
        this.recordBuilderFactory = recordBuilderFactory;
        this.config = config;
        CloudStorageAccount connection = connectionServices.createStorageAccount(config.getConnection());
        CloudBlobClient blobClient = connectionServices.createCloudBlobClient(connection,
                AzureComponentServices.DEFAULT_RETRY_POLICY);
        CloudBlobContainer container = blobClient.getContainerReference(config.getContainerName());

        Iterable<ListBlobItem> blobItems = container.listBlobs(config.getDirectory(), true);

        this.iterator = initItemRecordIterator(blobItems);
    }

    protected abstract ItemRecordIterator initItemRecordIterator(Iterable<ListBlobItem> blobItems);

    public Record readRecord() {
        return iterator.next();
    }

    // TODO no need of it in dss?
    protected RecordBuilderFactory getRecordBuilderFactory() {
        return recordBuilderFactory;
    }

    protected AzureBlobDataset getConfig() {
        return config;
    }

    public static class BlobFileReaderFactory {

        public static BlobFileReader getReader(AzureBlobDataset config, RecordBuilderFactory recordBuilderFactory,
                AzureComponentServices connectionServices) throws Exception {
            switch (config.getFileFormat()) {
            case CSV:
                return new CSVBlobFileReader(config, recordBuilderFactory, connectionServices);
            case AVRO:
                return new AvroBlobFileReader(config, recordBuilderFactory, connectionServices);
            case EXCEL: {
                if (config.getExcelOptions().getExcelFormat() == ExcelFormat.HTML) {
                    return new ExcelHTMLBlobFileReader(config, recordBuilderFactory, connectionServices);
                } else {
                    return new ExcelBlobFileReader(config, recordBuilderFactory, connectionServices);
                }
            }
            case PARQUET:
                return new ParquetBlobFileReader(config, recordBuilderFactory, connectionServices);
            default:
                throw new IllegalArgumentException("Unsupported file format"); // shouldn't be here
            }
        }
    }

    protected abstract class ItemRecordIterator<T> implements Iterator<Record> {

        private Iterator<ListBlobItem> blobItems;

        @Getter(AccessLevel.PROTECTED)
        private CloudBlob currentItem;

        protected ItemRecordIterator(Iterable<ListBlobItem> blobItemsList) {
            this.blobItems = blobItemsList.iterator();
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException("Use next() method until return null");
        }

        @Override
        public Record next() {
            T next = readNextItemRecord();

            return next != null ? convertToRecord(next) : null;
        }

        T readNextItemRecord() {
            if (currentItem == null) {
                return null; // No items exists
            }

            if (hasNextRecordTaken()) {
                return takeNextRecord();
            } else if (blobItems.hasNext()) {
                currentItem = (CloudBlob) blobItems.next();
                readItem();
                return readNextItemRecord(); // read record from next item
            } else {
                return null;
            }
        }

        protected abstract T takeNextRecord();

        protected abstract boolean hasNextRecordTaken();

        protected abstract Record convertToRecord(T next);

        protected abstract void readItem();

        protected void takeFirstItem() {
            if (blobItems.hasNext()) {
                currentItem = (CloudBlob) blobItems.next();
                readItem();
            }
        }
    }
}
