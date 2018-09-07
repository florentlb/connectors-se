package org.talend.components.netsuite.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.components.netsuite.dataset.NetSuiteCommonDataSet;
import org.talend.components.netsuite.dataset.NetsuiteInputDataSet;
import org.talend.components.netsuite.dataset.NetsuiteOutputDataSet;
import org.talend.components.netsuite.datastore.NetsuiteDataStore;
import org.talend.components.netsuite.datastore.NetsuiteDataStore.ApiVersion;
import org.talend.components.netsuite.datastore.NetsuiteDataStore.LoginType;
import org.talend.components.netsuite.runtime.model.search.SearchFieldOperatorType;
import org.talend.components.netsuite.runtime.v2018_2.model.RecordTypeEnum;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.SuggestionValues;
import org.talend.sdk.component.api.service.completion.SuggestionValues.Item;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;
import org.talend.sdk.component.api.service.schema.Schema;
import org.talend.sdk.component.junit5.WithComponents;
import org.talend.sdk.component.maven.MavenDecrypter;
import org.talend.sdk.component.maven.Server;

import com.netsuite.webservices.v2018_2.lists.accounting.Account;
import com.netsuite.webservices.v2018_2.platform.common.AccountSearchBasic;
import com.netsuite.webservices.v2018_2.platform.common.CustomRecordSearchBasic;
import com.netsuite.webservices.v2018_2.platform.common.TransactionSearchBasic;
import com.netsuite.webservices.v2018_2.transactions.purchases.PurchaseOrder;

@WithComponents("org.talend.components.netsuite")
public class UIActionServiceTest {

    @Service
    private UIActionService uiActionService;

    private static Messages i18n;

    private static NetsuiteDataStore dataStore;

    @BeforeAll
    public static void setupDataStore() {

        final MavenDecrypter decrypter = new MavenDecrypter();
        Server consumer = decrypter.find("netsuite.consumer");
        Server token = decrypter.find("netsuite.token");
        dataStore = new NetsuiteDataStore();
        dataStore.setEnableCustomization(false);
        dataStore.setAccount(System.getProperty("netsuite.account"));
        dataStore.setEndpoint(System.getProperty("netsuite.endpoint.url"));
        dataStore.setLoginType(LoginType.TBA);
        dataStore.setConsumerKey(consumer.getUsername());
        dataStore.setConsumerSecret(consumer.getPassword());
        dataStore.setTokenId(token.getUsername());
        dataStore.setTokenSecret(token.getPassword());
        i18n = new Messages() {

            @Override
            public String warnBatchTimeout() {
                return null;
            }

            @Override
            public String healthCheckOk() {
                return "Connection is succesful";
            }

            @Override
            public String healthCheckFailed(String cause) {
                return "Failed to connect";
            }
        };
    }

    @BeforeEach
    public void refresh() {
        dataStore.setEnableCustomization(false);
    }

    @Test
    public void testHealthCheck() {
        assertEquals(HealthCheckStatus.Status.OK, uiActionService.validateConnection(dataStore, i18n, null).getStatus());
    }

    @Test
    public void testHealthCheckFailed() {

        NetsuiteDataStore dataStoreWrong = new NetsuiteDataStore();
        dataStoreWrong.setLoginType(LoginType.BASIC);
        dataStoreWrong.setRole(3);
        dataStoreWrong.setAccount(System.getProperty("netsuite.account"));
        dataStoreWrong.setEndpoint(System.getProperty("netsuite.endpoint.url"));
        dataStoreWrong.setEmail("test_junit@talend.com");
        dataStoreWrong.setPassword("wrongPassword");
        dataStoreWrong.setApplicationId(UUID.randomUUID().toString());
        dataStoreWrong.setApiVersion(ApiVersion.V2018_2);
        assertEquals(HealthCheckStatus.Status.KO, uiActionService.validateConnection(dataStoreWrong, i18n, null).getStatus());
    }

    @Test
    public void testGuessInputSchema() {
        List<String> expectedList = Arrays.asList(Account.class.getDeclaredFields()).stream()
                .map(field -> field.getName().toLowerCase())
                .filter(field -> !field.equals("customfieldlist") && !field.equals("nullfieldlist")).sorted()
                .collect(Collectors.toList());
        NetsuiteInputDataSet inputDataSet = new NetsuiteInputDataSet();
        NetSuiteCommonDataSet commonDataSet = new NetSuiteCommonDataSet();
        commonDataSet.setDataStore(dataStore);
        commonDataSet.setRecordType("Account");
        inputDataSet.setCommonDataSet(commonDataSet);

        uiActionService.validateConnection(dataStore, i18n, null);

        Schema schema = uiActionService.guessInputSchema(inputDataSet);
        List<String> actualList = schema.getEntries().stream().map(entry -> entry.getName().toLowerCase()).sorted()
                .collect(Collectors.toList());
        assertIterableEquals(expectedList, actualList);
    }

