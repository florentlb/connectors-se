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

import java.util.Iterator;

import org.talend.sdk.component.api.record.Record;

public class ParquetIterator implements Iterator<Record> {

    // private static final Schema SCHEMA;
    private static final String SCHEMA_LOCATION = "/org/maxkons/hadoop_snippets/parquet/avroToParquet.avsc";

    // private static final Path OUT_PATH = new Path("/home/max/Downloads/sample.parquet");

    /*
     * static {
     * try (InputStream inStream = ParquetReaderWriterWithAvro.class.getResourceAsStream(SCHEMA_LOCATION)) {
     * SCHEMA = new Schema.Parser().parse(IOUtils.toString(inStream, "UTF-8"));
     * } catch (IOException e) {
     * throw new RuntimeException("Can't read SCHEMA file from" + SCHEMA_LOCATION, e);
     * }
     * }
     * 
     * public static void main(String[] args) throws IOException {
     * List<GenericData.Record> sampleData = new ArrayList<>();
     * 
     * GenericData.Record record = new GenericData.Record(SCHEMA);
     * record.put("c1", 1);
     * record.put("c2", "someString");
     * sampleData.add(record);
     * 
     * record = new GenericData.Record(SCHEMA);
     * record.put("c1", 2);
     * record.put("c2", "otherString");
     * sampleData.add(record);
     * 
     * ParquetReaderWriterWithAvro writerReader = new ParquetReaderWriterWithAvro();
     * writerReader.writeToParquet(sampleData, OUT_PATH);
     * writerReader.readFromParquet(OUT_PATH);
     * }
     * 
     * 
     * private static final Schema SCHEMA = ;
     * 
     * @SuppressWarnings("unchecked")
     * public void readFromParquet(Path filePathToRead) throws IOException {
     * try (ParquetReader<GenericData.Record> reader = AvroParquetReader
     * .<GenericData.Record>builder(filePathToRead)
     * .withConf(new ConfigurationUtil())
     * .build()) {
     * GenericData.Record record;
     * while ((record = reader.read()) != null) {
     * System.out.println(record);
     * }
     * }
     * }
     * 
     * public void writeToParquet(List<GenericData.Record> recordsToWrite, Path fileToWrite) throws IOException {
     * try (ParquetWriter<GenericData.Record> writer = AvroParquetWriter
     * .<GenericData.Record>builder(fileToWrite)
     * .withSchema(SCHEMA)
     * // .withConf(new Configuration())
     * // .withCompressionCodec(CompressionCodecName.SNAPPY)
     * .build()) {
     * 
     * for (GenericData.Record record : recordsToWrite) {
     * writer.write(record);
     * }
     * }
     * }
     */

    @Override
    public boolean hasNext() {
        /*
         * org.apache.avro.Schema schema;
         * try (ParquetWriter<GenericData.Record> writer = AvroParquetWriter.<GenericData.Record>builder(file)
         * .withSchema(schema)
         * .withCompressionCodec(CompressionCodecName.GZIP)
         * // .withConf(conf)
         * .withPageSize(4 * 1024 * 1024) //For compression
         * .withRowGroupSize(16 * 1024 * 1024) //For write buffering (Page size)
         * .build()) {
         * 
         * //We only have one record to write in our example
         * writer.write(record);
         * 
         * 
         * } catch (Exception e) {
         * 
         * }
         */
        throw new UnsupportedOperationException("#hasNext()");
    }

    @Override
    public Record next() {
        // try {
        // ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(file).build();
        // GenericRecord nextRecord = reader.read();
        //
        //// ParquetReader<Gen(ericRecord> reader = AvroParquetReader.<GenericRecord>builder(file).build();
        //// GenericRecord nextRecord = reader.read();
        // Schema schema = new Schema.Parser().parse(Resources.getResource("map.avsc").openStream());
        // File tmp = File.createTempFile(getClass().getSimpleName(), ".tmp");
        // tmp.deleteOnExit();
        // tmp.delete();
        // Path file = Paths.get(tmp.getPath());
        // AvroParquetWriter<GenericRecord> writer = new AvroParquetWriter<GenericRecord>(file, schema);
        // // Write a record with an empty map.
        // Map<String, Object> emptyMap = new HashMap<String, Object>();
        // // not empty any more
        // emptyMap.put("SOMETHING", "ew SOMETHING()");
        // GenericData.Record record = new GenericRecordBuilder(schema).set("mymap", emptyMap).build();
        // writer.write(record);
        // writer.close();
        //
        //
        // AvroParquetReader<GenericRecord> reader = new AvroParquetReader<GenericRecord>(file);
        // GenericRecord nextRecord = reader.read();
        //
        // String path;
        //
        //
        // } catch (Exception ex) {
        // ex.printStackTrace(System.out);
        // }
        throw new UnsupportedOperationException("#next()");
    }

}
