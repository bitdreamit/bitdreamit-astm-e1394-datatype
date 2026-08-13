/*
 * BitDreamIT Mirth Lab Extensions
 * Copyright (c) 2026 Kimi AI (Moonshot AI) — MIT License
 */
package com.bitdreamit.mirth.labextensions.astme1394datatype;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Message control ID tracker for deduplication.
 * Extra feature beyond commercial extension.
 */
public class AstmControlIdTracker {
    private final Set<String> controlIds;
    private final int maxSize;

    public AstmControlIdTracker(int maxSize) {
        this.maxSize = maxSize;
        // LinkedHashSet maintains insertion order for LRU-like behavior
        this.controlIds = Collections.synchronizedSet(new LinkedHashSet<String>() {
            @Override
            public boolean add(String e) {
                if (size() >= maxSize) {
                    // Remove oldest
                    iterator().remove();
                }
                return super.add(e);
            }
        });
    }

    public boolean isDuplicate(String controlId) {
        if (controlId == null || controlId.isEmpty()) return false;
        boolean exists = controlIds.contains(controlId);
        if (!exists) {
            controlIds.add(controlId);
        }
        return exists;
    }

    public void clear() {
        controlIds.clear();
    }

    public int size() {
        return controlIds.size();
    }
}