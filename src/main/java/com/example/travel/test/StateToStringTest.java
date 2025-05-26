package com.example.travel.test;

import com.example.travel.model.State;

/**
 * A simple test program to verify that the toString() method in the State enum
 * correctly displays both the state code and full name.
 */
public class StateToStringTest {
    public static void main(String[] args) {
        System.out.println("Testing State.toString() method:");
        
        // Test a few states to verify the toString() method
        System.out.println("ID: " + State.ID);
        System.out.println("CA: " + State.CA);
        System.out.println("NY: " + State.NY);
        
        // Test all states
        System.out.println("\nAll states:");
        for (State state : State.values()) {
            System.out.println(state);
        }
    }
}