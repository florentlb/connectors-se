package org.talend.components.adlsgen2.common.format.parquet;

import java.io.IOException;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.junit.jupiter.api.Test;

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
class ParquetIteratorTest {

    @Test
    void readParquetFile() {
        // converter = ParquetConverter.of();
        try {
            HadoopInputFile hdpIn = HadoopInputFile.fromPath(new Path("/home/egallois/tmp/parquet_file.parquet"),
                    new org.apache.hadoop.conf.Configuration());
            ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord> builder(hdpIn).build();
            GenericRecord current = reader.read();
            System.err.println(current);

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }
}
