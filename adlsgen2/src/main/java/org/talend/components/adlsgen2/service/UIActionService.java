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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.talend.components.adlsgen2.datastore.AdlsGen2Connection;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.SuggestionValues;
import org.talend.sdk.component.api.service.completion.SuggestionValues.Item;
import org.talend.sdk.component.api.service.completion.Suggestions;
import org.talend.sdk.component.api.service.healthcheck.HealthCheck;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;

import lombok.extern.slf4j.Slf4j;

import static org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus.Status.KO;
import static org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus.Status.OK;

@Slf4j
@Service
public class UIActionService implements Serializable {

    public static final String ACTION_HEALTHCHECK = "ACTION_HEALTHCHECK";

    public static final String ACTION_FILESYSTEMS = "ACTION_FILESYSTEMS";

    @Service
    private AdlsGen2Service service;

    @Service
    private I18n i18n;

    @HealthCheck(ACTION_HEALTHCHECK)
    public HealthCheckStatus validateConnection(@Option final AdlsGen2Connection connection) {
        log.info("[validateConnection] {}.", connection);
        try {
            service.filesystemList(connection);
        } catch (Exception e) {
            return new HealthCheckStatus(KO, i18n.healthCheckFailed(e.getMessage()));
        }
        return new HealthCheckStatus(OK, i18n.healthCheckOk());
    }

    @Suggestions(ACTION_FILESYSTEMS)
    public SuggestionValues filesystemList(@Option final AdlsGen2Connection connection) {
        log.warn("[filesystemList] connection: {}", connection);
        List<Item> items = new ArrayList<>();
        for (String s : service.filesystemList(connection)) {
            items.add(new SuggestionValues.Item(s, s));
        }
        return new SuggestionValues(true, items);
    }
}
