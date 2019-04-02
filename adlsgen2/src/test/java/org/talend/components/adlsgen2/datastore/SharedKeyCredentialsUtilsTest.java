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
package org.talend.components.adlsgen2.datastore;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.sdk.component.junit5.WithComponents;

import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpMethod;
import com.microsoft.rest.v2.http.HttpRequest;

@Slf4j
@WithComponents("org.talend.components.adlsgen2")
class SharedKeyCredentialsUtilsTest extends org.talend.components.adlsgen2.AdlsGen2TestBase {

    private SharedKeyUtils utils;

    @BeforeEach
    void setUp() throws Exception {
        utils = new SharedKeyUtils(accountName, accountKey);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getAccountName() {
        assertEquals(accountName, utils.getAccountName());
    }

    @Test
    void buildAuthenticationSignature() throws Exception {
        URL url = new URL(
                "https://undxgen2.dfs.core.windows.net/adls-gen2?directory=myNewFolder&recursive=false&resource=filesystem&timeout=60");
        HttpHeaders headers = new HttpHeaders(new HashMap<>());
        HttpRequest request = new HttpRequest(null, HttpMethod.GET, url, headers, null, null);
        String signature = utils.buildAuthenticationSignature(request);
        assertNotNull(signature);
        assertTrue(signature.startsWith("SharedKey undxgen2:"));
    }
}
