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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.configuration.Configuration;

public class UnknownIterator implements Iterator<Record> {

    private UnknownConverter converter;

    private BufferedReader reader;

    private String current;

    private UnknownIterator(BufferedReader ureader) {
        reader = ureader;
        converter = UnknownConverter.of();
    }

    @Override
    public boolean hasNext() {
        try {
            current = reader.readLine();
            if (current == null) {
                reader.close();
                return false;
            }
            return true;
        } catch (IOException e) {
            return false; // last call when stream is closed
        }
    }

    @Override
    public Record next() {
        if ((current != null) || hasNext()) {
            Record r = converter.toRecord(current);
            current = null;
            return r;
        } else {
            return null;
        }
    }

    public static class Builder {

        private Builder() {
        }

        public static UnknownIterator.Builder of() {
            return new UnknownIterator.Builder();
        }

        public UnknownIterator.Builder withConfiguration(
                @Configuration("unknownConfiguration") final UnknownConfiguration configuration) {
            return this;
        }

        public UnknownIterator parse(InputStream in) {
            // TODO manage encoding
            return new UnknownIterator(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
        }

        public UnknownIterator parse(String content) {
            return new UnknownIterator(new BufferedReader(new StringReader(content)));
        }
    }
}
