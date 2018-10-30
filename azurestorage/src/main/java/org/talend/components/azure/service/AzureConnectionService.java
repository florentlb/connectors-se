package org.talend.components.azure.service;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.components.azure.common.AzureConnection;
import org.talend.components.azure.common.AzureTableConnection;
import org.talend.components.azure.common.Protocol;
import org.talend.components.azure.table.input.InputProperties;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.SuggestionValues;
import org.talend.sdk.component.api.service.completion.Suggestions;
import org.talend.sdk.component.api.service.healthcheck.HealthCheck;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.talend.sdk.component.api.service.schema.DiscoverSchema;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.DynamicTableEntity;
import com.microsoft.azure.storage.table.EdmType;
import com.microsoft.azure.storage.table.EntityProperty;
import com.microsoft.azure.storage.table.TableQuery;

@Service
public class AzureConnectionService {

    @Service
    private RecordBuilderFactory factory;

    // you can put logic here you can reuse in components
    @HealthCheck("testConnection")
    public HealthCheckStatus testConnection(@Option AzureConnection azureConnection) {
        try {
            CloudStorageAccount cloudStorageAccount = createStorageAccount(azureConnection);
            final int MAX_TABLES = 1;
            final OperationContext operationContext = AzureTableUtils.getTalendOperationContext();
            // will throw an exception if not authorized
            // FIXME too long if account not exists
            cloudStorageAccount.createCloudTableClient().listTablesSegmented(null, MAX_TABLES, null, null, operationContext);
        } catch (Exception e) {
            return new HealthCheckStatus(HealthCheckStatus.Status.KO, e.getMessage());
        }
        // TODO i18n
        return new HealthCheckStatus(HealthCheckStatus.Status.OK, "Connected");
    }

    @Suggestions("getTableNames")
    public SuggestionValues getTableNames(@Option AzureConnection azureConnection) {
        List<SuggestionValues.Item> tableNames = new ArrayList<>();
        try {
            CloudStorageAccount storageAccount = createStorageAccount(azureConnection);
            final OperationContext operationContext = AzureTableUtils.getTalendOperationContext();
            for (String tableName : storageAccount.createCloudTableClient().listTables(null, null, operationContext)) {
                tableNames.add(new SuggestionValues.Item(tableName, tableName));
            }

        } catch (Exception e) {
            throw new RuntimeException("Can't get tableNames", e);
        }

        return new SuggestionValues(true, tableNames);
    }

    @DiscoverSchema("guessSchema")
    public Schema guessSchema(@Option final AzureTableConnection configuration) {
        final Schema.Entry.Builder entryBuilder = factory.newEntryBuilder();
        final Schema.Builder schemaBuilder = factory.newSchemaBuilder(Schema.Type.RECORD);
        // add 3 default columns
        schemaBuilder.withEntry(entryBuilder.withName("PartitionKey").withType(Schema.Type.STRING).build())
                .withEntry(entryBuilder.withName("RowKey").withType(Schema.Type.STRING).build())
                .withEntry(entryBuilder.withName("Timestamp").withType(Schema.Type.DATETIME).build());
        String tableName = configuration.getTableName();
        try {
            AzureConnection connection = configuration.getConnection();
            TableQuery<DynamicTableEntity> partitionQuery = TableQuery.from(DynamicTableEntity.class).take(1);
            CloudStorageAccount account = createStorageAccount(connection);
            System.out.println(account);
            Iterable<DynamicTableEntity> entities = executeQuery(account, tableName, partitionQuery);
            if (entities.iterator().hasNext()) {
                DynamicTableEntity result = entities.iterator().next();
                for (Map.Entry<String, EntityProperty> f : result.getProperties().entrySet()) {
                    schemaBuilder.withEntry(entryBuilder.withName(f.getKey())
                            .withType(getAppropriateType(f.getValue().getEdmType())).withNullable(true).build());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Can't get schema", e);
        }
        return schemaBuilder.build();
    }

    private Schema.Type getAppropriateType(EdmType edmType) {
        switch (edmType) {
        case BOOLEAN:
            return Schema.Type.BOOLEAN;
        case BYTE:
        case SBYTE:
        case INT16:
        case INT32:
            return Schema.Type.INT;
        case INT64:
        case DECIMAL:
        case SINGLE:
        case DOUBLE:
            return Schema.Type.DOUBLE;
        case DATE_TIME:
        case DATE_TIME_OFFSET:
            return Schema.Type.DATETIME;
        default:
            return Schema.Type.STRING;
        }

    }

    public Iterable<DynamicTableEntity> executeQuery(CloudStorageAccount storageAccount, String tableName,
            TableQuery<DynamicTableEntity> partitionQuery) throws URISyntaxException, StorageException {

        CloudTable cloudTable = storageAccount.createCloudTableClient().getTableReference(tableName);
        return cloudTable.execute(partitionQuery, null, AzureTableUtils.getTalendOperationContext());
    }

    public CloudStorageAccount createStorageAccount(AzureConnection azureConnection) throws URISyntaxException {
        StorageCredentials credentials = null;
        if (!azureConnection.isUseAzureSharedSignature()) {
            credentials = new StorageCredentialsAccountAndKey(azureConnection.getAccountName(), azureConnection.getAccountKey());
        } else {
            credentials = new StorageCredentialsSharedAccessSignature(azureConnection.getAzureSharedAccessSignature());
        }
        return new CloudStorageAccount(credentials, azureConnection.getProtocol() == Protocol.HTTPS);
    }

}