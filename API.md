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
curl -X POST http://localhost:8080/api/1.0/users/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/time-slots \
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
curl http://localhost:8080/api/1.0/users/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/time-slots/{slotId}
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
curl -X PUT http://localhost:8080/api/1.0/users/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/time-slots/{slotId} \
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
curl -X DELETE http://localhost:8080/api/1.0/users/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/time-slots/{slotId}
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
curl -X PATCH http://localhost:8080/api/1.0/users/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/time-slots/{slotId}/status \
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
    "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
    "c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33"
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
    {"id": "...", "name": "Bob Smith", "email": "bob@example.com"},
    {"id": "...", "name": "Charlie Brown", "email": "charlie@example.com"}
  ],
  "createdAt": "...",
  "updatedAt": "..."
}
```

**curl:**
```bash
curl -X POST http://localhost:8080/api/1.0/users/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/time-slots/{slotId}/meetings \
  -H "Content-Type: application/json" \
  -d '{"title":"Daily Stand-up","description":"Team sync","participantIds":["b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22"]}'
```

---

### Get Meeting Details
```
GET /api/1.0/users/{userId}/meetings/{meetingId}
```

**Response (200):** Full meeting object including participants.

**curl:**
```bash
curl http://localhost:8080/api/1.0/users/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/meetings/{meetingId}
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

The application comes with 3 pre-configured users:

| Name | UUID | Email |
|------|------|-------|
| Alice Johnson | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | alice@example.com |
| Bob Smith | `b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22` | bob@example.com |
| Charlie Brown | `c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33` | charlie@example.com |


