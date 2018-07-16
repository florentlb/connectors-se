package org.talend.components.netsuite.service;

import org.talend.sdk.component.api.internationalization.Internationalized;

@Internationalized
public interface Messages {

    String warnBatchTimeout();

    String healthCheckOk();

    String healthCheckFailed(final String cause);
}