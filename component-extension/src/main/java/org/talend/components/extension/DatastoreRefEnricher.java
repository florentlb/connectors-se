package org.talend.components.extension;

import org.talend.sdk.component.api.component.Components;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.runtime.manager.reflect.parameterenricher.BaseParameterEnricher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class DatastoreRefEnricher extends BaseParameterEnricher {

    public static final String META_PREFIX = "tcomp::configurationtyperef::";

    @Override
    public Map<String, String> onParameterAnnotation(final String parameterName, final Type parameterType,
            final Annotation annotation) {

        // todo : add validation at this level or contribute a validation listener to the model ?
        if (DatastoreRef.class.getName().equals(annotation.annotationType().getName())) {
            final Components c = findFamilyInfo(parameterType);
            if (c != null) {
                final String family = c.family();
                final String name = DataStore.class.cast(((Class) parameterType).getAnnotation(DataStore.class)).value();
                return new HashMap<String, String>() {

                    {
                        put(META_PREFIX + "family", family);
                        put(META_PREFIX + "name", name);
                    }
                };
            }
        }
        return emptyMap();
    }

    private Components findFamilyInfo(Type parameterType) {
        final String p = ((Class) parameterType).getPackage().getName();
        final ClassLoader loader = ((Class) parameterType).getClassLoader();
        if (p != null) {
            String currentPackage = p;
            do {
                try {
                    final Class<?> pckInfo = loader.loadClass(currentPackage + ".package-info");
                    if (pckInfo.isAnnotationPresent(Components.class)) {
                        return pckInfo.getAnnotation(Components.class);
                    }
                } catch (final ClassNotFoundException e) {
                    // no-op
                }

                final int endPreviousPackage = currentPackage.lastIndexOf('.');
                // we don't accept default package since it is not specific enough
                if (endPreviousPackage < 0) {
                    break;
                }

                currentPackage = currentPackage.substring(0, endPreviousPackage);
            } while (true);
        }
        return null;
    }
}
