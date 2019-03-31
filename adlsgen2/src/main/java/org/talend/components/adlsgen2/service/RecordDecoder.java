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
package org.talend.components.adlsgen2.service;

import java.lang.reflect.Type;

import org.talend.sdk.component.api.service.http.Decoder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecordDecoder implements Decoder {

    @Override
    public Object decode(byte[] value, Type expectedType) {
        log.error("[decode] type: {}; value: {}.", expectedType, value);
        String content = new String(value);
        // Arrays.stream(content.split("\\n")).map(s -> );

        throw new UnsupportedOperationException("#decode()");
    }
}
