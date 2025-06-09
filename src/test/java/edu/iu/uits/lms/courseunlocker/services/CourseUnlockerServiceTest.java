package edu.iu.uits.lms.courseunlocker.services;

/*-
 * #%L
 * course-unlocker
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import edu.iu.uits.lms.canvas.helpers.CanvasDateFormatUtil;
import edu.iu.uits.lms.canvas.model.CanvasTerm;
import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.courseunlocker.config.ToolConfig;
import edu.iu.uits.lms.courseunlocker.model.CourseUnlockStatus;
import edu.iu.uits.lms.courseunlocker.service.CourseUnlockerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@WebMvcTest(properties = {"oauth.tokenprovider.url=http://foo"})
@Import(ToolConfig.class)
@ActiveProfiles("none")
@Disabled
public class CourseUnlockerServiceTest {

	private static final String COURSE_ID_PFX = "TESTCOURSEID";
	private static final String COURSE_ACCT_ID_PFX = "TESTCOURSEACCTID";
	// course code = SIS site id
	private static final String COURSE_CODE_PFX = "TESTCOURSECODE";
	private static final String COURSE_NAME_PFX = "Test Course ";

	private static final int NUM_COURSES = 6;

	public static final String END_DATE_STRING = "2016-12-01T00:00:00Z";

	@MockitoBean
	private CourseService courseService;

	@Autowired
	private CourseUnlockerService cus;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);

		// Set up our mock data 
		List<Course> courseList = new ArrayList<>();

		for (int i=0; i<NUM_COURSES; i++) {
			Course course = new Course();
			course.setId(COURSE_ID_PFX + i);
			course.setAccountId(COURSE_ACCT_ID_PFX + i);
			course.setName(COURSE_NAME_PFX + i);
			course.setCourseCode(COURSE_CODE_PFX + i);
            course.setSisCourseId(COURSE_ID_PFX + i + "_SIS");
			courseList.add(course);

			Mockito.when(courseService.getCourse(COURSE_ID_PFX + i)).thenReturn(course);
		}
	}

	@Test
	public void testValidateServices() throws Exception {
		Assertions.assertNotNull(courseService, "CourseService is null");
		Assertions.assertNotNull(cus, "CourseUnlockerService is null");

	}

	/**
	 * Build a simple course object that can be used for tests
	 * @param courseEnd String representation of the course end date
	 * @param termEnd String representation of the term end date
	 * @param restrictEnrollmentsToCourseDates Flag indicating if the course access should be restricted by the course dates
	 * @return Course
	 */
	private Course buildSimpleCourse(String termEnd, String courseEnd, boolean restrictEnrollmentsToCourseDates) {
		Course course = new Course();
		course.setId("1");
		course.setEndAt(courseEnd);
		course.setRestrictEnrollmentsToCourseDates(restrictEnrollmentsToCourseDates);

		CanvasTerm canvasTerm = new CanvasTerm();
		canvasTerm.setEndAt(termEnd);

		course.setTerm(canvasTerm);
		return course;
	}

	/**
	 * Helper for validating course unlock response objects
	 * @param course
	 * @param expectedStatus
	 * @throws Exception
	 */
	private void testCourseUnlockStatus(Course course, CourseUnlockStatus expectedStatus) throws Exception {
		Mockito.when(courseService.getCourse(course.getId())).thenReturn(course);

		CourseUnlockStatus status = cus.getCourseUnlockStatus(course.getId());

		Assertions.assertEquals(expectedStatus, status, "statuses do not match");
	}

	@Test
	public void testCourseUnlockStatus_NoTermEndNoCourseEnd() throws Exception {
		//Case1
		Course course = buildSimpleCourse(null, null, false);
		CourseUnlockStatus status = new CourseUnlockStatus(false, false, course.getId());
		testCourseUnlockStatus(course, status);
	}

	@Test
	public void testCourseUnlockStatus_NoTermEndCourseEndNotPassed() throws Exception {
		//Case2
		Course course = buildSimpleCourse(null, getFutureDate(null), false);
		CourseUnlockStatus status = new CourseUnlockStatus( false, false, course.getId());
		testCourseUnlockStatus(course, status);
	}

	@Test
	public void testCourseUnlockStatus_NoTermEndCourseEndPassed() throws Exception {
		//Case3
		Course course = buildSimpleCourse(null, END_DATE_STRING, false);
		CourseUnlockStatus status = new CourseUnlockStatus( false, false, course.getId());
		testCourseUnlockStatus(course, status);
	}

	@Test
	public void testCourseUnlockStatus_TermEndNotPassedNoCourseEnd() throws Exception {
		//Case4
		Course course = buildSimpleCourse(getFutureDate(null), null, false);
		CourseUnlockStatus status = new CourseUnlockStatus(false, false, course.getId());
		testCourseUnlockStatus(course, status);

	}

	@Test
	public void testCourseUnlockStatus_TermEndNotPassedCourseEndNotPassed() throws Exception {
		//Case5
		Course course = buildSimpleCourse(getFutureDate(null), getFutureDate(null), false);
		CourseUnlockStatus status = new CourseUnlockStatus(false, false, course.getId());
		testCourseUnlockStatus(course, status);

	}

	@Test
	public void testCourseUnlockStatus_TermEndNotPassedCourseEndPassed() throws Exception {
		//Case6
		Course course = buildSimpleCourse(getFutureDate(null), END_DATE_STRING, false);
		CourseUnlockStatus status = new CourseUnlockStatus(false, false, course.getId());
		testCourseUnlockStatus(course, status);

	}

	@Test
	public void testCourseUnlockStatus_TermEndPassedNoCourseEnd() throws Exception {
		//Case7
		Course course = buildSimpleCourse(END_DATE_STRING, null, false);
		CourseUnlockStatus status = new CourseUnlockStatus(true, true, course.getId());
		testCourseUnlockStatus(course, status);

	}

	@Test
	public void testCourseUnlockStatus_TermEndPassedCourseEndNotPassedNoRestrict() throws Exception {
		//Case8
		Course course = buildSimpleCourse(END_DATE_STRING, getFutureDate(null), false);
		CourseUnlockStatus status = new CourseUnlockStatus(true, true, course.getId());
		testCourseUnlockStatus(course, status);

	}

	@Test
	public void testCourseUnlockStatus_TermEndPassedCourseEndNotPassedRestrict() throws Exception {
		//Case9
		Course course = buildSimpleCourse(END_DATE_STRING, getFutureDate(null), true);
		CourseUnlockStatus status = new CourseUnlockStatus(true, false, course.getId());
		testCourseUnlockStatus(course, status);

	}

	@Test
	public void testCourseUnlockStatus_TermEndPassedCourseEndPassed() throws Exception {
		//Case10
		Course course = buildSimpleCourse(END_DATE_STRING, END_DATE_STRING, false);
		CourseUnlockStatus status = new CourseUnlockStatus(true, true, course.getId());
		testCourseUnlockStatus(course, status);

	}

	private String getFutureDate(String startingDate) {
		ZonedDateTime startDateTime;
		if (startingDate == null) {
			startDateTime = ZonedDateTime.now(ZoneOffset.UTC);
		} else {
			startDateTime = ZonedDateTime.parse(startingDate);
		}

		Date futureDate = Date.from(startDateTime.plusDays(30).toInstant());
		SimpleDateFormat canvasDateFormat = new SimpleDateFormat(CanvasDateFormatUtil.CANVAS_DATE_FORMAT);
		return canvasDateFormat.format(futureDate);
	}


}

