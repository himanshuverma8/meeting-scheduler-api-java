CREATE TABLE users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name text NOT NULL,
    email text NOT NULL UNIQUE,
    hashed_password text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE schedules (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name text NOT NULL,
    timezone text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE schedule_entries (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_id uuid NOT NULL REFERENCES schedules(id) ON DELETE CASCADE,
    day_of_week integer,
    specific_date date,
    start_time time NOT NULL,
    end_time time NOT NULL
);

CREATE TABLE event_types (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    schedule_id uuid NOT NULL REFERENCES schedules(id),
    name text NOT NULL,
    duration integer NOT NULL,
    buffer_before integer NOT NULL,
    buffer_after integer NOT NULL,
    min_notice_minutes integer NOT NULL,
    max_days_in_advance integer NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE bookings (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    host_id uuid NOT NULL REFERENCES users(id),
    event_type_id uuid NOT NULL REFERENCES event_types(id),
    idempotency_key text UNIQUE,
    start_time timestamptz NOT NULL,
    end_time timestamptz NOT NULL,
    duration integer NOT NULL,
    invitee_name text NOT NULL,
    invitee_email text NOT NULL,
    invitee_timezone text NOT NULL,
    join_url text,
    host_url text,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT bookings_no_overlap EXCLUDE USING gist (
        host_id WITH =,
        tstzrange(start_time, end_time) WITH &&
    )
);