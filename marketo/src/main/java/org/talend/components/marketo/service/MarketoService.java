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
package org.talend.components.marketo.service;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.JsonWriterFactory;

import org.talend.components.marketo.dataset.MarketoDataSet;
import org.talend.components.marketo.dataset.MarketoDataSet.MarketoEntity;
import org.talend.components.marketo.dataset.MarketoInputConfiguration;
import org.talend.components.marketo.datastore.MarketoDataStore;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.record.Schema.Builder;
import org.talend.sdk.component.api.record.Schema.Entry;
import org.talend.sdk.component.api.record.Schema.Type;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.configuration.Configuration;
import org.talend.sdk.component.api.service.http.Response;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.joining;
import static javax.json.JsonValue.ValueType.NULL;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_ACCESS_TOKEN;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_ACTIVITY_DATE;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_ACTIVITY_TYPE_ID;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_ATTRIBUTES;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_CAMPAIGN_ID;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_CREATED_AT;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_FIELDS;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_ID;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_LEAD_ID;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_MARKETO_GUID;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_NAME;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_PRIMARY_ATTRIBUTE_VALUE;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_PRIMARY_ATTRIBUTE_VALUE_ID;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_REASONS;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_RESULT;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_SEQ;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_STATUS;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_UPDATED_AT;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_WORKSPACE_NAME;
import static org.talend.components.marketo.service.AuthorizationClient.CLIENT_CREDENTIALS;

@Accessors
@Slf4j
@Service
public class MarketoService {

    protected static final String DATETIME = "datetime";

    @Getter
    @Service
    protected I18nMessage i18n;

    @Getter
    @Service
    protected RecordBuilderFactory recordBuilder;

    @Getter
    @Service
    private JsonBuilderFactory jsonFactory;

    @Getter
    @Service
    private JsonReaderFactory jsonReader;

    @Getter
    @Service
    private JsonWriterFactory jsonWriter;

    @Getter
    @Service
    protected AuthorizationClient authorizationClient;

    @Getter
    @Service
    protected LeadClient leadClient;

    @Getter
    @Service
    protected CustomObjectClient customObjectClient;

    @Getter
    @Service
    protected CompanyClient companyClient;

    @Getter
    @Service
    protected OpportunityClient opportunityClient;

    @Getter
    @Service
    protected ListClient listClient;

    public void initClients(MarketoDataStore dataStore) {
        authorizationClient.base(dataStore.getEndpoint());
        leadClient.base(dataStore.getEndpoint());
        listClient.base(dataStore.getEndpoint());
        customObjectClient.base(dataStore.getEndpoint());
        companyClient.base(dataStore.getEndpoint());
        opportunityClient.base(dataStore.getEndpoint());
    }

    /**
     * Retrieve an set an access token for using API
     */
    public String retrieveAccessToken(@Configuration("configuration") final MarketoDataSet dataSet) {
        initClients(dataSet.getDataStore());
        Response<JsonObject> result = authorizationClient.getAuthorizationToken(CLIENT_CREDENTIALS,
                dataSet.getDataStore().getClientId(), dataSet.getDataStore().getClientSecret());
        log.debug("[retrieveAccessToken] [{}] : {}.", result.status(), result.body());
        if (result.status() == 200) {
            return result.body().getString(ATTR_ACCESS_TOKEN);
        } else {
            String error = i18n.accessTokenRetrievalError(result.status(), result.headers().toString());
            log.error("[retrieveAccessToken] {}", error);
            throw new RuntimeException(error);
        }
    }

    public String getFieldsFromDescribeFormatedForApi(JsonArray fields) {
        List<String> result = new ArrayList<>();
        for (JsonObject field : fields.getValuesAs(JsonObject.class)) {
            if (field.getJsonObject("rest") != null) {
                result.add(field.getJsonObject("rest").getString(ATTR_NAME));
            } else {
                result.add(field.getString(ATTR_NAME));
            }
        }
        return result.stream().collect(joining(","));
    }

    protected JsonArray parseResultFromResponse(Response<JsonObject> response) {
        if (response.status() == 200 && response.body() != null && response.body().getJsonArray(ATTR_RESULT) != null) {
            return response.body().getJsonArray(ATTR_RESULT);
        }
        log.error("[parseResultFromResponse] Error: [{}] headers:{}; body: {}.", response.status(), response.headers(),
                response.body());
        throw new IllegalArgumentException(i18n.invalidOperation());
    }

