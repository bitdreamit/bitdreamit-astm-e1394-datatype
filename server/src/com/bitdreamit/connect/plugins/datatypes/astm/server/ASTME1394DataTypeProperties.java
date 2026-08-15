package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypeProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Container that wires together the five ASTM E1394 property groups used by
 * Mirth Connect's data-type framework.
 *
 * <p>Mirth instantiates this class via the default constructor whenever a new
 * channel / connector is created. The five nested property objects then
 * expose their individual {@code PropertyDescriptor} maps to the Administrator
 * UI for editing.</p>
 *
 * <p>{@link DataTypeProperties} implements the {@code Migratable} and
 * {@code Purgable} interfaces, which declare {@code migrate3_x_0} hooks and
 * {@code getPurgedProperties()} respectively. Each nested property class
 * already overrides them; this top-level container mirrors the same pattern
 * so the parent's abstract contract is satisfied even when a migration
 * framework instantiates this class directly without going through the
 * nested property classes.</p>
 *
 * <p>The {@code getPurgedProperties()} implementation aggregates the purged
 * property maps from all five nested property groups, so that channel-export
 * purging removes all sensitive values in one pass.</p>
 */
public class ASTME1394DataTypeProperties extends DataTypeProperties {

    public ASTME1394DataTypeProperties() {
        serializationProperties       = new ASTME1394SerializationProperties();
        deserializationProperties     = new ASTME1394DeserializationProperties();
        batchProperties               = new ASTME1394BatchProperties();
        responseGenerationProperties  = new ASTME1394ResponseGenerationProperties();
        responseValidationProperties  = new ASTME1394ResponseValidationProperties();
    }

    // -----------------------------------------------------------------
    // Migratable hooks — no schema migrations are required for v1.x of
    // the plugin. Each version's migration point is intentionally a
    // no-op so the parent's abstract contract is satisfied.
    // -----------------------------------------------------------------
    @Override public void migrate3_0_1(DonkeyElement element) {}
    @Override public void migrate3_0_2(DonkeyElement element) {}
    @Override public void migrate3_1_0(DonkeyElement element) {}
    @Override public void migrate3_2_0(DonkeyElement element) {}
    @Override public void migrate3_3_0(DonkeyElement element) {}
    @Override public void migrate3_4_0(DonkeyElement element) {}
    @Override public void migrate3_5_0(DonkeyElement element) {}

    // -----------------------------------------------------------------
    // Purgable — aggregate purged properties from all nested groups.
    // -----------------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purged = new HashMap<String, Object>();
        if (serializationProperties != null) {
            purged.putAll(serializationProperties.getPurgedProperties());
        }
        if (deserializationProperties != null) {
            purged.putAll(deserializationProperties.getPurgedProperties());
        }
        if (batchProperties != null) {
            purged.putAll(batchProperties.getPurgedProperties());
        }
        if (responseGenerationProperties != null) {
            purged.putAll(responseGenerationProperties.getPurgedProperties());
        }
        if (responseValidationProperties != null) {
            purged.putAll(responseValidationProperties.getPurgedProperties());
        }
        return purged;
    }
}
