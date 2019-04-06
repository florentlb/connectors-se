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

// import org.apache.parquet.hadoop.metadata.ParquetMetadata;
public class ParquetConverter {// implements RecordConverter<ParquetMetadata> {

    public static ParquetConverter of() {
        return new ParquetConverter();
    }

    private ParquetConverter() {

    }

    // @Override
    // public Record toRecord(ParquetMetadata value) {
    // // try {
    // // InputFile file = new InputFile() {
    // // @Override
    // // public long getLength() throws IOException {
    // // throw new UnsupportedOperationException("#getLength()");
    // // }
    // //
    // // @Override
    // // public SeekableInputStream newStream() throws IOException {
    // // throw new UnsupportedOperationException("#newStream()");
    // // }
    // // };
    // // ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(file).build();
    // // GenericRecord nextRecord = reader.read();
    // // } catch (IOException e) {
    // // throw new IllegalStateException(e);
    // // }
    // //
    // //
    // throw new UnsupportedOperationException("#toRecord()");
    // }
    //
    // @Override
    // public ParquetMetadata fromRecord(Record record) {
    // // AvroRecord.class.cast(in).unwrap(IndexedRecord.class))
    // // new AvroRecord(in)
    //
    // // IndexedRecord ir = AvroRecord.class.cast(record).unwrap(IndexedRecord.class);
    // // AvroRecord recr = new AvroRecord(record);
    // // new AvroSchemaBuilder().withElementSchema(recr.getSchema());
    // // org.apache.avro.Schema schema = null;
    // // try (ParquetWriter<GenericData.Record> writer = AvroParquetWriter.<GenericData.Record>builder(file)
    // // .withSchema(schema)
    // // .withCompressionCodec(CompressionCodecName.GZIP)
    // // // .withConf(conf)
    // // .withPageSize(4 * 1024 * 1024) //For compression
    // // .withRowGroupSize(16 * 1024 * 1024) //For write buffering (Page size)
    // // .build()) {
    // //
    // // //We only have one record to write in our example
    // // writer.write(record);
    // //
    // //
    // // } catch (Exception e) {
    // //
    // // }
    //
    // throw new UnsupportedOperationException("#fromRecord()");
    // }
}
