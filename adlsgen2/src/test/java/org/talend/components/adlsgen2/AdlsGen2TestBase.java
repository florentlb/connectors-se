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
package org.talend.components.adlsgen2;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.talend.components.adlsgen2.common.format.FileFormat;
import org.talend.components.adlsgen2.common.format.csv.CsvConfiguration;
import org.talend.components.adlsgen2.common.format.csv.CsvConverter;
import org.talend.components.adlsgen2.common.format.csv.CsvFieldDelimiter;
import org.talend.components.adlsgen2.common.format.csv.CsvRecordSeparator;
import org.talend.components.adlsgen2.common.format.unknown.UnknownConverter;
import org.talend.components.adlsgen2.dataset.AdlsGen2DataSet;
import org.talend.components.adlsgen2.datastore.AdlsGen2Connection;
import org.talend.components.adlsgen2.datastore.AdlsGen2Connection.AuthMethod;
import org.talend.components.adlsgen2.datastore.SharedKeyUtils;
import org.talend.components.adlsgen2.input.InputConfiguration;
import org.talend.components.adlsgen2.output.OutputConfiguration;
import org.talend.components.adlsgen2.service.AdlsGen2Service;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.talend.sdk.component.junit.BaseComponentsHandler;
import org.talend.sdk.component.junit.http.api.HttpApiHandler;
import org.talend.sdk.component.junit.http.internal.impl.AzureStorageCredentialsRemovalResponseLocator;
import org.talend.sdk.component.junit.http.internal.junit5.JUnit5HttpApi;
import org.talend.sdk.component.junit.http.junit5.HttpApi;
import org.talend.sdk.component.junit.http.junit5.HttpApiInject;
import org.talend.sdk.component.junit5.Injected;
import org.talend.sdk.component.junit5.WithComponents;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@HttpApi(useSsl = true, responseLocator = AzureStorageCredentialsRemovalResponseLocator.class)
@WithComponents("org.talend.components.adlsgen2")
public class AdlsGen2TestBase implements Serializable {

    @org.junit.ClassRule
    public static final JUnit5HttpApi API = new JUnit5HttpApi().activeSsl();

    public static String accountName;

    public static String storageFs;

    public static String accountKey;

    public static String clientId;

    public static String clientSecret;

    public static String tenantId;

    public static String sas;

    static {
        Properties prop = new Properties();
        java.io.InputStream input = null;
        try {
            input = new FileInputStream(System.getenv("HOME") + "/azure.properties");
            prop.load(input);
            // System.setProperties(prop);
            for (String name : prop.stringPropertyNames()) {
                System.setProperty(name, prop.getProperty(name));
            }
        } catch (java.io.IOException ex) {
            System.err.println("Did not find azure properties, you can still pass them with -D");
        }
        accountName = System.getProperty("adlsgen2.accountName", "undxgen2");
        storageFs = System.getProperty("adlsgen2.storageFs", "adls-gen2");
        accountKey = System.getProperty("adlsgen2.accountKey", "ZZZ_KEY");
        clientId = System.getProperty("adlsgen2.clientId", "undx");
        clientSecret = System.getProperty("adlsgen2.clientSecret", "secret");
        tenantId = System.getProperty("adlsgen2.tenantId", "talendId");
        sas = System.getProperty("adlsgen2.sas", "ZZZ_SAS");

        System.setProperty("talend.junit.http.capture", "true");
    }

    @Injected
    protected BaseComponentsHandler components;

    @Service
    protected RecordBuilderFactory recordBuilderFactory;

    @Service
    protected AdlsGen2Service service;

    protected SharedKeyUtils utils;

    protected AdlsGen2Connection connection;

    protected AdlsGen2DataSet dataSet;

    protected InputConfiguration inputConfiguration;

    protected OutputConfiguration outputConfiguration;

    @HttpApiInject()
    private HttpApiHandler<?> handler;

    @BeforeEach
    void setUp() {
        service = new AdlsGen2Service();

        connection = new AdlsGen2Connection();
        connection.setAuthMethod(AuthMethod.SAS);
        connection.setTenantId(tenantId);
        connection.setAccountName(accountName);
        connection.setSharedKey(accountKey);
        connection.setClientId(clientId);
        connection.setClientSecret(clientSecret);
        connection.setSas(sas);

        dataSet = new AdlsGen2DataSet();
        dataSet.setConnection(connection);
        dataSet.setFilesystem(storageFs);
        dataSet.setBlobPath("myNewFolder/customer_20190325.csv");

        dataSet.setFormat(FileFormat.CSV);
        CsvConfiguration csvConfig = new CsvConfiguration();
        csvConfig.setFieldDelimiter(CsvFieldDelimiter.SEMICOLON);
        csvConfig.setRecordDelimiter(CsvRecordSeparator.LF);
        csvConfig.setCsvSchema("id;firstname;lastname;address;enrolled;zip;state");
        dataSet.setCsvConfiguration(csvConfig);

        inputConfiguration = new InputConfiguration();
        inputConfiguration.setDataSet(dataSet);

        outputConfiguration = new OutputConfiguration();
        outputConfiguration.setDataSet(dataSet);

        // inject needed services
        components.injectServices(UnknownConverter.of());
        components.injectServices(CsvConverter.class);
        components.injectServices(CsvConverter.of());
        CsvConverter.recordBuilderFactory = components.findService(RecordBuilderFactory.class);
    }

    protected Record createData() {
        Record record = recordBuilderFactory.newRecordBuilder() //
                .withString("id", "1") //
                .withString("firstname", "firstfirst") //
                .withString("lastname", "lastlast") //
                .withString("address", "addressaddr") //
                .withString("enrolled", "Datedsldsk") //
                .withString("zip", "89100") //
                .withString("state", "YO") //
                .build();

        return record;
    }

}