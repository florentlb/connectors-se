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

import org.talend.components.adlsgen2.datastore.Constants.HeaderConstants;
import org.talend.sdk.component.api.service.http.Configurer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessTokenConfigurer implements Configurer {

    protected static final String CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    @Override
    public void configure(Connection connection, ConfigurerConfiguration configuration) {
        final org.talend.components.adlsgen2.datastore.AdlsGen2Connection conn = configuration.get("connection",
                org.talend.components.adlsgen2.datastore.AdlsGen2Connection.class);
        log.debug("[configure] [{}] {}", connection.getMethod(), connection.getUrl());
        connection //
                .withHeader(HeaderConstants.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED) //
                .withHeader(HeaderConstants.CONTENT_LENGTH, String.valueOf(connection.getPayload().length));
    }
}
