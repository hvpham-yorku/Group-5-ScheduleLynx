package ca.yorku.eecs2311.schedulelynx.logic;

import ca.yorku.eecs2311.schedulelynx.domain.TimeBlock;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TimeBlockUtils {

  private TimeBlockUtils() {}

  public static boolean overlaps(TimeBlock a, TimeBlock b) {
    if (a.getDay() != b.getDay())
      return false;
    return a.getStart().isBefore(b.getEnd()) &&
        b.getStart().isBefore(a.getEnd());
  }

  public static List<TimeBlock>
  subtractFixedEvents(List<TimeBlock> availability,
                      List<TimeBlock> fixedEvents) {
    List<TimeBlock> free = new ArrayList<>();

    // Start with all availability blocks
    for (TimeBlock avail : availability) {
      List<TimeBlock> currentPieces = new ArrayList<>();
      currentPieces.add(avail);

      // Subtract each fixed event from the current pieces
      for (TimeBlock fixed : fixedEvents) {
        List<TimeBlock> nextPieces = new ArrayList<>();
        for (TimeBlock piece : currentPieces) {
          nextPieces.addAll(subtractOne(piece, fixed));
        }
        currentPieces = nextPieces;
      }

      free.addAll(currentPieces);
    }

    free.sort(Comparator
                  .comparing(TimeBlock::getDay,
                             Comparator.comparingInt(Enum::ordinal))
                  .thenComparing(TimeBlock::getStart));

    return free;
  }

  private static List<TimeBlock> subtractOne(TimeBlock avail, TimeBlock fixed) {
    List<TimeBlock> result = new ArrayList<>();

    if (avail.getDay() != fixed.getDay()) {
      result.add(avail);
      return result;
    }

    LocalTime aStart = avail.getStart();
    LocalTime aEnd = avail.getEnd();
    LocalTime fStart = fixed.getStart();
    LocalTime fEnd = fixed.getEnd();

    // No overlap
    boolean overlaps = aStart.isBefore(fEnd) && fStart.isBefore(aEnd);
    if (!overlaps) {
      result.add(avail);
      return result;
    }

    // Fixed fully covers availability -> nothing left
    if (!aStart.isBefore(fStart) && !fEnd.isBefore(aEnd)) {
      return result;
    }

    // Left piece
    if (aStart.isBefore(fStart)) {
      result.add(new TimeBlock(avail.getDay(), aStart, min(aEnd, fStart)));
    }

    // Right piece
    if (fEnd.isBefore(aEnd)) {
      result.add(new TimeBlock(avail.getDay(), max(aStart, fEnd), aEnd));
    }

    // Remove zero-length pieces (safety)
    result.removeIf(tb -> !tb.getStart().isBefore(tb.getEnd()));
    return result;
  }

  private static LocalTime min(LocalTime a, LocalTime b) {
    return a.isBefore(b) ? a : b;
  }

  private static LocalTime max(LocalTime a, LocalTime b) {
    return a.isAfter(b) ? a : b;
  }
}
