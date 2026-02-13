# Meeting Schedule API Documentation

Base URL: `http://localhost:8080/api/1.0`

All timestamps use ISO 8601 format (e.g., `2026-04-10T09:00:00Z`).

---

## Time Slots

### Create Time Slot
```
POST /api/1.0/users/{userId}/time-slots
```

**Request:**
```json
{
  "startTime": "2026-04-10T09:00:00Z",
  "endTime": "2026-04-10T10:00:00Z"
}
```

**Response (201):**
```json
{
  "id": "...",
  "startTime": "2026-04-10T09:00:00Z",
  "endTime": "2026-04-10T10:00:00Z",
  "status": "FREE",
  "hasMeeting": false,
  "createdAt": "...",
  "updatedAt": "..."
}
```

**curl:**
```bash
curl -X POST http://localhost:8080/api/1.0/users/62361f8b-bad2-40a0-9eea-e99596cf5459/time-slots \
  -H "Content-Type: application/json" \
  -d '{"startTime":"2026-04-10T09:00:00Z","endTime":"2026-04-10T10:00:00Z"}'
```

---

### Get Time Slot
```
GET /api/1.0/users/{userId}/time-slots/{slotId}
```

**Response (200):** Single time slot object.

**curl:**
```bash
curl http://localhost:8080/api/1.0/users/62361f8b-bad2-40a0-9eea-e99596cf5459/time-slots/{slotId}
```

---

### Update Time Slot
```
PUT /api/1.0/users/{userId}/time-slots/{slotId}
```

**Request:**
```json
{
  "startTime": "2026-04-10T14:00:00Z",
  "endTime": "2026-04-10T15:00:00Z"
}
```

**Response (200):** Updated time slot object.

**curl:**
```bash
curl -X PUT http://localhost:8080/api/1.0/users/62361f8b-bad2-40a0-9eea-e99596cf5459/time-slots/{slotId} \
  -H "Content-Type: application/json" \
  -d '{"startTime":"2026-04-10T14:00:00Z","endTime":"2026-04-10T15:00:00Z"}'
```

---

### Delete Time Slot
```
DELETE /api/1.0/users/{userId}/time-slots/{slotId}
```

**Response (204):** No content.

**curl:**
```bash
curl -X DELETE http://localhost:8080/api/1.0/users/62361f8b-bad2-40a0-9eea-e99596cf5459/time-slots/{slotId}
```

---

### Change Time Slot Status
```
PATCH /api/1.0/users/{userId}/time-slots/{slotId}/status
```

**Request:**
```json
{
  "status": "BUSY"
}
```

**Response (200):** Updated time slot object.

Note: Changing from `BUSY` to `FREE` on a slot with a meeting will delete the meeting.

**curl:**
```bash
curl -X PATCH http://localhost:8080/api/1.0/users/62361f8b-bad2-40a0-9eea-e99596cf5459/time-slots/{slotId}/status \
  -H "Content-Type: application/json" \
  -d '{"status":"FREE"}'
```

---

## Meetings

### Create Meeting (Convert Slot)
```
POST /api/1.0/users/{userId}/time-slots/{slotId}/meetings
```

The time slot must have status `FREE`. Creating a meeting automatically sets the slot to `BUSY`.

**Request:**
```json
{
  "title": "Daily Stand-up",
  "description": "Team sync meeting",
  "participantIds": [
    "1c906026-525b-4898-a08e-5a4fda5f868b",
    "cf3eceaa-1633-402b-9a83-59feea744509"
  ]
}
```

**Response (201):**
```json
{
  "id": "...",
  "timeSlotId": "...",
  "startTime": "2026-04-10T09:00:00Z",
  "endTime": "2026-04-10T10:00:00Z",
  "title": "Daily Stand-up",
  "description": "Team sync meeting",
  "participants": [
    {"id": "...", "name": "Seed User 2", "email": "user_2@example.com"},
    {"id": "...", "name": "Seed User 3", "email": "user_23example.com"}
  ],
  "createdAt": "...",
  "updatedAt": "..."
}
```

**curl:**
```bash
curl -X POST http://localhost:8080/api/1.0/users/62361f8b-bad2-40a0-9eea-e99596cf5459/time-slots/{slotId}/meetings \
  -H "Content-Type: application/json" \
  -d '{"title":"Daily Stand-up","description":"Team sync","participantIds":["1c906026-525b-4898-a08e-5a4fda5f868b"]}'
```

---

### Get Meeting Details
```
GET /api/1.0/users/{userId}/meetings/{meetingId}
```

**Response (200):** Full meeting object including participants.

**curl:**
```bash
curl http://localhost:8080/api/1.0/users/62361f8b-bad2-40a0-9eea-e99596cf5459/meetings/{meetingId}
```

---

## Error Responses

All errors return a standard format:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Time slot not found with id: ...",
  "details": [],
  "timestamp": "2026-04-10T09:00:00Z"
}
```

| Status | Meaning |
|--------|---------|
| 400    | Validation error or invalid input |
| 404    | Resource not found |
| 409    | Conflict (overlap, slot not available, optimistic lock) |

---

## Seed Users

The application comes with 100 pre-configured users check them in [V3__create_users_calendars.sql](src/main/resources/db/migration/V3__create_users_calendars.sql) file. 

Use their IDs to create time slots and meetings.



