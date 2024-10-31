package com.ims.models;

import lombok.Getter;

@Getter
public enum Type {

    SUPER(4),
    MANAGER(3),
    CLIENT(2),
    GUEST(1);

    private final int rank;

    Type(int rank) {

        this.rank = rank;
    }
}
