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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.hadoop.util.HadoopOutputFile;
import org.junit.jupiter.api.Test;
import org.talend.components.adlsgen2.AdlsGen2TestBase;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.junit5.WithComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithComponents("org.talend.components.adlsgen2")
class ParquetConverterTest extends AdlsGen2TestBase {

    private ParquetConverter converter = ParquetConverter.of();

    @Test
    void readParquetSample() throws Exception {
        Path sample = new Path(getClass().getResource("/common/format/parquet/sample.parquet").getFile());
        HadoopInputFile hdpIn = HadoopInputFile.fromPath(sample, new Configuration());
        ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord> builder(hdpIn).build();
        GenericRecord current;
        Record record;
        while ((current = reader.read()) != null) {
            record = converter.toRecord(current);
            assertNotNull(record);
            assertFalse(record.getString("name").isEmpty());
            assertTrue(record.getString("name").contains("Spark"));
            assertTrue(record.getArray(String.class, "topics").size() > 0);
        }
        reader.close();
    }

    @Test
    void writeParquetFile() throws Exception {
        String tmp = tmpDir + "talend-adlsgen2-test-" + UUID.randomUUID() + ".parquet";
        HadoopOutputFile hdpOut = HadoopOutputFile.fromPath(new Path(tmp), new org.apache.hadoop.conf.Configuration());
        GenericRecord record = converter.fromRecord(versatileRecord);
        assertNotNull(record);
        ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord> builder(hdpOut) //
                .withSchema(record.getSchema()) //
                .build();
        writer.write(record);
        writer.close();
        // ok let's check what's inside... we read parquet file back!
        HadoopInputFile hdpIn = HadoopInputFile.fromPath(new Path(tmp), new Configuration());
        ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord> builder(hdpIn).build();
        GenericRecord current;
        Record reconverted;
        current = reader.read();
        assertNotNull(current);
        reconverted = converter.toRecord(current);
        assertNotNull(reconverted);
        assertEquals("Bonjour", reconverted.getString("string1"));
        assertEquals("Olà", reconverted.getString("string2"));
        assertEquals(71, reconverted.getInt("int"));
        assertTrue(reconverted.getBoolean("boolean"));
        assertEquals(1971L, reconverted.getLong("long"));
        assertEquals(new Date(2019, 04, 22).getTime(), reconverted.getLong("datetime"));
        assertEquals(20.5f, reconverted.getFloat("float"));
        assertEquals(20.5, reconverted.getDouble("double"));
        //
        reader.close();
        Files.delete(Paths.get(tmp));
    }

}
