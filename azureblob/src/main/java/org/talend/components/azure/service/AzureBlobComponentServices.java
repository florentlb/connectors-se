/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.talend.components.azure.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.azure.connection.AzureCloudConnection;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.SuggestionValues;
import org.talend.sdk.component.api.service.completion.Suggestions;
import org.talend.sdk.component.api.service.healthcheck.HealthCheck;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import lombok.Getter;

@Service
public class AzureBlobComponentServices {

    public static final String GET_CONTAINER_NAMES = "getContainers";

    @Getter
    @Service
    AzureComponentServices connectionService;

    @Service
    private MessageService i18nService;

    @Suggestions(GET_CONTAINER_NAMES)
    public SuggestionValues getContainerNames(@Option AzureCloudConnection azureConnection) {
        List<SuggestionValues.Item> containerNames = new ArrayList<>();
        try {
            CloudStorageAccount storageAccount = connectionService.createStorageAccount(azureConnection);
            final OperationContext operationContext = AzureComponentServices.getTalendOperationContext();
            for (CloudBlobContainer container : connectionService
                    .createCloudBlobClient(storageAccount, AzureComponentServices.DEFAULT_RETRY_POLICY)
                    .listContainers(null, null, null, operationContext)) {
                containerNames.add(new SuggestionValues.Item(container.getName(), container.getName()));
            }

        } catch (Exception e) {
            throw new RuntimeException(i18nService.errorRetrieveContainers(), e);
        }

        return new SuggestionValues(true, containerNames);
    }
}