package ru.hogwarts.school.service;

import ru.hogwarts.school.model.Faculty;

import java.util.Collection;

public interface FacultyService {
    Faculty addFaculty(Faculty faculty);
    Faculty findFaculty(long id);
    Faculty editFaculty(Faculty faculty);
    Faculty deleteFaculty(long id);

    Collection<Faculty> findByColor(String color);
    Collection<Faculty> findAll();
}
