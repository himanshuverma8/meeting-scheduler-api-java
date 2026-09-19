package com.hv.meeting_scheduler_api_java;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MeetingSchedulerApiJavaApplicationTests {

	@Test
	void contextLoads() {
	}

}
