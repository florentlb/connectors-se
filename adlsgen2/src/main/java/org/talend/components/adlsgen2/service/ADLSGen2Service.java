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
package org.talend.components.adlsgen2.service;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.talend.components.adlsgen2.dataset.ADLSGen2DataSet;
import org.talend.components.adlsgen2.datastore.ADLSGen2Connection;
import org.talend.components.adlsgen2.datastore.Constants;
import org.talend.components.adlsgen2.datastore.Constants.HeaderConstants;
import org.talend.components.adlsgen2.datastore.SharedKeyUtils;
import org.talend.components.adlsgen2.input.InputConfiguration;
import org.talend.components.adlsgen2.output.OutputConfiguration;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Record.Builder;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.configuration.Configuration;
import org.talend.sdk.component.api.service.http.Response;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import com.csvreader.CsvReader;
import com.google.common.base.Splitter;
import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpMethod;
import com.microsoft.rest.v2.http.HttpRequest;

@Slf4j
@Service
public class ADLSGen2Service implements Serializable {

    // reflexion hack to support PATCH method.
    static {
        SupportPatch.allowMethods("PATCH");
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    @Service
    JsonBuilderFactory jsonFactory;

    @Service
    RecordBuilderFactory recordBuilder;

    @Service
    ADLSGen2APIClient client;

    @Service
    AccessTokenProvider accessTokenProvider;

    private transient String auth;

    private transient String sas;

    private transient Map<String, String> sasMap;

    private static final Set<Integer> successfulOperations = new HashSet<>(Arrays.asList(Constants.HTTP_RESPONSE_CODE_200_OK,
            Constants.HTTP_RESPONSE_CODE_201_CREATED, Constants.HTTP_RESPONSE_CODE_202_ACCEPTED));

    public ADLSGen2APIClient getClient(@Configuration("connection") final ADLSGen2Connection connection) {
        log.warn("[getClient] setting base url {}", connection.apiUrl());
        client.base(connection.apiUrl());
        return client;
    }

    public void preprareRequest(@Configuration("connection") final ADLSGen2Connection connection) {
        client.base(connection.apiUrl());
        auth = "";
        switch (connection.getAuthMethod()) {
        case SharedKey:
            try {
                String now = Constants.RFC1123GMTDateFormatter.format(OffsetDateTime.now());
                String version = HeaderConstants.TARGET_STORAGE_VERSION;
                String contentType = HeaderConstants.DFS_CONTENT_TYPE;
                URL url = new URL(connection.apiUrl());
                Map<String, String> heads = new HashMap<>();
                heads.put(Constants.HeaderConstants.DATE, now);
                heads.put(HeaderConstants.CONTENT_TYPE, contentType);
                heads.put(HeaderConstants.VERSION, version);
                HttpHeaders headers = new HttpHeaders(heads);
                HttpRequest request = new HttpRequest(null, HttpMethod.GET, url, headers, null, null);
                auth = new SharedKeyUtils(connection.getAccountName(), connection.getSharedKey())
                        .buildAuthenticationSignature(request);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            break;
        case AccessToken:
            String token = getAccessToken(connection);
            auth = String.format(HeaderConstants.AUTH_BEARER, token);
            break;
        case SAS:
            sas = connection.getSas().substring(1);
            auth = String.format(HeaderConstants.AUTH_SHARED_ACCESS_SIGNATURE, sas);
            sasMap = Splitter.on("&").withKeyValueSeparator("=").split(sas);
            break;
        case ADAL:
            throw new UnsupportedOperationException("Authentication method unsupported");
        }
    }

    public Response<JsonObject> handleResponse(Response<JsonObject> response) {
        log.info("[handleResponse] response:[{}] {}.", response.status(), response.headers());
        if (successfulOperations.contains(response.status())) {
            return response;
        } else {
            StringBuilder sb = new StringBuilder("[" + response.status() + "] ");
            List<String> errors = response.headers().get(HeaderConstants.HEADER_X_MS_ERROR_CODE);
            if (errors != null && errors.size() > 0) {
                for (String error : errors) {
                    sb.append(error);
                    if (ApiErrors.valueOf(error) != null) {
                        sb.append(": " + ApiErrors.valueOf(error));
                    }
                    sb.append("\n");
                }
            }
            log.error("[handleResponse] {}", sb.toString());
            throw new RuntimeException(sb.toString());
        }
    }

    public List<Record> extractCSVRecords(@Configuration("dataSet") final ADLSGen2DataSet dataSet, String content) {
        List<Record> records = new ArrayList<>();
        char delimiter = dataSet.getFieldDelimiter().getDelimiterChar();
        CsvReader csvReader = new CsvReader(new StringReader(content), delimiter);
        String[] columns = dataSet.getCsvSchema().split(dataSet.getFieldDelimiter().getDelimiter());
        log.debug("[extractCSVRecords][{}] cols: {}", delimiter, columns);
        Builder record;
        try {
            if (dataSet.isHeader()) {
                csvReader.readHeaders();
            } else {
                csvReader.setHeaders(columns);
            }
            while (csvReader.readRecord()) {
                record = recordBuilder.newRecordBuilder();
                for (String column : columns) {
                    try {
                        record.withString(column, csvReader.get(column));
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
                records.add(record.build());
            }
        } catch (IOException e) {
        }
        log.warn("[extractCSVRecords] record count: {}. sample: {}.", records.size(), records.get(10));
        return records;
    }

    public List<Record> convertToRecordList(@Configuration("dataSet") final ADLSGen2DataSet dataSet, Object content) {
        log.warn("[convertToRecordList] type: {}", content.getClass().getName());
        switch (dataSet.getFormat()) {
        case CSV:
            return extractCSVRecords(dataSet, (String) content);
        case AVRO:
        case JSON:
        case PARQUET:
            throw new IllegalArgumentException("Not implemented");
        }
        return null;
    }

    public String getAccessToken(@Configuration("connection") final ADLSGen2Connection connection) {
        accessTokenProvider.base(connection.oauthUrl());
        String payload = String.format(Constants.TOKEN_FORM, connection.getClientId(), connection.getClientSecret());
        Response<JsonObject> token = handleResponse(accessTokenProvider.getAccessToken(connection.getTenantId(), payload));
        return token.body().getString(Constants.ATTR_ACCESS_TOKEN);
    }

    public List<String> filesystemList(@Configuration("connection") final ADLSGen2Connection connection) {
        preprareRequest(connection);
        Response<JsonObject> result = handleResponse(client.filesystemList(connection, auth, sasMap, Constants.ATTR_ACCOUNT));
        List<String> fs = new ArrayList<>();
        for (JsonValue v : result.body().getJsonArray(Constants.ATTR_FILESYSTEMS)) {
            fs.add(v.asJsonObject().getString(Constants.ATTR_NAME));
        }
        log.info("fs: {}", fs);
        return fs;
    }

    public JsonArray pathList(@Configuration("configuration") final InputConfiguration configuration) {
        preprareRequest(configuration.getDataSet().getConnection());
        Response<JsonObject> result = handleResponse(client.pathList( //
                configuration.getDataSet().getConnection(), //
                auth, //
                configuration.getDataSet().getFilesystem(), //
                sasMap, //
                configuration.getDataSet().getBlobPath(), //
                Constants.ATTR_FILESYSTEM, //
                true, //
                null, //
                5000, //
                "", //
                60 //
        ));
        return result.body().getJsonArray(Constants.ATTR_PATHS);
    }

    public String extractFolderPath(String blobPath) {
        Path path = Paths.get(blobPath);
        log.debug("[extractFolderPath] blobPath: {}. Path: {}. {}", blobPath, path.toString(), path.getNameCount());
        if (path.getNameCount() == 1) {
            return "/";
        }
        return Paths.get(blobPath).getParent().toString();
    }

    public String extractFileName(String blobPath) {
        Path path = Paths.get(blobPath);
        log.debug("[extractFileName] blobPath: {}. Path: {}. {}", blobPath, path.toString(), path.getNameCount());
        return Paths.get(blobPath).getFileName().toString();
    }

    public Map<String, String> pathGetProperties(@Configuration("dataSet") final ADLSGen2DataSet dataSet) {
        preprareRequest(dataSet.getConnection());
        Map<String, String> properties = new HashMap<>();
        Response<JsonObject> result = handleResponse(client.pathGetProperties( //
                dataSet.getConnection(), //
                auth, //
                dataSet.getFilesystem(), //
                dataSet.getBlobPath(), //
                sasMap //
        ));
        log.warn("[pathGetProperties] [{}] {}.\n{}", result.status(), result.headers());
        if (result.status() == 200) {
            for (String header : result.headers().keySet()) {
                if (header.startsWith(Constants.PREFIX_FOR_STORAGE_HEADER)) {
                    properties.put(header, result.headers().get(header).toString());
                }
            }
        }
        return properties;
    }

    @Data
    @ToString
    class BlobInformations {

        // {"contentLength":"21","etag":"Mon, 25 Mar 2019 15:35:47 GMT","group":"$superuser","lastModified":"Mon, 25 Mar 2019
        // 15:35:47 GMT","name":"myNewFolder/customer.csv","owner":"$superuser","permissions":"rw-r-----"}
        private Boolean exists = Boolean.FALSE;

        private String path;

        private String fileName;

        private Integer contentLength = 0;

        public String etag;

        public String group;

        public String lastModified;

        public String name;

        public String owner;

        public String permissions;
    }

    public BlobInformations getBlobInformations(@Configuration("dataSet") final ADLSGen2DataSet dataSet) {
        preprareRequest(dataSet.getConnection());
        BlobInformations infos = new BlobInformations();
        Response<JsonObject> result = client.pathList( //
                dataSet.getConnection(), //
                auth, //
                dataSet.getFilesystem(), //
                sasMap, //
                extractFolderPath(dataSet.getBlobPath()), //
                // dataSet.getBlobPath(), //
                Constants.ATTR_FILESYSTEM, //
                false, //
                null, //
                5000, //
                "", //
                60 //
        );
        log.warn("[pathExists] [{}] {}.\n{}", result.status(), result.headers(), result.body());
        if (result.status() != Constants.HTTP_RESPONSE_CODE_200_OK) {
            return infos;
        }
        String fileName = extractFileName(dataSet.getBlobPath());
        for (JsonValue f : result.body().getJsonArray(Constants.ATTR_PATHS)) {
            log.info("[pathExists] => {}.", f.asJsonObject().getString(Constants.ATTR_NAME));
            if (f.asJsonObject().getString(Constants.ATTR_NAME).equals(dataSet.getBlobPath())) {
                infos.setExists(true);
                infos.setName(f.asJsonObject().getString(Constants.ATTR_NAME));
                infos.setFileName(fileName);
                infos.setPath(extractFolderPath(dataSet.getBlobPath()));
                infos.setEtag(f.asJsonObject().getString("etag"));
                infos.setContentLength(Integer.parseInt(f.asJsonObject().getString("contentLength")));
                infos.setLastModified(f.asJsonObject().getString("lastModified"));
                infos.setOwner(f.asJsonObject().getString("owner"));
                infos.setPermissions(f.asJsonObject().getString("permissions"));
            }
        }

        return infos;
    }

    public Boolean pathExists(@Configuration("dataSet") final ADLSGen2DataSet dataSet) {
        return getBlobInformations(dataSet).getExists();
    }

    public List<Record> pathRead(@Configuration("configuration") final InputConfiguration configuration) {
        preprareRequest(configuration.getDataSet().getConnection());
        Response<JsonObject> result = handleResponse(client.pathRead( //
                configuration.getDataSet().getConnection(), //
                auth, //
                configuration.getDataSet().getFilesystem(), //
                configuration.getDataSet().getBlobPath(), //
                60, //
                sasMap //
        ));
        log.warn("[pathRead] [{}] {}.", result.status(), result.headers());
        return convertToRecordList(configuration.getDataSet(), result.body());
    }

    public Response<JsonObject> pathCreate(@Configuration("configuration") final OutputConfiguration configuration) {
        preprareRequest(configuration.getDataSet().getConnection());
        Response<JsonObject> result = handleResponse(client.pathCreate( //
                configuration.getDataSet().getConnection(), //
                auth, //
                configuration.getDataSet().getFilesystem(), //
                configuration.getDataSet().getBlobPath(), //
                Constants.ATTR_FILE, //
                sasMap, //
                ""));
        log.warn("[pathCreate] [{}] {}.\n{}", result.status(), result.headers(), result.body());
        return result;
    }

    public Response<JsonObject> pathUpdate(@Configuration("configuration") final OutputConfiguration configuration,
            String content) {
        preprareRequest(configuration.getDataSet().getConnection());
        BlobInformations blob = getBlobInformations(configuration.getDataSet());
        int position = blob.getContentLength();
        log.warn("[pathUpdate]blob: {}.", blob);
        if (configuration.isOverwrite() || !blob.getExists()) {
            pathCreate(configuration);
            position = 0;
        }

        Response<JsonObject> result = handleResponse(client.pathUpdate( //
                configuration.getDataSet().getConnection(), //
                auth, //
                configuration.getDataSet().getFilesystem(), //
                configuration.getDataSet().getBlobPath(), //
                Constants.ATTR_ACTION_APPEND, //
                position, //
                sasMap, //
                content //
        ));

        log.warn("[pathUpdate::update] [{}] {}", result.status(), result.headers());

        position += content.length();
        /*
         * To flush, the previously uploaded data must be contiguous, the position parameter must be specified and equal to the
         * length of the file after all data has been written, and there must not be a request entity body included with the
         * request.
         */
        result = handleResponse(client.pathUpdate( //
                configuration.getDataSet().getConnection(), //
                auth, //
                configuration.getDataSet().getFilesystem(), //
                configuration.getDataSet().getBlobPath(), //
                Constants.ATTR_ACTION_FLUSH, //
                position, //
                sasMap, //
                "" //
        ));

        log.warn("[pathUpdate::flush] [{}] {}", result.status(), result.headers());

        return result;
    }
}
