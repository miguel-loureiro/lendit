package com.ims.models;

import lombok.Getter;

@Getter
public enum Role{

    SUPER(4),
    MANAGER(3),
    CLIENT(2),
    GUEST(1);

    private final int rank;

    Role(int rank) {

        this.rank = rank;
    }
}
