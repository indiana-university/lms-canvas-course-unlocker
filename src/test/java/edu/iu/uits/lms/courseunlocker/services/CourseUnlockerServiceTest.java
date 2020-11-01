package edu.iu.uits.lms.courseunlocker.services;

import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.model.CanvasTerm;
import canvas.client.generated.model.Course;
import canvas.helpers.CanvasDateFormatUtil;
import edu.iu.uits.lms.courseunlocker.config.ToolConfig;
import edu.iu.uits.lms.courseunlocker.controller.CourseUnlockerController;
import edu.iu.uits.lms.courseunlocker.model.CourseUnlockResponseObject;
import edu.iu.uits.lms.courseunlocker.model.CourseUnlockStatus;
import edu.iu.uits.lms.courseunlocker.rest.CourseUnlockerRestController;
import edu.iu.uits.lms.courseunlocker.service.CourseUnlockerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@RunWith(SpringRunner.class)
@WebMvcTest(CourseUnlockerController.class)
@Import(ToolConfig.class)
@ActiveProfiles("none")
@Ignore
public class CourseUnlockerServiceTest {

	private static final String COURSE_ID_PFX = "TESTCOURSEID";
	private static final String COURSE_ACCT_ID_PFX = "TESTCOURSEACCTID";
	// course code = SIS site id
	private static final String COURSE_CODE_PFX = "TESTCOURSECODE";
	private static final String COURSE_NAME_PFX = "Test Course ";

	private static final int NUM_COURSES = 6;

	public static final String END_DATE_STRING = "2016-12-01T00:00:00Z";

	@MockBean
	private CoursesApi coursesApi;

	@Autowired
	private CourseUnlockerService cus;

	@Before
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

			Mockito.when(coursesApi.getCourse(COURSE_ID_PFX + i)).thenReturn(course);
		}
	}

	@Test
	public void testValidateServices() throws Exception {
		Assert.assertNotNull("CoursesApi is null", coursesApi);
		Assert.assertNotNull("CourseUnlockerService is null", cus);

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
		Mockito.when(coursesApi.getCourse(course.getId())).thenReturn(course);

		CourseUnlockStatus status = cus.getCourseUnlockStatus(course.getId());

		Assert.assertEquals("statuses do not match", expectedStatus, status);
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

