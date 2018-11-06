// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.azure.common;

import static org.talend.components.azure.service.AzureComponentServices.TEST_CONNECTION;

import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Checkable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Credential;
import org.talend.sdk.component.api.meta.Documentation;

@GridLayout({ @GridLayout.Row({ "accountName" }), @GridLayout.Row({ "accountKey" }), @GridLayout.Row({ "protocol" }),
        @GridLayout.Row({ "useAzureSharedSignature" }), @GridLayout.Row({ "azureSharedAccessSignature" }) })
@Data
@DataStore
@Checkable(TEST_CONNECTION)
public class AzureConnection {

    @Option("accountName")
    @Documentation("Name of the storage account you need to access. "
            + "A storage account name can be found in the Storage accounts dashboard of the Microsoft Azure Storage system to be used. "
            + "Ensure that the administrator of the system has granted you the appropriate access permissions to this storage account.")
    @ActiveIf(target = "useAzureSharedSignature", value = "false")
    private String accountName;

    @Option
    @Documentation("The key associated with the storage account you need to access. "
            + "Two keys are available for each account and by default, either of them can be used for this access.")
    @Credential
    @ActiveIf(target = "useAzureSharedSignature", value = "false")
    private String accountKey;

    @Option
    @Documentation("The protocol for connection to be created.")
    @ActiveIf(target = "useAzureSharedSignature", value = "false")
    private Protocol protocol = Protocol.HTTPS;

    @Option
    @Documentation("Use a shared access signature (SAS) to access the storage resources without need for the account key. "
            + "For more information, see Using Shared Access Signatures (SAS): "
            + "https://docs.microsoft.com/en-us/azure/storage/storage-dotnet-shared-access-signature-part-1")
    private boolean useAzureSharedSignature;

    @Option
    @Documentation("Enter your account SAS URL between double quotation marks.\n"
            + "You can get the SAS URL for each allowed service on Microsoft Azure portal after generating SAS.\n"
            + "The SAS URL format is https://<$storagename>.<$service>.core.windows.net/<$sastoken>,"
            + "where <$storagename> is the storage account name, "
            + "<$service> is the allowed service name (blob, file, queue or table)," + "and <$sastoken> is the SAS token value."
            + "For more information, see Azure documentation.\n" + "Note that the SAS has valid period, "
            + "you can set the start time at which the SAS becomes valid and the expiry time after which the SAS is no longer valid when generating it, "
            + "and you need to make sure your SAS is still valid when running your Job.")
    @ActiveIf(target = "useAzureSharedSignature", value = "true")
    private String azureSharedAccessSignature;
}