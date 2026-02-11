-- Covering index for overlap detection queries
CREATE INDEX idx_time_slots_calendar_start_end ON time_slots(calendar_id, start_time, end_time);
