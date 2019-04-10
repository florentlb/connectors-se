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
package org.talend.components.adlsgen2.common.format.unknown;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.components.adlsgen2.AdlsGen2TestBase;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.record.Schema.Type;
import org.talend.sdk.component.junit5.WithComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WithComponents("org.talend.components.adlsgen2")
class UnknownConverterTest extends AdlsGen2TestBase {

    private Schema schema;

    private UnknownConverter converter;

    private String content = "Another content with no meaning...";

    @BeforeEach
    protected void setUp() {
        super.setUp();
        converter = UnknownConverter.of();
        schema = recordBuilderFactory.newSchemaBuilder(Type.RECORD)
                .withEntry(recordBuilderFactory.newEntryBuilder().withName("content").withType(Type.STRING).build()).build();
    }

    @Test
    void of() {
        assertNotNull(UnknownConverter.of());
    }

    @Test
    void inferSchema() {
        assertEquals(schema, converter.inferSchema(content));
    }

    @Test
    void toRecord() {
        assertEquals(content, converter.toRecord(content).getString("content"));
    }

    @Test
    void fromRecord() {
        Record record = recordBuilderFactory.newRecordBuilder().withString("content", content).build();
        assertEquals(String.format("content:{%s}", content), converter.fromRecord(record));
        record = recordBuilderFactory.newRecordBuilder() //
                .withString("field0", "Bonjour") //
                .withString("field1", "Olà") //
                .withInt("field2", 71) //
                .build();
        assertEquals("field0:{Bonjour},field1:{Olà},field2:{71}", converter.fromRecord(record));
        assertEquals(
                "string1:{Bonjour},string2:{Olà},int:{71},boolean:{true},long:{1971},datetime:{61516620000000},float:{20.5},double:{20.5}",
                converter.fromRecord(versatileRecord));
    }
}
