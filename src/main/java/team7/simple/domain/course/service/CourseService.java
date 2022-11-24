package team7.simple.domain.course.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team7.simple.domain.course.dto.CourseDetailResponseDto;
import team7.simple.domain.course.dto.CourseRequestDto;
import team7.simple.domain.course.dto.CourseUpdateParam;
import team7.simple.domain.course.dto.RegisterCancelRequestDto;
import team7.simple.domain.course.entity.Course;
import team7.simple.domain.course.error.exception.CAlreadyJoinedCourseException;
import team7.simple.domain.course.error.exception.CCourseNotFoundException;
import team7.simple.domain.course.repository.CourseJpaRepository;
import team7.simple.domain.study.entity.Study;
import team7.simple.domain.study.service.EnrollService;
import team7.simple.domain.user.entity.User;

import java.util.List;


@RequiredArgsConstructor
@Service
public class CourseService {
    private final CourseJpaRepository courseJpaRepository;
    private final EnrollService enrollService;

    @Transactional
    public Long createCourse(CourseRequestDto courseRequestDto, User instructor) {
        Course course = courseRequestDto.toEntity(instructor);
        return courseJpaRepository.save(course).getId();
    }

    @Transactional
    public CourseDetailResponseDto getCourseInfo(Long courseId) {
        Course course = getCourseById(courseId);
        int attendeeCount = 0;
        List<Study> studyList = enrollService.getStudyListByCourse(course);
        if (studyList != null) {
            attendeeCount = studyList.size();
        }
        return new CourseDetailResponseDto(course, attendeeCount);
    }


    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = getCourseById(courseId);
        courseJpaRepository.delete(course);
    }

    @Transactional
    public Long updateCourse(Long courseId, CourseUpdateParam courseUpdateParam) {
        Course course = getCourseById(courseId);
        course.setTitle(courseUpdateParam.getTitle());
        course.setSubtitle(courseUpdateParam.getSubtitle());
        return courseId;
    }

    public Long register(Long courseId, User user) {
        Course course = getCourseById(courseId);
        if (enrollService.doesEnrolled(course, user)){
            throw new CAlreadyJoinedCourseException();
        }
        return enrollService.saveStudy(course, user);
    }

    public void cancel(Long courseId, User user) {
        Course course = getCourseById(courseId);
        Study study = enrollService.getStudyByCourseAndUser(course, user);
        enrollService.deleteStudy(study);
    }

    public Course getCourseById(Long id) {
        return courseJpaRepository.findById(id).orElseThrow(CCourseNotFoundException::new);
    }
}
