// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.fileio.s3;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.talend.components.test.RecordSetUtil.getSimpleTestData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.beam.runners.direct.DirectOptions;
import org.apache.beam.runners.spark.SparkPipelineOptions;
import org.apache.beam.runners.spark.SparkRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.talend.components.adapter.beam.BeamLocalRunnerOption;
import org.talend.components.adapter.beam.transform.DirectCollector;
import org.talend.components.adapter.beam.transform.DirectConsumerCollector;
import org.talend.components.fileio.configuration.FieldDelimiterType;
import org.talend.components.fileio.configuration.RecordDelimiterType;
import org.talend.components.fileio.configuration.SimpleFileIOFormat;
import org.talend.components.test.RecordSet;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.java8.Consumer;

import com.talend.shaded.com.amazonaws.services.s3.model.ObjectMetadata;

/**
 * Integration tests for {@link S3Input} and {@link S3Output} on spark, focusing on
 * the use cases where data is written by an output component, and then read by an input component.
 *
 * The input component should be able to read all files generated by the output component.
 */
public class S3SparkRuntimeTestIT {

    public Pipeline pWrite;

    public Pipeline pRead;

    private SparkPipelineOptions options;

    /** Set up credentials for integration tests. */
    @Rule
    public S3TestResource s3 = S3TestResource.of();

    @Before
    public void setupLazyAvroCoder() {
        options = PipelineOptionsFactory.as(SparkPipelineOptions.class);
        options.setRunner(SparkRunner.class);
        options.setSparkMaster("local");
        options.setStreaming(false);
        pWrite = Pipeline.create(options);
        pRead = Pipeline.create(options);

    }

    /**
     * Tests a round-trip on the data when writing to the data source using the given output properties, then
     * subsequently reading using the given input properties. This is the equivalent of two pipeline jobs.
     *
     * @param initialData The initial data set to write, then read.
     * @param outputProps The properties used to create the output runtime.
     * @param inputProps The properties used to create the input runtime.
     * @return The data returned from the round-trip.
     */
    protected List<IndexedRecord> runRoundTripPipelines(List<IndexedRecord> initialData, S3OutputConfig outputConfig,
            S3DataSet dataset) {
        // Create the runtimes.
        S3Output outputRuntime = new S3Output(outputConfig);
        S3Input inputRuntime = new S3Input(dataset);

        // Create a pipeline to write the records to the output.
        {
            PCollection<IndexedRecord> input = pWrite.apply(Create.<IndexedRecord> of(initialData));
            input.apply(outputRuntime);
            pWrite.run().waitUntilFinish();
        }

        // Read the records that were written.
        try (DirectCollector<IndexedRecord> collector = DirectCollector.of()) {
            PCollection<IndexedRecord> input = pRead.apply(inputRuntime);
            input.apply(collector);
            pRead.run().waitUntilFinish();

            // Return the list of records from the round trip.
            return collector.getRecords();
        }
    }

    protected List<IndexedRecord> getSample(S3DataSet dataset) {
        dataset.setLimit(10);

        S3Input datasetRuntime = new S3Input(dataset);

        // TODO in this options, disable lots of thing, not right, need to check it for more complex env, not only directly local
        // and no parallize
        // run
        // options = PipelineOptionsFactory.as(DirectOptions.class);
        // options.setTargetParallelism(1);
        // options.setRunner(DirectRunner.class);
        // options.setEnforceEncodability(false);
        // options.setEnforceImmutability(false);
        DirectOptions options = BeamLocalRunnerOption.getOptions();
        final Pipeline p = Pipeline.create(options);

        List<IndexedRecord> result = new ArrayList<IndexedRecord>();
        Consumer consumer = new Consumer<IndexedRecord>() {

            @Override
            public void accept(IndexedRecord in) {
                result.add(in);
            }
        };

        try (DirectConsumerCollector<IndexedRecord> collector = DirectConsumerCollector.of(consumer)) {
            // Collect a sample of the input records.
            p.apply(datasetRuntime) //
                    .apply(collector);
            try {
                p.run().waitUntilFinish();
            } catch (Pipeline.PipelineExecutionException e) {
                if (e.getCause() instanceof TalendRuntimeException)
                    throw (TalendRuntimeException) e.getCause();
                throw e;
            }
        }
        return result;
    }

