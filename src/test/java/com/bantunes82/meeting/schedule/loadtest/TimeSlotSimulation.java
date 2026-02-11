package com.bantunes82.meeting.schedule.loadtest;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Gatling load test simulation for all TimeSlot endpoints.
 * <p>
 * Covers: POST (create), GET (read), PUT (update), PATCH (change status),
 * DELETE.
 * Each virtual user performs a full CRUD lifecycle on a unique time slot.
 * <p>
 * Run with:
 *
 * <pre>
 *   ./mvnw gatling:test -Dgatling.simulationClass=com.bantunes82.meeting.schedule.loadtest.TimeSlotSimulation
 * </pre>
 */
public class TimeSlotSimulation extends Simulation {

  private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
  private static final Duration DURATION = Duration.ofMillis(500);

  private static final String[] SEED_USER_IDS = {
      "62361f8b-bad2-40a0-9eea-e99596cf5459", "1c906026-525b-4898-a08e-5a4fda5f868b",
      "cf3eceaa-1633-402b-9a83-59feea744509", "9b55dad7-3930-408d-8084-45051a49994d",
      "ed018be2-c3f6-4f4a-a2bf-e45a58598cc2", "558bf7cc-7ce9-4439-a0cd-83edab9e1f71",
      "07585b31-6319-4738-a72b-8594a7d09f71", "1732f3dc-fe5b-4879-b5f1-f5b9d0a4f095",
      "9b3f90cc-37cf-40d4-96b2-9f99f7d1c465", "3b6d9ec2-5b91-4b8f-bfef-61e1db05c41e",
      "d452fa71-c846-4e58-9401-4306831d1540", "63c72fc1-2be5-417c-9313-811b2c9122a5",
      "5acc7a0c-ff1b-4610-989a-ac2278933954", "0d978511-2c93-42a7-884f-1cc7fab49dc1",
      "8e1e27b0-7c50-4117-9865-a84a849f2670", "3e91b964-3754-4a6c-9b52-bf58705479f4",
      "e818af8b-7e3b-4915-8559-b90c67cd1be6", "0424340f-97ba-4112-8e39-5223d6080452",
      "f7474df0-0c78-4197-b019-b05d30753978", "8dbccfb8-8eb9-4ae0-96e3-990c0fbb6c57",
      "f12ea494-11b8-4bb6-a7f5-0edea03e304c", "a71e7fd6-6f8b-4c13-9a63-654d3c0221d5",
      "729991e9-3b5c-4d5d-b826-54c484f2e189", "f873918c-0d47-4150-8671-243a3e5ae98a",
      "3cd222e2-e729-4ad3-a83d-59516ba9afae", "a31aebdb-4e8d-4570-a947-7ecbdd6b7d21",
      "48af7726-221f-4b8f-b30a-031d1943bb6c", "3d725ca7-7808-49a9-b9a8-ab05be9d5a83",
      "ef63567a-061c-4fe7-a3b7-cc212dc3d4c0", "d912aa46-748c-47bd-99b2-3d0233ed7414",
      "b1d652e6-a892-4d13-a355-610b05c662f7", "f3be6d8d-1895-4c62-94fa-91255a0409f9",
      "69109b6f-7381-4a01-a78f-0757a5dbd469", "1fd0972b-a847-450a-8079-c8ecf6140632",
      "7712f515-2598-4b0d-94cb-f8ab4c1c0d6f", "2d55dea3-cecd-4108-9023-ebb693f3e2c8",
      "9e5cacab-0c65-4a4b-8080-f27d7c7ca6ee", "da330afd-e59e-421c-ad15-5f79e8ca8363",
      "c7dccdfe-c983-44a0-a9b7-f3197a3836b4", "9085d2d7-1ff0-4dfb-a7fd-f46b92ca6846",
      "4c7bbf0e-cff5-4e52-a39d-0ea63f583a2d", "9e1655f4-1d42-4105-8f6d-e36c5e1c7c06",
      "88684822-b944-4237-bd7d-aec75166f351", "d3b51b3b-111d-4ee8-a6f8-89ba542aeebe",
      "301ed156-c078-4db2-9c79-8442e8e19ceb", "d0f68065-87c9-4dfb-9bd2-0ea6ab74fac4",
      "9e369750-eba3-48db-81d9-ec8a223c99f2", "0fbcc6c3-ec78-403d-8788-f9be3adbea3a",
      "1c64a98f-803e-4340-99b8-0a20632a9def", "a39fb734-1082-477e-a769-80cfb5162118",
      "3b78d4f1-2d0f-4330-973c-a6ce6a45bf78", "c6c1839a-ef91-4626-8b99-d9e31546a821",
      "990819ef-cf45-48ae-b93e-29738139819c", "9253c65a-29f8-49cd-88b9-92eb35f258fa",
      "fd220b20-05cc-425a-8417-b205b9e4ec49", "f22f290e-397c-4c16-8a34-ff5d42f1409d",
      "0613e3d2-5ef2-491f-88ca-719f3728d7c4", "ae6ad64c-cbb7-4894-897d-d0dfcd134d82",
      "773c352f-0500-4ead-9d5b-248a52d0dc04", "f182025a-bcc1-44a2-88dd-7ac340f1ad38",
      "da943976-2aec-44b2-8338-a3e0877c095d", "6fa4cb86-7298-4ff6-a188-e8c2b704fdb5",
      "7949076d-b2eb-4a8c-b665-611467d6020b", "d8dbd434-7c4e-4cb5-a861-a445685e5fd4",
      "aec0c2d7-9cff-4be6-8fa6-01cf54f85dc2", "edb7eaff-7eb9-4686-8702-935df743f62c",
      "5b72bf31-1ed0-480f-9f6f-8dcfeff674d2", "d9df2c6d-f457-4840-8f4a-bea6d1ba6219",
      "efab936d-8c73-4277-b3fd-9a36106ad494", "e42cb2ae-7da8-46d8-b791-58747e2b8c27",
      "94fd4285-7a03-4058-b786-f4df1fa26bd3", "e40f1d90-7037-4e4d-b649-d5fe39d289a0",
      "fe8add1e-c42b-4b03-a7e2-217e2069eb83", "a8af9a6a-cb4e-4934-a2ba-4e79c75f11b4",
      "0528ebc1-8a07-4b24-98db-ee2f111335f4", "e65d6631-533a-457d-b2be-913679281fe9",
      "d21af226-47e3-4e1b-ac54-19d305fbfb70", "60e6784b-de95-4da9-8666-9479a9b328d7",
      "bd357b44-e612-4571-832f-2fcfb599d804", "285198a8-0c7d-4ce8-9b78-8daa43648d71",
      "3de4c932-acc1-48e4-8f69-eb829dd526be", "21ea70cc-9f94-4357-96a0-b789a29b27b7",
      "67dbed9f-0dfe-4967-a037-bfe0231ad6e9", "18ddc457-b978-4518-92f9-18882c5b2fda",
      "f570abdd-9b11-4365-832b-2871f853f2d5", "f8872e5a-ac16-4f65-81b7-46a664f2f6cc",
      "cc3a1bad-5bee-4604-b068-9681a3a60f92", "788dc608-6745-4e17-b49c-0f578f9ce59b",
      "375dd288-b418-4908-ac8e-272d510ea28a", "907ae56b-194f-4d45-b1da-1521f6536cd7",
      "5720b117-19e9-45de-a754-3deb0288cc2b", "9d9a99a6-5946-4148-ae39-6d91643feee5",
      "97ba0096-7132-4cdf-b55f-bba9df9e2734", "9f0b9019-fe72-4098-ba1b-d45bfb864562",
      "abc2a74c-b395-4b68-9318-0571d04d8d99", "300fb057-a6d1-4044-b981-69133e870369",
      "c425cbaa-7738-487c-90ca-c1ac71c54548", "8215d8e9-44ea-461d-88c7-5a5cb1054b81",
      "3435200c-b030-457d-a13c-10b9793050f3", "2ca60f1b-f2c9-4d09-bef3-d6d9a75de835",
  };
  private static final AtomicInteger HOUR_OFFSET = new AtomicInteger(1 + new Random().nextInt(100000000));
  private final HttpProtocolBuilder httpProtocol = http.baseUrl(BASE_URL).acceptHeader("application/json")
      .contentTypeHeader("application/json");

