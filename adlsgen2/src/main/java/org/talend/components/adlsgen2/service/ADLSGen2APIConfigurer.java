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

import lombok.extern.slf4j.Slf4j;

import org.talend.components.adlsgen2.datastore.ADLSGen2Connection;
import org.talend.components.adlsgen2.datastore.ADLSGen2Connection.AuthMethod;
import org.talend.components.adlsgen2.datastore.Constants.HeaderConstants;
import org.talend.sdk.component.api.service.http.Configurer;

@Slf4j
public class ADLSGen2APIConfigurer implements Configurer {

    @Override
    public void configure(final Connection connection, final ConfigurerConfiguration configuration) {
        final ADLSGen2Connection conn = configuration.get("connection", ADLSGen2Connection.class);
        final String auth = configuration.get("auth", String.class);
        log.debug("[configure] connection {}", conn);
        log.warn("[configure] [{}] {}", connection.getMethod(), connection.getUrl());
        log.debug("[configure] auth       {}", auth);
        connection //
                .withHeader(HeaderConstants.ACCEPT, HeaderConstants.ACCEPT_DEFAULT) //
                .withHeader(HeaderConstants.CONTENT_TYPE, HeaderConstants.DFS_CONTENT_TYPE) //
                .withHeader(HeaderConstants.VERSION, HeaderConstants.TARGET_STORAGE_VERSION);
        if (!AuthMethod.SAS.equals(conn.getAuthMethod())) {
            connection.withHeader(HeaderConstants.AUTHORIZATION, auth);
        }
        if (connection.getMethod().equals("POST")) {
            connection.withHeader(" X-HTTP-Method", "PUT"); //
        }
    }
}
