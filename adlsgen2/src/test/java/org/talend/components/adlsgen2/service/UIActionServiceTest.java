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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.SuggestionValues;
import org.talend.sdk.component.api.service.completion.SuggestionValues.Item;
import org.talend.sdk.component.junit.http.junit5.HttpApi;
import org.talend.sdk.component.junit5.WithComponents;

@Slf4j
@HttpApi(useSsl = true, responseLocator = org.talend.sdk.component.junit.http.internal.impl.AzureStorageCredentialsRemovalResponseLocator.class)
@WithComponents("org.talend.components.adlsgen2")
class UIActionServiceTest extends org.talend.components.adlsgen2.AdlsGen2TestBase {

    @Service
    UIActionService ui;

    @Test
    void filesystemList() {
        SuggestionValues fs = ui.filesystemList(connection);
        assertNotNull(fs);
        log.error("[filesystemList] {}", fs);
        for (Item i : fs.getItems()) {
            log.error("[filesystemList] {}", i);
        }
    }
}
