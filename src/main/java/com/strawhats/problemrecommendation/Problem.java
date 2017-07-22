package com.strawhats.problemrecommendation;

public class Problem {
    int problemID;
    String category;
    int sumbmissions;
    int accepted;

    @Override
    public String toString() {
        return "{Problem ID: " + problemID + ", Category: " + category + ", Sumbissions: " + sumbmissions + ", Accepted: " + accepted + "}";
    }
    
}
