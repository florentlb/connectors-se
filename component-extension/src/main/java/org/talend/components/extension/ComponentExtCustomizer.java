package org.talend.components.extension;

import org.talend.sdk.component.runtime.manager.ComponentManager;

import java.util.stream.Stream;

public class ComponentExtCustomizer implements ComponentManager.Customizer {

    @Override
    public Stream<String> containerClassesAndPackages() {
        return Stream.of(DatastoreRef.class.getName());
    }

    @Override
    public boolean ignoreBeamClassLoaderExclusions() {
        return false;
    }
}
