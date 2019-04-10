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
package org.talend.components.adlsgen2.common.format.parquet;

import java.io.IOException;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.junit.jupiter.api.Test;
import org.talend.components.adlsgen2.AdlsGen2TestBase;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.junit5.WithComponents;

@WithComponents("org.talend.components.adlsgen2")
class ParquetConverterTest extends AdlsGen2TestBase {

    @Test
    void readParquetSample() {
        ParquetConverter converter = ParquetConverter.of();
        try {
            Path sample = new Path(getClass().getResource("/common/format/parquet/sample.parquet").getFile());
            HadoopInputFile hdpIn = HadoopInputFile.fromPath(sample, new Configuration());
            ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord> builder(hdpIn).build();
            GenericRecord current;
            Record record;
            while ((current = reader.read()) != null) {
                record = converter.toRecord(current);
                System.err.println(current);
                System.err.println(record);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }
}
