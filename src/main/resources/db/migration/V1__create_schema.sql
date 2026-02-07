-- Enable btree_gist extension for exclusion constraints
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Users table
CREATE TABLE users (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Calendars table (one per user)
CREATE TABLE calendars (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Time slots table
CREATE TABLE time_slots (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    calendar_id UUID NOT NULL REFERENCES calendars(id) ON DELETE CASCADE,
    start_time  TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time    TIMESTAMP WITH TIME ZONE NOT NULL,
    status      VARCHAR(10) NOT NULL DEFAULT 'FREE' CHECK (status IN ('FREE', 'BUSY')),
    version     INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT chk_time_slot_duration CHECK (end_time > start_time),

    -- Database-enforced overlap prevention: no two slots in the same calendar can overlap
    CONSTRAINT excl_no_overlapping_slots EXCLUDE USING gist (
        calendar_id WITH =,
        tstzrange(start_time, end_time) WITH &&
    )
);

-- Meetings table (one-to-one with time slot)
CREATE TABLE meetings (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    time_slot_id UUID NOT NULL UNIQUE REFERENCES time_slots(id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Meeting participants join table
CREATE TABLE meeting_participants (
    meeting_id UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (meeting_id, user_id)
);

-- Indexes on join table for bidirectional lookups
CREATE INDEX idx_meeting_participants_user_id ON meeting_participants(user_id);
