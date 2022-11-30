package team7.simple.domain.course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team7.simple.global.error.advice.exception.CAccessDeniedException;
import team7.simple.domain.course.dto.CourseDetailResponseDto;
import team7.simple.domain.course.dto.CourseRequestDto;
import team7.simple.domain.course.dto.CourseUpdateParam;
import team7.simple.domain.course.entity.Course;
import team7.simple.domain.course.error.exception.CAlreadyJoinedCourseException;
import team7.simple.domain.course.error.exception.CCourseNotFoundException;
import team7.simple.domain.course.repository.CourseJpaRepository;
import team7.simple.domain.enroll.entity.Enroll;
import team7.simple.domain.enroll.service.EnrollService;
import team7.simple.domain.user.entity.User;

import java.util.List;


@RequiredArgsConstructor
@Slf4j
@Service
public class CourseService {
    private final CourseJpaRepository courseJpaRepository;
    private final EnrollService enrollService;


    /**
     * ���¸� ����մϴ�.
     * @param courseRequestDto ���� ����, ���� ������
     * @param instructor ����
     * @return ��ϵ� ���� ���̵�
     */
    @Transactional
    public Long createCourse(CourseRequestDto courseRequestDto, User instructor) {
        Course course = courseRequestDto.toEntity(instructor);
        return courseJpaRepository.save(course).getId();
    }


    /**
     * ���� ������ ��ȸ�մϴ�.
     * @param courseId ��ȸ�� ���� ���̵�
     * @return CourseDetailResponseDto (���� ���̵�, ���� ����, ���� ������, ���� �̸�, ������ ��)
     */
    @Transactional
    public CourseDetailResponseDto getCourseInfo(Long courseId) {
        Course course = getCourseById(courseId);
        int attendeeCount = 0;
        List<Enroll> enrollList = enrollService.getStudyListByCourse(course);
        if (enrollList != null) {
            attendeeCount = enrollList.size();
        }
        return new CourseDetailResponseDto(course, attendeeCount);
    }


    /**
     * ���� ������ �����մϴ�.
     * @param courseId ���� ���̵�
     * @param courseUpdateParam ���� ����, ���� ������
     * @param user �����
     * @return ������ ���� ���̵�
     */
    @Transactional
    public Long updateCourse(Long courseId, CourseUpdateParam courseUpdateParam, User user) {
        Course course = getCourseById(courseId);
        if (!course.getInstructor().getAccount().equals(user.getAccount())) {
            throw new CAccessDeniedException();
        }
        course.update(courseUpdateParam.getTitle(), courseUpdateParam.getSubtitle());
        return courseId;
    }


    /**
     * ���¸� �����մϴ�.
     * @param courseId ������ ���� ���̵�
     * @param user �����
     */
    @Transactional
    public void deleteCourse(Long courseId, User user) {
        Course course = getCourseById(courseId);
        if (!course.getInstructor().getAccount().equals(user.getAccount())) {
            throw new CAccessDeniedException();
        }
        courseJpaRepository.delete(course);
    }


    /**
     * ���¸� �����մϴ�. �����ڿ� API�Դϴ�.
     * @param courseId ������ ���� ���̵�
     * @param courseUpdateParam ���� ����, ���� ������
     * @return ������ ���� ���̵�
     */
    @Transactional
    public Long updateCourse(Long courseId, CourseUpdateParam courseUpdateParam) {
        Course course = getCourseById(courseId);
        course.update(courseUpdateParam.getTitle(), courseUpdateParam.getSubtitle());
        return courseId;
    }


    /**
     * ���¸� �����մϴ�. �����ڿ� API�Դϴ�.
     * @param courseId ������ ���� ���̵�
     */
    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = getCourseById(courseId);
        courseJpaRepository.delete(course);
    }


    /**
     * ���¿� ���� ����մϴ�.
     * @param courseId ���� ���̵�
     * @param user �����
     */
    public void register(Long courseId, User user) {
        Course course = getCourseById(courseId);
        if (enrollService.doesEnrolled(course, user)) {
            throw new CAlreadyJoinedCourseException();
        }
        enrollService.saveStudy(course, user);
    }


    /**
     * ���¸� ���� ����մϴ�.
     * @param courseId ���� ���̵�
     * @param user �����
     */
    public void cancel(Long courseId, User user) {
        Course course = getCourseById(courseId);
        Enroll enroll = enrollService.getStudyByCourseAndUser(course, user);
        enrollService.deleteStudy(enroll);
    }


    public Course getCourseById(Long id) {
        return courseJpaRepository.findById(id).orElseThrow(CCourseNotFoundException::new);
    }
}
