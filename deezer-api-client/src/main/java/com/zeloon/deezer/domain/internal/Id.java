package com.zeloon.deezer.domain.internal;


public abstract class Id {
    public final Long value;

    protected Id(final Long value) {
        this.value = value;
    }
}
