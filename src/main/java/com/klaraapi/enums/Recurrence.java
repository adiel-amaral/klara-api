package com.klaraapi.enums;

import lombok.Getter;

@Getter
public enum Recurrence {
    ONCE(0), MONTHLY(1), BIMONTHLY(2), QUARTERLY(3), SEMIANNUAL(6), ANNUAL(12);

    private final int monthsFactor;

    Recurrence(int monthsFactor) {
        this.monthsFactor = monthsFactor;
    }
}
