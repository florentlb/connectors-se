package org.talend.components.netsuite.service;

import java.util.List;
import java.util.stream.Collectors;

import org.talend.components.netsuite.dataset.NetSuiteDataSet;
import org.talend.components.netsuite.datastore.NetSuiteDataStore;
import org.talend.components.netsuite.runtime.NetSuiteDatasetRuntime;
import org.talend.components.netsuite.runtime.NetSuiteDatasetRuntimeImpl;
import org.talend.components.netsuite.runtime.NetSuiteEndpoint;
import org.talend.components.netsuite.runtime.client.NetSuiteClientService;
import org.talend.components.netsuite.runtime.v2018_2.client.NetSuiteClientFactoryImpl;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.SuggestionValues;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

@Service
public class NetSuiteService {

    private NetSuiteEndpoint endpoint;

    private NetSuiteDatasetRuntime dataSetRuntime;

    private NetSuiteClientService<?> clientService;

    @Service
    private RecordBuilderFactory recordBuilderFactory;

    public synchronized void connect(NetSuiteDataStore dataStore) {
        endpoint = new NetSuiteEndpoint(NetSuiteClientFactoryImpl.getFactory(),
                NetSuiteEndpoint.createConnectionConfig(dataStore));
        clientService = endpoint.getClientService();
        dataSetRuntime = new NetSuiteDatasetRuntimeImpl(clientService.getMetaDataSource(), recordBuilderFactory);
    }

    List<SuggestionValues.Item> getRecordTypes(NetSuiteDataStore dataStore) {
        if (dataSetRuntime == null) {
            connect(dataStore);
        }
        return dataSetRuntime.getRecordTypes();
    }

    List<SuggestionValues.Item> getSearchTypes(NetSuiteDataSet dataSet) {
        if (dataSetRuntime == null) {
            connect(dataSet.getDataStore());
        }
        return dataSetRuntime.getSearchInfo(dataSet.getRecordType()).getFields().stream()
                .map(info -> new SuggestionValues.Item(info.getName(), info.getName())).collect(Collectors.toList());
    }

    List<SuggestionValues.Item> getSearchFieldOperators(NetSuiteDataStore dataStore) {
        if (dataSetRuntime == null) {
            connect(dataStore);
        }
        return dataSetRuntime.getSearchFieldOperators().stream().map(name -> new SuggestionValues.Item(name, name))
                .collect(Collectors.toList());
    }

    public Schema getSchema(NetSuiteDataSet dataSet) {
        if (dataSetRuntime == null) {
            connect(dataSet.getDataStore());
        }
        return dataSetRuntime.getSchema(dataSet.getRecordType());
    }

    public Schema getRejectSchema(NetSuiteDataSet dataSet, Schema schema) {
        if (dataSetRuntime == null) {
            connect(dataSet.getDataStore());
        }
        return dataSetRuntime.getSchemaReject(dataSet.getRecordType(), schema);
    }

    public NetSuiteClientService<?> getClientService(NetSuiteDataStore dataStore) {
        if (clientService == null) {
            connect(dataStore);
        }
        return clientService;
    }

}