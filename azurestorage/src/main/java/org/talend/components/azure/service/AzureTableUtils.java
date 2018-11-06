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
package org.talend.components.azure.service;

import org.apache.commons.lang3.StringUtils;
import org.talend.components.azure.common.Comparison;
import org.talend.components.azure.table.input.InputProperties;

import com.microsoft.azure.storage.table.TableQuery;

import lombok.Data;

@Data
public class AzureTableUtils {

    public static final String TABLE_TIMESTAMP = "Timestamp";

    public static String generateCombinedFilterConditions(InputProperties options) {
        String filter = "";
        if (isValidFilterExpression(options)) {
            for (InputProperties.FilterExpression filterExpression : options.getFilterExpressions()) {
                String cfn = filterExpression.getFunction().toString();
                String cop = filterExpression.getPredicate().toString();
                String typ = filterExpression.getFieldType().toString();

                String filterB = TableQuery.generateFilterCondition(filterExpression.getColumn(),
                        Comparison.getQueryComparisons(cfn), filterExpression.getValue(),
                        InputProperties.FieldType.getEdmType(typ));

                filter = filter.isEmpty() ? filterB
                        : TableQuery.combineFilters(filter, InputProperties.Predicate.getOperator(cop), filterB);
            }
        }
        return filter;
    }

    /**
     * this method check if the data in the Filter expression is valid and can produce a Query filter.<br/>
     * the table is valid if :<br>
     * 1) all column, fieldType, function, operand and predicate lists are not null<br/>
     * 2) values in the lists column, fieldType, function, operand and predicate are not empty
     *
     * <br/>
     *
     * @return {@code true } if the two above condition are true
     *
     */
    private static boolean isValidFilterExpression(InputProperties options) {

        if (options.getFilterExpressions() == null) {
            return false;
        }
        for (InputProperties.FilterExpression filterExpression : options.getFilterExpressions()) {
            if (StringUtils.isEmpty(filterExpression.getColumn()) || filterExpression.getFieldType() == null
                    || filterExpression.getFunction() == null || StringUtils.isEmpty(filterExpression.getValue())
                    || filterExpression.getPredicate() == null) {
                return false;
            }
        }

        return true;
    }
}