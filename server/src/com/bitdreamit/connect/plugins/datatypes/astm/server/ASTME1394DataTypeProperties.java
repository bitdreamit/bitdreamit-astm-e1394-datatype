package com.bitdreamit.connect.plugins.datatypes.astm.server;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypeProperties;

/**
 * Container that wires together the five ASTM E1394 property groups used by
 * Mirth Connect's data-type framework.
 *
 * <p>Mirth instantiates this class via the default constructor whenever a new
 * channel / connector is created. The five nested property objects then
 * expose their individual {@code PropertyDescriptor} maps to the Administrator
 * UI for editing.</p>
 *
 * <p>{@link DataTypeProperties} implements the {@code Migratable} interface,
 * which declares seven {@code migrate3_x_0(DonkeyElement)} hooks. Each nested
 * property class already overrides them as no-ops; this top-level container
 * mirrors the same pattern so the parent's abstract methods are satisfied
 * even when a migration framework instantiates this class directly without
 * going through the nested property classes.</p>
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
}