    @Test
    public void testGuessOutputSchema() {
        List<String> fields = Arrays.asList(PurchaseOrder.class.getDeclaredFields()).stream()
                .map(field -> field.getName().toLowerCase())
                .filter(field -> !field.equals("customfieldlist") && !field.equals("nullfieldlist")).collect(Collectors.toList());
        NetsuiteOutputDataSet outputDataSet = new NetsuiteOutputDataSet();
        NetSuiteCommonDataSet commonDataSet = new NetSuiteCommonDataSet();
        commonDataSet.setDataStore(dataStore);
        commonDataSet.setRecordType("PurchaseOrder");
        outputDataSet.setCommonDataSet(commonDataSet);
        // For custom fields need to re-create connection (might be a case for refactoring of Service)
        dataStore.setEnableCustomization(true);
        uiActionService.validateConnection(dataStore, i18n, null);

        Schema schema = uiActionService.guessOutputSchema(outputDataSet);
        // In case of customization count of entries in schema must be more than actual class fields.
        assertTrue(fields.size() < schema.getEntries().size());
    }

    @Test
    public void testLoadRecordTypes() {
        List<String> expectedList = Arrays.asList(RecordTypeEnum.values()).stream().map(RecordTypeEnum::getTypeName).sorted()
                .collect(Collectors.toList());
        uiActionService.validateConnection(dataStore, i18n, null);
        SuggestionValues values = uiActionService.loadRecordTypes(dataStore);
        List<String> actualList = values.getItems().stream().map(Item::getLabel).sorted().collect(Collectors.toList());

        assertIterableEquals(expectedList, actualList);
    }

    @Test
    public void testLoadCustomRecordTypes() {
        dataStore.setEnableCustomization(true);
        uiActionService.validateConnection(dataStore, i18n, null);
        SuggestionValues values = uiActionService.loadRecordTypes(dataStore);

        // For enabled customization we must have more record types. (even for passing other tests, they must be
        // defined)
        assertTrue(RecordTypeEnum.values().length < values.getItems().size());
    }

    @Test
    public void testLoadFields() {
        List<String> expectedList = Arrays.asList(AccountSearchBasic.class.getDeclaredFields()).stream().map(Field::getName)
                .sorted().collect(Collectors.toList());

        NetSuiteCommonDataSet commonDataSet = new NetSuiteCommonDataSet();
        commonDataSet.setDataStore(dataStore);
        commonDataSet.setRecordType("Account");

        uiActionService.validateConnection(dataStore, i18n, null);
        SuggestionValues values = uiActionService.loadFields(commonDataSet);
        List<String> actualList = values.getItems().stream().map(Item::getLabel).sorted().collect(Collectors.toList());
        assertIterableEquals(expectedList, actualList);
    }

    @Test
    public void testLoadCustomSearchFields() {
        List<String> expectedList = Arrays.asList(CustomRecordSearchBasic.class.getDeclaredFields()).stream().map(Field::getName)
                .sorted().collect(Collectors.toList());

        dataStore.setEnableCustomization(true);
        NetSuiteCommonDataSet commonDataSet = new NetSuiteCommonDataSet();
        commonDataSet.setDataStore(dataStore);
        commonDataSet.setRecordType("customrecordqacomp_custom_recordtype");

        uiActionService.validateConnection(dataStore, i18n, null);
        SuggestionValues values = uiActionService.loadFields(commonDataSet);
        List<String> actualList = values.getItems().stream().map(Item::getLabel).sorted().collect(Collectors.toList());
        assertIterableEquals(expectedList, actualList);
    }

    @Test
    public void testLoadTransactionSearchFields() {
        List<String> expectedList = Arrays.asList(TransactionSearchBasic.class.getDeclaredFields()).stream().map(Field::getName)
                .sorted().collect(Collectors.toList());

        NetSuiteCommonDataSet commonDataSet = new NetSuiteCommonDataSet();
        commonDataSet.setDataStore(dataStore);
        commonDataSet.setRecordType("PurchaseOrder");

        uiActionService.validateConnection(dataStore, i18n, null);
        SuggestionValues values = uiActionService.loadFields(commonDataSet);
        List<String> actualList = values.getItems().stream().map(Item::getLabel).sorted().collect(Collectors.toList());
        assertIterableEquals(expectedList, actualList);
    }

    @Test
    public void testLoadSearchOperator() {
        Set<String> expectedList = new TreeSet<>();
        expectedList.add("boolean");
        for (SearchFieldOperatorType type : SearchFieldOperatorType.values()) {
            if (type == SearchFieldOperatorType.BOOLEAN) {
                continue;
            }
            Field[] fields = getDeclaredFields(type.getOperatorTypeName());
            if (fields != null) {
                Arrays.stream(fields).filter(field -> (field.getModifiers() & Modifier.PRIVATE) == 0)
                        .map(field -> field.getName()).forEach(fieldName -> expectedList
                                .add(String.join(".", type.getDataType(), String.join("", fieldName.split("_"))).toLowerCase()));
            }
        }

        SuggestionValues values = uiActionService.loadOperators(dataStore);

        List<String> actualList = values.getItems().stream().map(Item::getLabel).map(String::toLowerCase).sorted()
                .collect(Collectors.toList());
        assertIterableEquals(expectedList, actualList);
    }

    private Field[] getDeclaredFields(String name) {
        try {
            return Class.forName("com.netsuite.webservices.v2018_2.platform.core.types." + name).getDeclaredFields();
        } catch (Exception e) {
            return null;
        }
    }
}
