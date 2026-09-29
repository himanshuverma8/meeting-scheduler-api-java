package com.hv.meeting_scheduler_api_java.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hv.meeting_scheduler_api_java.TestcontainersConfiguration;
import com.hv.meeting_scheduler_api_java.domain.EventType;
import com.hv.meeting_scheduler_api_java.domain.Schedule;
import com.hv.meeting_scheduler_api_java.domain.User;
import com.hv.meeting_scheduler_api_java.repository.BookingRepository;
import com.hv.meeting_scheduler_api_java.repository.EventTypeRepository;
import com.hv.meeting_scheduler_api_java.repository.ScheduleRepository;
import com.hv.meeting_scheduler_api_java.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestRestTemplate
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingConcurrencyTest {

    private static final int CONCURRENT_REQUESTS = 10;

    @Autowired TestRestTemplate restTemplate;
    @Autowired UserRepository userRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired EventTypeRepository eventTypeRepository;
    @Autowired BookingRepository bookingRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void tenConcurrentRequestsForTheSameSlotProduceExactlyOneBooking() throws Exception {
        UUID eventTypeId = seedEventType();
        String requestBody = buildBookingRequestJson(eventTypeId, "2026-11-02T10:00:00+05:30");

        List<Integer> statusCodes = fireConcurrently(requestBody, CONCURRENT_REQUESTS);

        long successCount = statusCodes.stream().filter(code -> code == 201).count();
        long conflictCount = statusCodes.stream().filter(code -> code == 409).count();

        assertThat(successCount).isEqualTo(1);
        assertThat(conflictCount).isEqualTo(CONCURRENT_REQUESTS - 1);
        assertThat(bookingRepository.count()).isEqualTo(1);
    }

    private UUID seedEventType() {
        User host = userRepository.save(new User(
                "Concurrency Test Host", "host-" + UUID.randomUUID() + "@test.com", "irrelevant-hash"));

        Schedule schedule = scheduleRepository.save(
                new Schedule(host, "Concurrency Test Schedule", "Asia/Kolkata"));

        EventType eventType = eventTypeRepository.save(new EventType(
                host, schedule, "30 Min Meeting", 30, 10, 10, 0, 30));

        return eventType.getId();
    }

    private String buildBookingRequestJson(UUID eventTypeId, String startTime) throws Exception {
        Map<String, Object> body = Map.of(
                "eventTypeId", eventTypeId.toString(),
                "startTime", startTime,
                "inviteeName", "Concurrent Invitee",
                "inviteeEmail", "concurrent@test.com",
                "inviteeTimezone", "Asia/Kolkata"
        );
        return objectMapper.writeValueAsString(body);
    }

    private List<Integer> fireConcurrently(String requestBody, int count) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(count);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(count);
        List<Integer> statusCodes = new CopyOnWriteArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        IntStream.range(0, count).forEach(i -> executor.submit(() -> {
            try {
                startGate.await();
                ResponseEntity<String> response =
                        restTemplate.postForEntity("/bookings", entity, String.class);
                statusCodes.add(response.getStatusCode().value());
            } catch (Exception e) {
                statusCodes.add(-1);
            } finally {
                doneGate.countDown();
            }
        }));

        startGate.countDown();
        boolean completed = doneGate.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).as("all 10 requests should complete within 30s").isTrue();
        return statusCodes;
    }

}