  private final ScenarioBuilder scenario = scenario("TimeSlot CRUD Lifecycle")
      .feed(timeSlotFeeder())
      .exec(http("Create TimeSlot")
          .post("/api/1.0/users/#{userId}/time-slots")
          .body(StringBody("""
              {
                "startTime": "#{startTime}",
                "endTime": "#{endTime}"
              }
              """))
          .check(status().is(201))
          .check(jsonPath("$.id").saveAs("slotId")))
      .exitHereIfFailed()
      .pause(DURATION)
      .exec(http("Get TimeSlot")
          .get("/api/1.0/users/#{userId}/time-slots/#{slotId}")
          .check(status().is(200))
          .check(jsonPath("$.id").is(session -> session.getString("slotId"))))
      .pause(DURATION)
      .exec(http("Update TimeSlot")
          .put("/api/1.0/users/#{userId}/time-slots/#{slotId}")
          .body(StringBody("""
              {
                "startTime": "#{updatedStartTime}",
                "endTime": "#{updatedEndTime}"
              }
              """))
          .check(status().is(200))
          .check(jsonPath("$.id").is(session -> session.getString("slotId"))))
      .pause(DURATION)
      .exec(http("Update TimeSlot Status to BUSY")
          .patch("/api/1.0/users/#{userId}/time-slots/#{slotId}/status")
          .body(StringBody("""
              {
                "status": "BUSY"
              }
              """))
          .check(status().is(200))
          .check(jsonPath("$.status").is("BUSY")))
      .pause(DURATION)
      .exec(http("Delete TimeSlot")
          .delete("/api/1.0/users/#{userId}/time-slots/#{slotId}")
          .check(status().is(204)));

