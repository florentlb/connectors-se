package org.talend.components.azure.common;

import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.meta.Documentation;

@Data
@DataSet("AzureDataSet")
public class AzureTableConnection {

    @Option
    @Documentation("Connection")
    private AzureConnection connection;

    @Option
    @Documentation("Table Name")
    @Suggestable(value = "getTableNames", parameters = "connection")
    private String tableName;
}