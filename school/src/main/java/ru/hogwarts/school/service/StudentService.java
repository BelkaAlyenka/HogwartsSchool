package ru.hogwarts.school.service;

import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;

import java.io.IOException;
import java.util.Collection;

public interface StudentService {
    Student addStudent(Student student);
    Student findStudent(long id);
    Student editStudent(Student student);
    Student deleteStudent(long id);

    Collection<Student> findByAge(int age);
    Collection<Student> findByAgeBetween(int min, int max);
    Collection<Student> findAll();
    Faculty getFacultyByStudentId(Long studentId);

    Avatar findAvatar(long studentId);
    void uploadAvatar(Long studentId, MultipartFile file) throws IOException;
}
