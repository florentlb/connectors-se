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

import lombok.Data;
import lombok.ToString;

import static org.talend.components.adlsgen2.service.UIActionService.ACTION_HEALTHCHECK;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Checkable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

@Data
@ToString
@DataStore("ADLSGen2Connection")
@Checkable(ACTION_HEALTHCHECK)
@GridLayout({ //
        @GridLayout.Row("accountName"), //
        @GridLayout.Row("authMethod"), //
        @GridLayout.Row("sharedKey"), //
        @GridLayout.Row("tenantId"), //
        @GridLayout.Row({ "clientId", "clientSecret" }), //
        @GridLayout.Row("sas"), //
})
@Documentation("The datastore to connect Azure Data Lake Storage Gen2")
public class ADLSGen2Connection implements Serializable {

    @Option
    @Required
    @Documentation("Storage Account Name")
    private String accountName;

    @Option
    @Required
    @Documentation("Authentication method")
    private AuthMethod authMethod;

    @Option
    @Required
    @ActiveIf(target = "authMethod", value = "SharedKey")
    @Documentation("Storage Shared Key")
    private String sharedKey;

    @Option
    @Required
    @ActiveIf(target = "authMethod", value = "AccessToken")
    @Documentation("Tenant Id")
    private String tenantId;

    @Option
    @Required
    @ActiveIf(target = "authMethod", value = "AccessToken")
    @Documentation("Client Id")
    private String clientId;

    @Option
    @Required
    @ActiveIf(target = "authMethod", value = "AccessToken")
    @Documentation("Client Secret")
    private String clientSecret;

    @Option
    @Required
    @ActiveIf(target = "authMethod", value = "SAS")
    @Documentation("Shared Access Signature")
    private String sas;

    public String apiUrl() {
        return String.format(Constants.DFS_URL, getAccountName());
    }

    public String apiUrlWithSas() {
        String url = String.format(Constants.DFS_URL, getAccountName());
        if (authMethod.equals(AuthMethod.SAS)) {
            url = url + getSas();
        }
        return url;
    }

    public String oauthUrl() {
        return String.format(Constants.TOKEN_URL, getTenantId());
    }

    public enum AuthMethod {
        SharedKey,
        AccessToken,
        SAS,
        ADAL,
    }

}
