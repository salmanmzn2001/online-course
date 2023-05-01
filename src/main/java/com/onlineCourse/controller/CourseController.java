package com.onlineCourse.controller;


import com.onlineCourse.entities.Course;
import com.onlineCourse.entities.CourseEnrollment;
import com.onlineCourse.repository.CourseEnrollmentRepository;
import com.onlineCourse.repository.CourseRepository;
import com.onlineCourse.repository.UserRepository;
import com.onlineCourse.service.interfaces.CourseService;
import com.onlineCourse.service.interfaces.UserService;
import com.onlineCourse.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.onlineCourse.entities.User;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Controller
@Slf4j
public class CourseController {

	@Autowired
	private CourseEnrollmentRepository courseEnrollmentRepository;
  	@Autowired
	private CourseRepository courseRepository;
	@Autowired
	private UserService userService;

	@Autowired
	private CourseService courseService;

	@GetMapping("/courses")
	public String courses(Model model) {
		List<Course> courseList = courseService.getCourseList();
		log.info("courseList : " + courseList);
		model.addAttribute("title", "Courses");
		model.addAttribute("courseList", courseList);
		return "courses/courses";
	}

	@PostMapping("/search")
	public String search( @RequestParam(value = "searchText") String searchText, Model model) {
		log.info("Search Criteria : " + searchText);
		List<Course> courseList = courseService.search(searchText);
		log.info("courseList : " + courseList);
		model.addAttribute("title", "Courses");
		model.addAttribute("courseList", courseList);
		return "courses/courses";
	}
	@GetMapping("/coursedetail")
	public String courseDetail(@ModelAttribute("user") Course course,  Model model) {
		//log.info("Course : " + course);
		//log.info("User : " + user);

		//model.addAttribute("user ",user);
		List<Course> courseList = courseService.getCourseList();
		log.info("courseList : " + courseList);
		model.addAttribute("title", "Courses");
		model.addAttribute("courseList", courseList);
		return "courses/course-detail" ;

	}

	@RequestMapping(value = "/enroll/{id}", method = RequestMethod.GET)
	public String enrollUser(HttpSession session, @PathVariable("id") int courseId, Model model) {
		log.info("Course for Enrollment : " +  courseId);
		User sessionUser = (User) session.getAttribute("user");
		CourseEnrollment courseEnrollment = new CourseEnrollment();
		courseEnrollment.setCourseId(courseId);
		courseEnrollment.setUserId(sessionUser.getId());
		courseEnrollment.setUserName(sessionUser.getName());
		CourseEnrollment dbEnrollment = courseEnrollmentRepository.save(courseEnrollment);

		model.addAttribute("success", sessionUser.getName() + " successfully enrolled for courseId : " + courseId);
		log.info("success" +  sessionUser.getName() + " successfully enrolled for courseId : " + courseId);
		return myCourses(session, model);
	}

	@GetMapping(value = "/init-add-course")
	public String initAddCourse(Model model) {
		model.addAttribute("course", new Course());
		model.addAttribute("title", "Add Course");
		log.info("loading init-add-course..!");
		return "courses/add-course";
	}

	@RequestMapping(value = "/submit-add-course", method = RequestMethod.POST)
	public String submitAddCourse(@ModelAttribute("course") Course course, Model model) {
		Course dbCourse = courseRepository.save(course);
		model.addAttribute("success", dbCourse.getCourseName() + " added successfully. You can continue to add more..!");
		log.info("success - " + course.getCourseName() + " added successfully. Course =  : " + dbCourse);
		return initAddCourse(model);
	}

	@RequestMapping(value = "/init-manage-course", method = RequestMethod.POST)
	public String initManageCourse(@ModelAttribute("course") Course course, Model model) {
		model.addAttribute("course", course);
		model.addAttribute("title", "Manage Course");
		log.info("loading init-manage-course..! course=" + course);
		return "courses/manage-course";
	}

	@RequestMapping(value = "/submit-manage-course", method = RequestMethod.POST)
	public String submitManageCourse(@ModelAttribute("course") Course course, Model model) {
		log.info("course=" + course);
		if(course.getId()>0){
			Course dbCourse = courseRepository.save(course);
			model.addAttribute("success", dbCourse.getCourseName() + " updated successfully.");
			return courses(model);
		}
		model.addAttribute("error", course.getCourseName() + " update failed.");
		return courses(model);
	}

	@RequestMapping(value = "/delete-course/{id}", method = RequestMethod.GET)
	public String deleteCourse(@PathVariable("id") int id, Model model) {
		log.info("Id=" + id);
		if(id>0){
			try {
				courseRepository.deleteById(id);
				model.addAttribute("success", "Course with " + id + " deleted successfully.");
			} catch (Exception e) {
				log.error("Error : " + e.getLocalizedMessage());
				model.addAttribute("error", "Course with id : " + id + " delete failed with exception.");			}
			return courses(model);
		}
		model.addAttribute("error", "Course with id : " + id + " delete failed.");
		return courses(model);
	}

	@GetMapping("/my-courses")
	public String myCourses(HttpSession session,  Model model) {
		User sessionUser = (User) session.getAttribute("user");
		List<Course> courseList = courseService.getEnrolledCourseList(sessionUser.getId());

		log.info("courseList : " + courseList);
		model.addAttribute("title", "My Courses");
		model.addAttribute("courseList", courseList);
		return "courses/courses";
	}


	//courseDetail - Course

}