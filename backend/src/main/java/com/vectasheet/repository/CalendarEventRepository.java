package com.vectasheet.repository;

import com.vectasheet.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {
    List<CalendarEvent> findByWorkspaceIdOrderByStartAtAsc(UUID workspaceId);
}
