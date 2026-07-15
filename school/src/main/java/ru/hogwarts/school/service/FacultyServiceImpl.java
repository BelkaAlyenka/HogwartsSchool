package ru.hogwarts.school.service;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

@Service
public class FacultyServiceImpl implements FacultyService {

    private static final Logger logger = LoggerFactory.getLogger(FacultyServiceImpl.class);

    private final FacultyRepository facultyRepository;

    public FacultyServiceImpl(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    @Override
    public Faculty addFaculty(Faculty faculty) {
        logger.info("Was invoked method for add faculty");
        return facultyRepository.save(faculty);
    }

    @Override
    public Faculty findFaculty(long id) {
        logger.info("Was invoked method for finding faculty");
        return facultyRepository.findById(id).orElse(null);
    }

    @Override
    public Faculty editFaculty(Faculty faculty) {
        logger.info("Was invoked method for edit faculty");
        if (facultyRepository.existsById(faculty.getId())) {
            return facultyRepository.save(faculty);
        }
        logger.error("No faculty with id={}", faculty.getId());
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Факультет не найден");
    }

    @Override
    public Faculty deleteFaculty(long id) {
        logger.info("Was invoked method for deleting faculty");
        Faculty faculty = findFaculty(id);
        if (faculty != null) {
            facultyRepository.deleteById(id);
            return faculty;
        }
        logger.error("No faculty with id={}", id);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Факультет не найден");
    }

    @Override
    public Collection<Faculty> findAll() {
        logger.info("Was invoked method for finding all faculties");
        return facultyRepository.findAll();
    }

    @Override
    public Collection<Faculty> findByNameOrColor(String search) {
        logger.info("Was invoked method for finding faculty by name or color");
        if (search == null || search.trim().isEmpty()) {
            logger.warn("В метод была передана пустая поисковая строка");
        }
        return facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase(search, search);
    }

    @Override
    public Collection<Student> getStudentsByFacultyId(Long facultyId) {
        logger.info("Was invoked method for getting students by faculty id");
        return facultyRepository.findById(facultyId)
                .map(Faculty::getStudents)
                .orElseThrow(() -> {
                    logger.error("No faculty with id={}", facultyId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Факультет не найден");
                });
    }
}
