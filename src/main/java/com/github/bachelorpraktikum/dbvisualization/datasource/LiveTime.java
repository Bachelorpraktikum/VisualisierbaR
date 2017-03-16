package com.github.bachelorpraktikum.dbvisualization.datasource;

class LiveTime {

    private Result result;

    public int getTime() {
        return (int) (result.timeValue * 1000.0);
    }

    static class Result {

        private double timeValue;
    }
}
