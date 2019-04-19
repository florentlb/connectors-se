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
package org.talend.components.marketo.output;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.json.JsonObject;

import org.talend.components.marketo.MarketoSourceOrProcessor;
import org.talend.components.marketo.dataset.MarketoOutputConfiguration;
import org.talend.components.marketo.service.MarketoService;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Icon.IconType;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.AfterGroup;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Input;
import org.talend.sdk.component.api.processor.Processor;
import org.talend.sdk.component.api.record.Record;

import lombok.extern.slf4j.Slf4j;

import static org.talend.components.marketo.MarketoApiConstants.ATTR_REASONS;
import static org.talend.components.marketo.MarketoApiConstants.ATTR_RESULT;

@Slf4j
@Version
@Processor(family = "Marketo", name = "Output")
@Icon(value = IconType.MARKETO)
@Documentation("Marketo Output Component")
public class MarketoProcessor extends MarketoSourceOrProcessor {

    private static final int BATCH_SIZE = 300;

    protected final MarketoOutputConfiguration configuration;

    private ProcessorStrategy strategy;

    private List<JsonObject> records;

    public MarketoProcessor(@Option("configuration") final MarketoOutputConfiguration configuration, //
            final MarketoService service) {
        super(configuration.getDataSet(), service);
        this.configuration = configuration;
        records = new ArrayList<>();
        strategy = new LeadStrategy(configuration, service);
    }

    @PostConstruct
    @Override
    public void init() {
        strategy.init();
    }

    @ElementListener
    public void map(@Input final Record incomingData) {
        JsonObject data = marketoService.toJson(incomingData);
        records.add(data);
        log.debug("[map] received: {}.", data);
        if (records.size() >= BATCH_SIZE) {
            flush();
        }
    }

    @AfterGroup
    public void flush() {
        log.warn("[flush] called. Processing {} records.", records.size());
        if (records.isEmpty()) {
            return;
        }
        JsonObject payload = strategy.getPayload(records);
        log.debug("[map] payload : {}.", payload);
        JsonObject result = strategy.runAction(payload);
        records.clear();
        log.debug("[map] result  : {}.", result);
        result.getJsonArray(ATTR_RESULT).getValuesAs(JsonObject.class).stream().filter(strategy::isRejected).forEach(e -> {
            log.error(getErrors(e.getJsonArray(ATTR_REASONS)));
        });
    }

}
