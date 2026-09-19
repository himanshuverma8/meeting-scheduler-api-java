package com.hv.meeting_scheduler_api_java;

import org.springframework.boot.SpringApplication;

public class TestMeetingSchedulerApiJavaApplication {

	public static void main(String[] args) {
		SpringApplication.from(MeetingSchedulerApiJavaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
