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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import lombok.Data;
import lombok.ToString;

import static org.talend.components.marketo.service.UIActionService.ACTIVITIES_LIST;
import static org.talend.components.marketo.service.UIActionService.FIELD_NAMES;
import static org.talend.components.marketo.service.UIActionService.LEAD_KEY_NAME_LIST;

@Data
@GridLayout({ //
        @GridLayout.Row({ "dataSet" }), //
        @GridLayout.Row({ "leadAction" }), //
        @GridLayout.Row({ "leadKeyName" }), //
        @GridLayout.Row({ "leadKeyValues" }), //
        @GridLayout.Row({ "leadId", }), //
        @GridLayout.Row({ "leadIds" }), //
        @GridLayout.Row({ "assetIds" }), //
        @GridLayout.Row({ "listId" }), //
        @GridLayout.Row({ "activityTypeIds" }), //
        @GridLayout.Row({ "sinceDateTime" }), //
        @GridLayout.Row({ "fields" }), //
}) //
@Documentation("Marketo Source Configuration")
@ToString(callSuper = true)
public class MarketoInputConfiguration implements Serializable {

    public static final String NAME = "MarketoInputConfiguration";

    /*
     * DataSet
     */
    @Option
    @Documentation("Marketo DataSet")
    private MarketoDataSet dataSet;

    /*
     * Lead DataSet parameters
     */
    @Option
    @Documentation("Lead Action")
    private LeadAction leadAction = LeadAction.getLeadChanges;

    @Option
    @ActiveIf(target = "leadAction", value = "getLead")
    @Documentation("Lead Id")
    private Integer leadId;

    @Option
    @ActiveIf(target = "leadAction", value = "getMultipleLeads")
    @Suggestable(value = LEAD_KEY_NAME_LIST)
    @Documentation("Key Name")
    private String leadKeyName;

    @Option
    @ActiveIf(target = "leadAction", value = "getMultipleLeads")
    @Documentation("Values (Comma-separated)")
    private String leadKeyValues;

    @Option
    @ActiveIf(target = "leadAction", value = { "getLeadChanges", "getLeadActivity" })
    @Documentation("Static List Id")
    private Integer listId;

    /*
     * Changes & Activities
     */
    @Option
    @ActiveIf(target = "leadAction", value = { "getLeadChanges", "getLeadActivity" })
    @Documentation("Since Date Time")
    private String sinceDateTime = ZonedDateTime.now().minusMonths(7)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd" + " HH:mm:ss"));

    @Option
    @ActiveIf(target = "leadAction", value = { "getLeadChanges", "getLeadActivity" })
    @Documentation("Lead Ids (Comma-separated Lead Ids)")
    private String leadIds;

    @Option
    @ActiveIf(target = "leadAction", value = { "getLeadChanges", "getLeadActivity" })
    @Documentation("Asset Ids (Comma-separated Asset Ids)")
    private String assetIds;

    @Option
    @ActiveIf(target = "leadAction", value = "getLeadActivity")
    @Suggestable(value = ACTIVITIES_LIST, parameters = { "../dataSet/dataStore" })
    @Documentation("Activity Type Ids (10 max supported")
    private List<String> activityTypeIds;

    @Option
    @Suggestable(value = FIELD_NAMES, parameters = { "../dataSet" })
    @Documentation("Fields")
    private List<String> fields = Arrays.asList("firstName", "LastName", "email", "company");

    public enum LeadAction {
        getLead,
        getMultipleLeads,
        getLeadActivity,
        getLeadChanges
    }

    public enum ListAction {
        list,
        get,
        isMemberOf,
        getLeads
    }

    public enum OtherEntityAction {
        list,
        get
    }

}
