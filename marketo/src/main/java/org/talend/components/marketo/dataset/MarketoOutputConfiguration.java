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
package org.talend.components.marketo.dataset;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;
import lombok.ToString;

import static org.talend.components.marketo.service.UIActionService.LEAD_KEY_NAME_LIST;

@Data
@GridLayout({ //
        @GridLayout.Row({ "dataSet" }), //
        @GridLayout.Row({ "action" }), //
        @GridLayout.Row({ "syncMethod" }), //
        @GridLayout.Row({ "lookupField" }), //
}) //
@Documentation("Marketo Sink Configuration")
@ToString(callSuper = true)
public class MarketoOutputConfiguration implements Serializable {

    public static final String NAME = "MarketoOutputConfiguration";

    public enum OutputAction {
        sync,
        delete
    }

    public enum ListAction {
        addTo,
        removeFrom
    }

    public enum SyncMethod {
        createOnly,
        updateOnly,
        createOrUpdate,
        createDuplicate
    }

    public enum DeleteBy {
        dedupeFields,
        idField
    }

    /*
     * DataSet
     */
    @Option
    @Documentation("Marketo DataSet")
    private MarketoDataSet dataSet;

    @Option
    @Documentation("Action")
    private OutputAction action;

    /*
     * Lead Entity
     */
    @Option
    @ActiveIf(target = "action", value = "sync")
    @Suggestable(value = LEAD_KEY_NAME_LIST)
    @Documentation("Lookup Field")
    private String lookupField;

    /*
     * All entities
     */
    @Option
    @ActiveIf(target = "action", value = { "sync" })
    @Documentation("Synchronization method")
    private SyncMethod syncMethod;

}
