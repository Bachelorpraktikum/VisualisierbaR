package com.github.bachelorpraktikum.dbvisualization.datasource;

import com.github.bachelorpraktikum.dbvisualization.model.train.Train;

public class LiveTrain {

    /**
     * Represents the result of a failed {@link RestSource#getTrain(Train)} call.
     */
    static final LiveTrain INVALID = new LiveTrain("", -1, -1, "", "");

    private String name;
    private int emergCount;
    private int fahrCount;
    private String v;
    private String accelState;

    // needed for GSON.
    private LiveTrain() {
    }

    private LiveTrain(String name, int emergCount, int fahrCount, String v, String accelState) {
        this.name = name;
        this.emergCount = emergCount;
        this.fahrCount = fahrCount;
        this.v = v;
        this.accelState = accelState;
    }

    /**
     * <p>Gets the readable / short name of this train.</p>
     *
     * <p>This method is guaranteed to not return null, <b>if this is a {@link #isValid() valid
     * instance} of LiveTrain.</b></p>
     *
     * @return the name
     */
    public String getReadableName() {
        return name;
    }

    /**
     * Gets the "emergCount" value of this train.
     *
     * @return the value
     */
    public int getEmergCount() {
        return emergCount;
    }

    /**
     * Gets the "fahrCount" value of this train.
     *
     * @return the value
     */
    public int getFahrCount() {
        return fahrCount;
    }

    /**
     * <p>Gets the "v" value of this train.</p>
     *
     * <p>This method is guaranteed to not return null, <b>if this is a {@link #isValid() valid
     * instance} of LiveTrain.</b></p>
     *
     * @return the value
     */
    public String getV() {
        return v;
    }

    /**
     * <p>Gets the "accelState" value of this train.</p>
     *
     * <p>This method is guaranteed to not return null, <b>if this is a {@link #isValid() valid
     * instance} of LiveTrain.</b></p>
     *
     * @return the value
     */
    public String getAccelState() {
        return accelState;
    }

    /**
     * Gets a valid instance of this class.
     * If this instance is valid, it is returned. If not, {@link #INVALID} is returned.
     *
     * @return a valid instance of LiveTrain
     */
    LiveTrain getValid() {
        return isValid() ? this : INVALID;
    }

    /**
     * Determines whether this is a valid instance of LiveTrain.
     *
     * @return whether this instance is valid
     */
    boolean isValid() {
        return !(name == null || accelState == null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LiveTrain liveTrain = (LiveTrain) o;

        if (emergCount != liveTrain.emergCount) {
            return false;
        }
        if (fahrCount != liveTrain.fahrCount) {
            return false;
        }
        if (!v.equals(liveTrain.v)) {
            return false;
        }
        if (!name.equals(liveTrain.name)) {
            return false;
        }
        return accelState.equals(liveTrain.accelState);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + emergCount;
        result = 31 * result + fahrCount;
        result = 31 * result + v.hashCode();
        result = 31 * result + accelState.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LiveTrain{" +
            "name='" + name + '\'' +
            ", emergCount=" + emergCount +
            ", fahrCount=" + fahrCount +
            ", v=" + v +
            ", accelState='" + accelState + '\'' +
            '}';
    }
}
