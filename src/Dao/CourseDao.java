package Dao;

import java.util.List;

import Models.*;

public interface CourseDao {
	public List<Course> getAllCourse();
	public List<Course> getAllCourseByclass(String classify);
}