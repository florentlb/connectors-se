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

import javax.json.JsonObject;

import org.talend.sdk.component.api.service.http.HttpClient;
import org.talend.sdk.component.api.service.http.Path;
import org.talend.sdk.component.api.service.http.Request;
import org.talend.sdk.component.api.service.http.Response;
import org.talend.sdk.component.api.service.http.UseConfigurer;

public interface AccessTokenProvider extends HttpClient {
    // https://login.microsoftonline.com/<tenantid>/oauth2/v2.0/token

    /**
     * Headers : "Content-Type: application/x-www-form-urlencoded"
     * Body :
     * {"client_id": <CLIENT_ID>,
     * "client_secret": <CLIENT_SECRET>,
     * "scope" : "https://storage.azure.com/.default",
     * "grant_type" : "client_credentials"
     * }
     */
    @UseConfigurer(AccessTokenConfigurer.class)
    @Request(path = "/{tenantId}/oauth2/v2.0/token", method = "POST")
    Response<JsonObject> getAccessToken( //
            @Path("tenantId") String tenantId, //
            String payload //
    );

}
