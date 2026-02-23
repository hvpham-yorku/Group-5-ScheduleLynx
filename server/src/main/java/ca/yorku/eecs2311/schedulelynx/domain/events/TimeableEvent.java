package ca.yorku.eecs2311.schedulelynx.domain.events;

public interface TimeableEvent {

    /** @return the estimated time to complete the event, in minutes. */
    public int getEstimatedTime();

    /** Overwrites the estimated time to complete the event. */
    public void setEstimatedTime(int minutes);

}
