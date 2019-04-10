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
package org.talend.components.adlsgen2.common.format.avro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.components.adlsgen2.AdlsGen2TestBase;
import org.talend.sdk.component.junit5.WithComponents;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@WithComponents("org.talend.components.adlsgen2")
class AvroConverterTest extends AdlsGen2TestBase {

    private AvroConverter converter = AvroConverter.of();

    @BeforeEach
    protected void setUp() {
        super.setUp();
    }

    @Test
    void of() {
    }

    @Test
    void inferSchema() {
    }

    @Test
    void toRecord() {
    }

    @Test
    void fromRecord() {
        assertNotNull(converter.fromRecord(versatileRecord));
        assertNotNull(converter.fromRecord(complexRecord));
    }
}