    protected Schema getSchema(S3DataSet dataset) {
        List<IndexedRecord> rows = getSample(dataset);
        return rows.get(0).getSchema();
    }

    public void test_noEncryption(S3DataSet dataset) throws IOException {
        // The file that we will be creating.
        RecordSet rs = getSimpleTestData(0);

        // Configure the components.
        S3OutputConfig outputConfig = new S3OutputConfig();
        outputConfig.setDataset(dataset);

        List<IndexedRecord> actual = runRoundTripPipelines(rs.getAllData(), outputConfig, dataset);

        List<IndexedRecord> expected = rs.getAllData();
        assertThat(actual, containsInAnyOrder(expected.toArray()));

        List<IndexedRecord> samples = getSample(dataset);
        assertThat(samples, containsInAnyOrder(expected.toArray()));

        Schema schema = getSchema(dataset);
        assertEquals(expected.get(0).getSchema(), schema);
    }

    /**
     * Basic Avro test.
     */
    @Test
    public void testAvro_noEncryption() throws IOException {
        S3DataSet dataset = s3.createS3DataSet();
        dataset.setFormat(SimpleFileIOFormat.AVRO);
        test_noEncryption(dataset);

        // Get some object metadata from the results.
        ObjectMetadata md = s3.getObjectMetadata(dataset);
        assertThat(md.getSSEAlgorithm(), nullValue());
        assertThat(md.getSSEAwsKmsKeyId(), nullValue());
    }

    /**
     * Basic Csv test.
     */
    @Test
    @Ignore("columns name different, can't editable")
    public void testCsv_noEncryption() throws IOException {
        S3DataSet dataset = s3.createS3DataSet();
        dataset.setFormat(SimpleFileIOFormat.CSV);
        dataset.setRecordDelimiter(RecordDelimiterType.LF);
        dataset.setFieldDelimiter(FieldDelimiterType.SEMICOLON);
        test_noEncryption(dataset);
    }

    /**
     * Basic Parquet test.
     */
    @Test
    public void testParquet_noEncryption() throws IOException {
        S3DataSet dataset = s3.createS3DataSet();
        dataset.setFormat(SimpleFileIOFormat.PARQUET);
        test_noEncryption(dataset);
    }

    /**
     * Basic Avro test with sseKmsEncryption.
     */
    @Test
    public void testAvro_sseKmsEncryption() throws IOException {
        S3DataSet dataset = s3.createS3Dataset(true, false);
        dataset.setFormat(SimpleFileIOFormat.AVRO);
        test_noEncryption(dataset);

        // Get some object metadata from the results.
        ObjectMetadata md = s3.getObjectMetadata(dataset);
        assertThat(md.getSSEAlgorithm(), is("aws:kms"));
        assertThat(md.getSSEAwsKmsKeyId(), is(dataset.getKmsForDataAtRest()));
    }

    /**
     * Basic Avro test with cseKmsEncryption.
     */
    @Ignore("cse not yet supported.")
    @Test
    public void testAvro_cseKmsEncryption() throws IOException {
        S3DataSet dataset = s3.createS3Dataset(false, true);
        dataset.setFormat(SimpleFileIOFormat.AVRO);
        test_noEncryption(dataset);
    }

    /**
     * Basic Avro test with sseKmsEncryption.
     */
    @Ignore("cse not yet supported.")
    @Test
    public void testAvro_sseAndCseKmsEncryption() throws IOException {
        S3DataSet dataset = s3.createS3Dataset(true, true);
        dataset.setFormat(SimpleFileIOFormat.AVRO);
        test_noEncryption(dataset);
    }
}