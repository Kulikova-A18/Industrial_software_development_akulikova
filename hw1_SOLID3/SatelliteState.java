public class SatelliteState {
    private boolean isActive;

    public SatelliteState(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    public void activate() {
        if (!isActive) {
            isActive = true;
        }
    }

    public void deactivate() {
        if (isActive) {
            isActive = false;
        }
    }

    @Override
    public String toString() {
        return "SatelliteState{isActive=" + isActive + "}";
    }
}