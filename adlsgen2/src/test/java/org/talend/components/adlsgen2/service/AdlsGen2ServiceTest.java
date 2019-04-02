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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.junit.http.internal.impl.AzureStorageCredentialsRemovalResponseLocator;
import org.talend.sdk.component.junit.http.junit5.HttpApi;
import org.talend.sdk.component.junit5.WithComponents;

@Slf4j
@HttpApi(useSsl = true, responseLocator = AzureStorageCredentialsRemovalResponseLocator.class)
@WithComponents("org.talend.components.adlsgen2")
class AdlsGen2ServiceTest extends org.talend.components.adlsgen2.AdlsGen2TestBase {

    @Service
    AdlsGen2Service service;

    @Service
    AdlsGen2APIClient serviceTestClient;

    @Service
    AccessTokenProvider accessTokenProvider;

    @Test
    void getClient() {
        serviceTestClient = service.getClient(connection);
        assertNotNull(serviceTestClient);
    }

    @Test
    void accessToken() {
        String result = service.getAccessToken(connection);
        assertNotNull(result);
        assertTrue(result.length() > 100);
    }

    @Test
    void filesystemList() {
        log.warn("[filesystemList] {}", connection);
        List<String> result = service.filesystemList(connection);
        assertNotNull(result);
        assertTrue(result.size() >= 0);
    }

    @Test
    void pathList() {
        String path = "";
        inputConfiguration.getDataSet().setBlobPath(path);
        Object result = service.pathList(inputConfiguration);

        log.warn("[pathList] {}", result);
    }

    @Test()
    void pathListInexistent() {
        String path = "myNewFolderZZZ";
        inputConfiguration.getDataSet().setBlobPath(path);
        Object result = null;
        try {
            result = service.pathList(inputConfiguration);
            fail("The path should not exist");
        } catch (Exception e) {
        }
    }

    @Test
    void extractFolderPath() {
        String blobPath = "folder01/folder02/newFolder01/blob.txt";
        assertEquals("folder01/folder02/newFolder01", service.extractFolderPath(blobPath));
        blobPath = "/folder01/folder02/newFolder01/blob.txt";
        assertEquals("/folder01/folder02/newFolder01", service.extractFolderPath(blobPath));
        blobPath = "newFolder01/blob.txt";
        assertEquals("newFolder01", service.extractFolderPath(blobPath));
        blobPath = "/newFolder01/blob.txt";
        assertEquals("/newFolder01", service.extractFolderPath(blobPath));
        blobPath = "/blob.txt";
        assertEquals("/", service.extractFolderPath(blobPath));
        blobPath = "blob.txt";
        assertEquals("/", service.extractFolderPath(blobPath));
        blobPath = "";
        assertEquals("/", service.extractFolderPath(blobPath));
    }

    @Test
    void pathExists() {
        // paths should exists
        String blobPath = "/myNewFolder/blob.txt";
        inputConfiguration.getDataSet().setBlobPath(blobPath);
        assertTrue(service.pathExists(inputConfiguration.getDataSet()));
        blobPath = "myNewFolder/blob.txt";
        inputConfiguration.getDataSet().setBlobPath(blobPath);
        assertTrue(service.pathExists(inputConfiguration.getDataSet()));
        blobPath = "myNewFolder/subfolder02/blob.txt";
        inputConfiguration.getDataSet().setBlobPath(blobPath);
        assertTrue(service.pathExists(inputConfiguration.getDataSet()));
        blobPath = "myNewFolder/subfolder02/subfolder03/blob.txt";
        inputConfiguration.getDataSet().setBlobPath(blobPath);
        assertTrue(service.pathExists(inputConfiguration.getDataSet()));
        // paths do not exist
        blobPath = "myNewFolder/subfolder03/blob.txt";
        inputConfiguration.getDataSet().setBlobPath(blobPath);
        assertFalse(service.pathExists(inputConfiguration.getDataSet()));
        blobPath = "/newFolder01ZZZ/blob.txt";
        inputConfiguration.getDataSet().setBlobPath(blobPath);
        assertFalse(service.pathExists(inputConfiguration.getDataSet()));
    }

    @Test
    void pathReadSmallFile() {
        String path = "myNewFolder/nostorelookup.java";
        inputConfiguration.getDataSet().setBlobPath(path);
        Object result = service.pathRead(inputConfiguration);
        log.warn("[pathList] {}", result);
    }

    @Test
    void pathReadMediumFile() {
        // String path = "myNewFolder/customer.csv";
        String path = "myNewFolder/customer_20190325.csv";
        inputConfiguration.getDataSet().setBlobPath(path);
        Object result = service.pathRead(inputConfiguration);
    }

    @Test
    void pathUpdateFile() {
        String path = "myNewFolder/customer.csv";
        outputConfiguration.getDataSet().setBlobPath(path);
        outputConfiguration.setOverwrite(true);
        String content = "ABC;DEF;123;true;GBG\n";
        Object result = service.pathUpdate(outputConfiguration, content, 0);
        log.warn("[pathList] {}", result);
    }

    @Test
    void pathGetProperties() {
        String path = "myNewFolder/customer.csv";
        outputConfiguration.getDataSet().setBlobPath(path);
        Object result = service.pathGetProperties(outputConfiguration.getDataSet());
        log.warn("[pathGetProperties] {}", result);
    }
}
