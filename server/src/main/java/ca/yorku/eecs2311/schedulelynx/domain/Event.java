package ca.yorku.eecs2311.schedulelynx.domain;

import java.time.LocalTime;

public class Event extends TimeBlock {

    private Long id;
    private String title;

    public Event(
            Long id,
            String title,
            Weekday day,
            LocalTime start,
            LocalTime end)
    {
        super(day, start, end);
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