    public Schema getEntitySchema(final MarketoInputConfiguration configuration) {
        log.debug("[getEntitySchema] {} ", configuration);
        return getEntitySchema(configuration.getDataSet().getDataStore(), configuration.getDataSet().getEntity().name(), "", "");
    }

    public Schema getEntitySchema(final MarketoDataStore dataStore, final String entity, final String customObjectName,
            final String listAction) {
        log.debug("[getEntitySchema] {} - {} - {}- {}", dataStore, entity, customObjectName, listAction);
        try {
            initClients(dataStore);
            String accessToken = authorizationClient.getAccessToken(dataStore);
            JsonArray entitySchema = null;
            switch (MarketoEntity.valueOf(entity)) {
            case Lead:
                entitySchema = parseResultFromResponse(leadClient.describeLead(accessToken));
                return getSchemaForEntity(entitySchema);
            // case List:
            // if (ListAction.getLeads.name().equals(listAction)) {
            // entitySchema = parseResultFromResponse(leadClient.describeLead(accessToken));
            // } else {
            // return getInputSchema(MarketoEntity.List, listAction);
            // }
            // break;
            // case CustomObject:
            // entitySchema = parseResultFromResponse(customObjectClient.describeCustomObjects(accessToken, customObjectName))
            // .get(0).asJsonObject().getJsonArray(ATTR_FIELDS);
            // break;
            // case Company:
            // entitySchema = parseResultFromResponse(companyClient.describeCompanies(accessToken)).get(0).asJsonObject()
            // .getJsonArray(ATTR_FIELDS);
            // break;
            // case Opportunity:
            // entitySchema = parseResultFromResponse(opportunityClient.describeOpportunity(accessToken)).get(0).asJsonObject()
            // .getJsonArray(ATTR_FIELDS);
            // break;
            // case OpportunityRole:
            // entitySchema = parseResultFromResponse(opportunityClient.describeOpportunityRole(accessToken)).get(0)
            // .asJsonObject().getJsonArray(ATTR_FIELDS);
            // break;
            }
            log.debug("[getEntitySchema] entitySchema: {}.", entitySchema);
            return getSchemaForEntity(entitySchema);
        } catch (Exception e) {
            log.error(i18n.exceptionOccured(e.getMessage()));
        }
        return null;
    }

    private Schema mergeSchemas(Schema first, Schema second) {
        Builder b = recordBuilder.newSchemaBuilder(Type.RECORD);
        first.getEntries().forEach(b::withEntry);
        second.getEntries().forEach(b::withEntry);
        return b.build();
    }

    protected Schema getSchemaForEntity(JsonArray entitySchema) {
        List<Entry> entries = new ArrayList<>();
        for (JsonObject field : entitySchema.getValuesAs(JsonObject.class)) {
            String entryName;
            Schema.Type entryType;
            String entityComment;
            if (field.getJsonObject("rest") != null) {
                entryName = field.getJsonObject("rest").getString(ATTR_NAME);
            } else {
                entryName = field.getString(ATTR_NAME);
            }
            String dataType = field.getString("dataType", "string");
            entityComment = dataType;
            switch (dataType) {
            case ("string"):
            case ("text"):
            case ("phone"):
            case ("email"):
            case ("url"):
            case ("lead_function"):
            case ("reference"):
                entryType = Schema.Type.STRING;
                break;
            case ("percent"):
            case ("score"):
            case ("integer"):
                entryType = Schema.Type.INT;
                break;
            case ("checkbox"):
            case ("boolean"):
                entryType = Schema.Type.BOOLEAN;
                break;
            case ("float"):
            case ("currency"):
                entryType = Type.FLOAT;
                break;
            case ("date"):
                /*
                 * Used for date. Follows W3C format. 2010-05-07
                 */
            case DATETIME:
                /*
                 * Used for a date & time. Follows W3C format (ISO 8601). The best practice is to always include the time zone
                 * offset.
                 * Complete date plus hours and minutes:
                 *
                 * YYYY-MM-DDThh:mmTZD
                 *
                 * where TZD is “+hh:mm” or “-hh:mm”
                 */
                entryType = Type.DATETIME;
                break;
            default:
                log.warn(i18n.nonManagedType(dataType, entryName));
                entryType = Schema.Type.STRING;
            }
            entries.add(
                    recordBuilder.newEntryBuilder().withName(entryName).withType(entryType).withComment(entityComment).build());
        }
        Builder b = recordBuilder.newSchemaBuilder(Schema.Type.RECORD);
        entries.forEach(b::withEntry);
        return b.build();
    }

