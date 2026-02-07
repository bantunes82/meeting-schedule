-- Composite indexes for efficient time slot queries

-- Range queries: list slots in a time window
CREATE INDEX idx_time_slots_calendar_start ON time_slots(calendar_id, start_time);

-- Free/busy filtering: list slots by status in a time window
CREATE INDEX idx_time_slots_calendar_status_start ON time_slots(calendar_id, status, start_time);

-- Covering index for overlap detection queries
CREATE INDEX idx_time_slots_calendar_start_end ON time_slots(calendar_id, start_time, end_time);
