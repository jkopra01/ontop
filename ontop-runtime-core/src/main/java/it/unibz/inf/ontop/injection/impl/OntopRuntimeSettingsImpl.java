package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;
import it.unibz.inf.ontop.injection.OntopRuntimeSettings;

import java.util.Properties;

public class OntopRuntimeSettingsImpl extends OntopOBDASettingsImpl implements OntopRuntimeSettings {

    private static final String DEFAULT_FILE = "runtime-default.properties";
    private final OntopOptimizationSettings optimizationSettings;

    OntopRuntimeSettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
        optimizationSettings = new OntopOptimizationSettingsImpl(copyProperties());
    }


    private static Properties loadProperties(Properties userProperties) {
        Properties properties = OntopOptimizationSettingsImpl.loadDefaultOptimizationProperties();
        properties.putAll(loadDefaultRuntimeProperties());
        properties.putAll(userProperties);
        return properties;
    }

    static Properties loadDefaultRuntimeProperties() {
        return loadDefaultPropertiesFromFile(OntopOBDASettings.class, DEFAULT_FILE);
    }

    @Override
    public boolean isRewritingEnabled() {
        return getRequiredBoolean(REWRITE);
    }

    @Override
    public boolean isDistinctPostProcessingEnabled() {
        return getRequiredBoolean(DISTINCT_RESULTSET);
    }

    @Override
    public boolean isIRISafeEncodingEnabled() {
        return getRequiredBoolean(SQL_GENERATE_REPLACE);
    }
}