    // TODO this is not the correct defaults schemas!!!
    public Schema getInputSchema(MarketoEntity entity, String action) {
        switch (entity) {
        case Lead:
            // case List:
            switch (action) {
            case "isMemberOfList":
                return getLeadListDefaultSchema();
            case "list":
            case "get":
                return getListGetDefaultSchema();
            default:
                return getLeadListDefaultSchema();
            }
            // TODO reenable after
            // case CustomObject:
            // case Company:
            // case Opportunity:
            // case OpportunityRole:
            // return getCustomObjectDefaultSchema();
        }
        return null;
    }

    public JsonObject toJson(final Record record) {
        String recordStr = record.toString().replaceAll("AvroRecord\\{delegate=(.*)\\}$", "$1");
        JsonReader reader = jsonReader.createReader(new StringReader(recordStr));
        Throwable throwable = null;
        JsonObject json;
        try {
            json = reader.readObject();
        } catch (Throwable throwable1) {
            throwable = throwable1;
            throw throwable1;
        } finally {
            if (reader != null) {
                if (throwable != null) {
                    try {
                        reader.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                } else {
                    reader.close();
                }
            }
        }
        return json;
    }

    public Record convertToRecord(final JsonObject json, final Map<String, Entry> schema) {
        log.debug("[convertToRecord] json: {} with schema: {}.", json, schema);
        Record.Builder b = getRecordBuilder().newRecordBuilder();
        java.util.Set<java.util.Map.Entry<String, JsonValue>> props = json.entrySet();// new HashSet<>();
        // props.addAll(json.entrySet());
        // TODO check if json is not missing a value in master schema. Otherwise this will lead to a
        // ArrayIndexOutOfBoundsException
        log.warn("[convertToRecord] {} vs {}", json.entrySet().size(), schema.keySet().size());
        // if (json.entryset().size() < schema.keyset().size()) {
        // schema.keyset().stream().filter(s -> json.get(s) == null).foreach(s -> {
        // schema.entry ent = schema.get(s);
        // log.warn("[converttorecord] adding schema column : {}.", ent.getname());
        // props.add(new abstractmap.simpleentry<>(ent.getname(), jsonvalue.null));
        // });
        // }
        for (java.util.Map.Entry<String, JsonValue> jsonEntry : props) {
            String jsonKey = jsonEntry.getKey();
            Schema.Entry schemaEntry = schema.get(jsonKey);
            log.warn("schema key {} entry : {}.", jsonKey, schemaEntry);
            Type type = null;
            ValueType jsonValueType = jsonEntry.getValue().getValueType();
            if (schemaEntry == null) {
                type = Type.STRING;
                switch (jsonValueType) {
                case NUMBER:
                    type = ((JsonNumber) jsonEntry.getValue()).isIntegral() ? Type.LONG : Type.DOUBLE;
                    break;
                case TRUE:
                case FALSE:
                    type = Type.BOOLEAN;
                    break;
                case ARRAY:
                    type = Type.ARRAY;
                    break;
                case NULL:
                case OBJECT:
                case STRING:
                    type = Type.STRING;
                    break;
                }
            } else {
                type = schemaEntry.getType();
                if (Type.LONG.equals(type) && DATETIME.equals(schemaEntry.getComment())) {
                    type = Type.DATETIME;
                }
            }
            log.debug("[convertToRecord] {} - {} ({})", jsonEntry, schemaEntry, jsonValueType);
            switch (type) {
            case STRING:
                switch (jsonValueType) {
                case ARRAY:
                    b.withString(jsonKey, json.getJsonArray(jsonKey).stream().map(JsonValue::toString).collect(joining(",")));
                    break;
                case OBJECT:
                    b.withString(jsonKey, String.valueOf(json.getJsonObject(jsonKey).toString()));
                    break;
                case STRING:
                    b.withString(jsonKey, json.getString(jsonKey));
                    break;
                case NUMBER:
                    if (json.getJsonNumber(jsonKey).isIntegral()) {
                        b.withLong(jsonKey, json.getJsonNumber(jsonKey).longValue());
                    } else {
                        b.withDouble(jsonKey, json.getJsonNumber(jsonKey).doubleValue());
                    }
                    break;
                case TRUE:
                case FALSE:
                    b.withBoolean(jsonKey, json.getBoolean(jsonKey));
                    break;
                case NULL:
                    b.withString(jsonKey, null);
                    break;
                }
                break;
            case INT:
                b.withInt(jsonKey, jsonValueType.equals(NULL) ? 0 : json.getInt(jsonKey));
                break;
            case LONG:
                b.withLong(jsonKey, jsonValueType.equals(NULL) ? 0 : json.getJsonNumber(jsonKey).longValue());
                break;
            case FLOAT:
                b.withFloat(jsonKey, jsonValueType.equals(NULL) ? 0 : Float.valueOf(json.getString(jsonKey)));
                break;
            case DOUBLE:
                b.withDouble(jsonKey, jsonValueType.equals(NULL) ? 0 : json.getJsonNumber(jsonKey).doubleValue());
                break;
            case BOOLEAN:
                b.withBoolean(jsonKey, jsonValueType.equals(NULL) ? false : json.getBoolean(jsonKey));
                break;
            case DATETIME:
                try {
                    b.withDateTime(jsonKey, jsonValueType.equals(NULL) ? null
                            : new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(json.getString(jsonKey)));
                } catch (ParseException e1) {
                    log.error("[convertToRecord] Date parsing error: {}.", e1.getMessage());
                }
                break;
            case ARRAY:
                String ary = json.getJsonArray(jsonKey).stream().map(JsonValue::toString).collect(joining(","));
                // not in a sub array
                if (!ary.contains("{")) {
                    ary = ary.replaceAll("\"", "").replaceAll("(\\[|\\])", "");
                }
                b.withString(jsonKey, ary);
                break;
            case BYTES:
            case RECORD:
                b.withString(jsonKey, json.getString(jsonKey));
            }
        }
        Record record = b.build();
        log.warn("[convertToRecord] returning : {}. (schema: {}).", record, record.getSchema());
        return record;
    }

    Schema getLeadChangesAndActivitiesSchema() {
        return recordBuilder.newSchemaBuilder(Type.RECORD)
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_ACTIVITY_DATE).withType(Type.STRING)
                        .withComment(DATETIME).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_ACTIVITY_TYPE_ID).withType(Type.INT).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_ATTRIBUTES).withType(Type.STRING).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_FIELDS).withType(Type.STRING).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_CAMPAIGN_ID).withType(Type.INT).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_LEAD_ID).withType(Type.INT).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_MARKETO_GUID).withType(Type.STRING).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_PRIMARY_ATTRIBUTE_VALUE).withType(Type.STRING).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_PRIMARY_ATTRIBUTE_VALUE_ID).withType(Type.INT).build())
                //
                .build();
    }

    Schema getLeadListDefaultSchema() {
        return recordBuilder.newSchemaBuilder(Schema.Type.RECORD)
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_ID).withType(Schema.Type.INT).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_STATUS).withType(Schema.Type.STRING).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_REASONS).withType(Schema.Type.STRING).build()).build();
    }

    Schema getListGetDefaultSchema() {
        return recordBuilder.newSchemaBuilder(Schema.Type.RECORD)
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_ID).withType(Schema.Type.INT).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_NAME).withType(Schema.Type.STRING).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_WORKSPACE_NAME).withType(Schema.Type.STRING).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_CREATED_AT).withType(Schema.Type.STRING).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_UPDATED_AT).withType(Schema.Type.STRING).build())
                .build();
    }

    Schema getCustomObjectDefaultSchema() {
        return recordBuilder.newSchemaBuilder(Schema.Type.RECORD)
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_SEQ).withType(Schema.Type.INT).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_MARKETO_GUID).withType(Schema.Type.STRING).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_STATUS).withType(Schema.Type.STRING).build())
                .withEntry(recordBuilder.newEntryBuilder().withName(ATTR_REASONS).withType(Schema.Type.STRING).build()).build();
    }

}