  {

    setUp(scenario.injectOpen(
        // Warmup
        rampUsersPerSec(0).to(100).during(Duration.ofSeconds(10)),
        // Load to peak
        rampUsersPerSec(100).to(500).during(Duration.ofSeconds(30)),
        // Hold peak
        constantUsersPerSec(500).during(Duration.ofSeconds(60))))
        .protocols(httpProtocol)
        .assertions(
            // percentage of successful requests should be greater than 90
            global().successfulRequests().percent().gt(95.0));
  }

  /**
   * Feeder that cycles through seed users and generates unique future timestamps.
   */
  private static Iterator<Map<String, Object>> timeSlotFeeder() {
    AtomicInteger counter = new AtomicInteger(0);
    Supplier<Map<String, Object>> supplier = () -> {
      int idx = counter.getAndIncrement();
      String userId = SEED_USER_IDS[idx % SEED_USER_IDS.length];
      int offset = HOUR_OFFSET.getAndAdd(4);
      Instant start = Instant.now().plus(Duration.ofHours(offset));
      Instant end = start.plus(Duration.ofHours(1));
      Instant updatedStart = end.plus(Duration.ofMinutes(30));
      Instant updatedEnd = updatedStart.plus(Duration.ofHours(1));
      return Map.of("userId", userId,
          "startTime", start.toString(),
          "endTime", end.toString(),
          "updatedStartTime", updatedStart.toString(),
          "updatedEndTime", updatedEnd.toString());
    };
    return Stream.generate(supplier).iterator();
  }
}
