package com.ims.models;

public enum Type {

    SUPER(4),
    MANAGER(3),
    CLIENT(2),
    GUEST(1);

    private final int rank;

    Type(int rank) {

        this.rank = rank;
    }

    public int getRank() {

        return this.rank;
    }
}
