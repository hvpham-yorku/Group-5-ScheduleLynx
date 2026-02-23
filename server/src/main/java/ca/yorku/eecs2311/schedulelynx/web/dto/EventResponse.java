package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.events.*;
import ca.yorku.eecs2311.schedulelynx.web.controller.EventController;

import java.time.LocalDateTime;

/**
 * @param id a unique number representing the specific event.
 * @see EventController for other parameters' documentation.
 */
public record EventResponse(
        long            id,
        EventType       type,
        String          name,
        String          description,
        LocalDateTime   start,
        LocalDateTime   end,
        Recurrence      recurrence,
        int             estMinutes,
        Difficulty      difficulty
) {

    public static EventResponse getFrom(AbstractEvent event) {

        var id   = event.getID();
        var type = event.getType();
        var name = event.getName();
        var desc = event.getDesc();
        var end  = event.getEndDateTime();
        var diff = event.getDifficulty();

        if (type == EventType.LECTURE) {

            var lectureEvent = (LectureEvent) event;

            var start = lectureEvent.getStartDateTime();
            var rec   = lectureEvent.getRecurrence();

            return new EventResponse(id, type, name, desc, start, end, rec, 0, diff);
        }
        if (type == EventType.LABORATORY) {

            var labEvent = (LaboratoryEvent) event;

            var start = labEvent.getStartDateTime();
            var rec   = labEvent.getRecurrence();

            return new EventResponse(id, type, name, desc, start, end, rec, 0, diff);
        }
        if (type == EventType.ASSIGNMENT) {

            var assignmentEvent = (AssignmentEvent) event;

            var estTime = assignmentEvent.getEstimatedTime();

            return new EventResponse(id, type, name, desc, null, end, null, estTime, diff);
        }
        if (type == EventType.EXAM) {

            var examEvent = (ExamEvent) event;

            var start   = examEvent.getStartDateTime();
            var estTime = examEvent.getEstimatedTime();

            return new EventResponse(id, type, name, desc, start, end, null, estTime, diff);
        }

        // Placeholder for when these are made
        
//        if (type == EventType.WORK) {
//            return null;
//        }
//        if (type == EventType.RECREATION) {
//            return null;
//        }

        return null;
    }

}