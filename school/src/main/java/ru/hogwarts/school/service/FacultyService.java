package ru.hogwarts.school.service;

import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import java.util.Collection;

public interface FacultyService {
    Faculty addFaculty(Faculty faculty);
    Faculty findFaculty(long id);
    Faculty editFaculty(Faculty faculty);
    Faculty deleteFaculty(long id);

    Collection<Faculty> findAll();
    Collection<Faculty> findByNameOrColor(String search);
    Collection<Student> getStudentsByFacultyId(Long facultyId);
    String findTheLongestFacultyName();
}
