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
package org.talend.sdk.component.junit.http.internal.impl;

import lombok.extern.slf4j.Slf4j;

import static java.util.Optional.ofNullable;

import java.util.function.Predicate;

import org.talend.components.adlsgen2.datastore.Constants.HeaderConstants;
import org.talend.sdk.component.junit.http.api.Request;

@Slf4j
public class AzureStorageCredentialsRemovalResponseLocator extends DefaultResponseLocator {

    private static final String PREFIX = "talend/testing/http/";

    private final String AZURE_FIND_SAS = ".*&sig=[^&]*.*";

    private final String AZURE_REPLACE_SAS = "&sig=[^&]*";

    private final String AZURE_SAS_REPLACEMENT = "&sig=sign-here";

    private final String AZURE_FIND_TENANT = "https://login.microsoftonline.com/.*/oauth2/v2.0/token";

    private final String AZURE_TENANT_REPLACEMENT = "https://login.microsoftonline.com/oauth2/v2.0/token";

    private final String AZURE_FIND_TOKEN = ".*access_token\":\".*";

    private final String AZURE_TOKEN_REPLACEMENT = "{\"token_type\":\"Bearer\",\"expires_in\":3600," + "\"ext_expires_in\":3600,"
            + "\"access_token"
            + "\":\"ACCESS_TOKEN_10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001"
            + "\"}";

    public AzureStorageCredentialsRemovalResponseLocator() {
        super(PREFIX, "");
    }

    @Override
    protected boolean matches(Request request, RequestModel model, boolean exact, Predicate<String> headerFilter) {
        final String method = ofNullable(model.getMethod()).orElse("GET");
        final String requestUri = request.uri();
        log.error("[AzureStorageCredentialsRemovalResponseLocator] [{}] Checking URI: {}.", requestUri);
        boolean uriMatches;
        if (requestUri.matches(AZURE_FIND_SAS)) {
            uriMatches = true;
            log.error("[AzureStorageCredentialsRemovalResponseLocator] [{}] Checking URI: {}.", uriMatches, requestUri);
        } else {
            uriMatches = requestUri.equals(model.getUri());
        }

        boolean headLineMatches = uriMatches && request.method().equalsIgnoreCase(method);
        final String payload = request.payload();
        final boolean headersMatch = doesHeadersMatch(request, model, headerFilter);
        if (headLineMatches && headersMatch && (model.getPayload() == null || model.getPayload().equals(payload))) {
            return true;
        } else if (exact) {
            return false;
        }

        if (log.isDebugEnabled()) {
            log.error("Matching test: {} for {}", request, model);
        }

        if (!headLineMatches && requestUri.contains("?")) { // strip the query
            headLineMatches = requestUri.substring(0, requestUri.indexOf('?')).equals(model.getUri())
                    && request.method().equalsIgnoreCase(method);
        }

        return headLineMatches && headersMatch && (model.getPayload() == null
                || (payload != null && (payload.matches(model.getPayload()) || payload.equals(model.getPayload()))));
    }

    @Override
    public void addModel(Model model) {
        // removing some sensitive informations in URIs
        if (model.getRequest().getUri().matches(AZURE_FIND_SAS)) {
            model.getRequest().setUri(model.getRequest().getUri().replaceAll(AZURE_REPLACE_SAS, AZURE_SAS_REPLACEMENT));
            log.debug("[addModel] removing SAS credentials [{}]: {}", model.getRequest().getMethod(),
                    model.getRequest().getUri());
        }
        if (model.getRequest().getUri().matches(AZURE_FIND_TENANT)) {
            model.getRequest().setUri(model.getRequest().getUri().replaceAll(AZURE_FIND_TENANT, AZURE_TENANT_REPLACEMENT));
            log.debug("[addModel] removing tenantID [{}]: {}", model.getRequest().getMethod(), model.getRequest().getUri());
        }
        if (model.getResponse().getPayload().matches(AZURE_FIND_TOKEN)) {
            model.getResponse().setPayload(AZURE_TOKEN_REPLACEMENT);
            log.debug("[addModel] removing access_token: {}", model.getResponse().getPayload());
        }
        // remove some sensitive headers
        if (model.getRequest().getHeaders().get(HeaderConstants.AUTHORIZATION) != null) {
            model.getRequest().getHeaders().put(HeaderConstants.AUTHORIZATION, "SharedAccessKey user:kiki");
        }
        if (model.getResponse().getHeaders().get("Set-Cookie") != null) {
            model.getResponse().getHeaders().put("Set-Cookie", "IWantMyCookie");
        }

        getCapturingBuffer().add(model);
    }
}
