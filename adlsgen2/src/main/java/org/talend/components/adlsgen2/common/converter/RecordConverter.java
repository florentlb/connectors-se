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
package org.talend.components.adlsgen2.common.converter;

import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;

public interface RecordConverter<T> {

    Schema inferSchema(T record);

    Record toRecord(T record);

    T fromRecord(Record record);

}
