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

import org.talend.sdk.component.api.service.http.Configurer;

@Slf4j
public class AdlsGen2UpdateConfigurer implements Configurer {

    @Override
    public void configure(Connection connection, ConfigurerConfiguration configuration) {
        log.warn("[configure] [{}] URL: {}.", connection.getMethod(), connection.getUrl());
    }
}
