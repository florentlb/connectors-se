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

package org.talend.components.azure.runtime.output;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.util.HadoopOutputFile;
import org.apache.parquet.io.OutputFile;
import org.talend.components.azure.common.service.AzureComponentServices;
import org.talend.components.azure.output.BlobOutputConfiguration;
import org.talend.components.azure.runtime.converters.ParquetConverter;
import org.talend.components.azure.service.AzureBlobComponentServices;
import org.talend.sdk.component.api.record.Record;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

public class ParquetBlobFileWriter extends BlobFileWriter {

    private BlobOutputConfiguration config;

    public ParquetBlobFileWriter(BlobOutputConfiguration config, AzureBlobComponentServices connectionServices) throws Exception {
        super(config, connectionServices);
        this.config = config;
    }

    @Override
    public void generateFile() throws URISyntaxException, StorageException {
        String fileName = config.getDataset().getDirectory() + "/" + config.getBlobNameTemplate() + System.currentTimeMillis()
                + ".parquet";

        CloudBlockBlob blob = getContainer().getBlockBlobReference(fileName);
        if (blob.exists(null, null, AzureComponentServices.getTalendOperationContext())) {
            generateFile();
            return;
        }

        setCurrentItem(blob);
    }

    @Override
    public void flush() {
        File tempFilePath = null;
        try {
            tempFilePath = File.createTempFile("tempFile", ".parquet");
            OutputFile tempFile = HadoopOutputFile.fromPath(new org.apache.hadoop.fs.Path(tempFilePath.getPath()), new Configuration());
            ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(tempFile).build();
            for (Record r: getBatch()) {
                writer.write(ParquetConverter.of().fromRecord(r));
            }

            writer.close();
            Files.copy(tempFilePath.toPath(), ((CloudBlockBlob) getCurrentItem()).openOutputStream());
        } catch (IOException | StorageException e) {
            e.printStackTrace();
        } finally {
            if (tempFilePath != null) {
                tempFilePath.delete();
            }
        }
    }

    @Override
    public void complete() {
        //NOOP
    }
}
