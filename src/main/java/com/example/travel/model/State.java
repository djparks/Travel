package com.example.travel.model;

public enum State {
    AL, AK, AZ, AR, CA, CO, CT, DE, FL, GA,
    HI, ID, IL, IN, IA, KS, KY, LA, ME, MD,
    MA, MI, MN, MS, MO, MT, NE, NV, NH, NJ,
    NM, NY, NC, ND, OH, OK, OR, PA, RI, SC,
    SD, TN, TX, UT, VT, VA, WA, WV, WI, WY;

    @Override
    public String toString() {
        // Get the corresponding USState to access the full name
        USState usState = USState.valueOf(this.name());
        return this.name() + " - " + usState.getFullName();
    }
}
