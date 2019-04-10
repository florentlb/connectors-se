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

import java.util.Arrays;
import java.util.Date;

import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.components.adlsgen2.AdlsGen2TestBase;
import org.talend.sdk.component.junit5.WithComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        GenericRecord record = converter.fromRecord(versatileRecord);
        assertEquals("Bonjour", record.get("string1"));
        assertEquals("Olà", record.get("string2"));
        assertEquals(71, record.get("int"));
        assertEquals(true, record.get("boolean"));
        assertEquals(1971L, record.get("long"));
        assertEquals(new Date(2019, 04, 22).getTime(), record.get("datetime"));
        assertEquals(20.5f, record.get("float"));
        assertEquals(20.5, record.get("double"));
        assertNotNull(record);
        record = converter.fromRecord(complexRecord);
        assertNotNull(record);
        assertEquals("ComplexR", record.get("name"));
        assertNotNull(record.get("record"));
        assertEquals(versatileRecord, record.get("record"));
        assertEquals(Arrays.asList("ary1", "ary2", "ary3"), record.get("array"));
    }
}
