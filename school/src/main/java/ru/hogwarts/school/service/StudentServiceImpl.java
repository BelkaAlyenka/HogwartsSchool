package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    @Value("${avatars.dir.path}")
    private String avatarsDir;

    private final StudentRepository studentRepository;
    private final AvatarRepository avatarRepository;

    public StudentServiceImpl(StudentRepository studentRepository, AvatarRepository avatarRepository) {
        this.studentRepository = studentRepository;
        this.avatarRepository = avatarRepository;
    }

    @Override
    public Student addStudent(Student student) {
        logger.info("Was invoked method for add student");
        student.setId(null);
        return studentRepository.save(student);
    }

    @Override
    public Student findStudent(long id) {
        logger.info("Was invoked method for find student");
        return studentRepository.findById(id).orElse(null);
    }

    @Override
    public Student editStudent(Student student) {
        logger.info("Was invoked method for edit student");
        if (studentRepository.existsById(student.getId())) {
            return studentRepository.save(student);
        }
        logger.error("No student with id={}", student.getId());
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
    }

    @Override
    public Student deleteStudent(long id) {
        logger.info("Was invoked method for delete student");
        Student student = findStudent(id);
        if (student != null) {
            studentRepository.deleteById(id);
            return student;
        }
        logger.error("No student with id={}", id);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
    }

    @Override
    public Collection<Student> findByAge(int age) {
        logger.info("Was invoked method for find students by age");
        return studentRepository.findByAge(age);
    }

    @Override
    public Collection<Student> findAll() {
        logger.info("Was invoked method for find all students");
        return studentRepository.findAll();
    }

    @Override
    public Collection<Student> findByAgeBetween(int min, int max) {
        logger.info("Was invoked method for find students by age between");
        return studentRepository.findByAgeBetween(min, max);
    }

    @Override
    public Faculty getFacultyByStudentId(Long studentId) {
        logger.info("Was invoked method for get faculty by student id");
        return studentRepository.findById(studentId)
                .map(Student::getFaculty)
                .orElseThrow(() -> {
                    logger.error("No student with id={}", studentId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Студент не найден");
                });
    }

    @Override
    public Avatar findAvatar(long studentId) {
        logger.info("Was invoked method for find avatar");
        return avatarRepository.findByStudentId(studentId).orElse(null);
    }

    @Override
    public void uploadAvatar(Long studentId, MultipartFile file) throws IOException {
        logger.info("Was invoked method for upload avatar");
        if (file.isEmpty()) {
            logger.warn("Попытка загрузить пустой файл аватара для студента с id={}", studentId);
        }
        Student student = findStudent(studentId);
        if (student == null) {
            logger.error("No student with id={}", studentId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }

        Path filePath = Path.of(avatarsDir, studentId + "." + getExtension(file.getOriginalFilename()));
        Files.createDirectories(filePath.getParent());
        Files.deleteIfExists(filePath);

        try (InputStream is = file.getInputStream();
             OutputStream os = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW);
             BufferedInputStream bis = new BufferedInputStream(is, 1024);
             BufferedOutputStream bos = new BufferedOutputStream(os, 1024)
        ) {
            bis.transferTo(bos);
        }

        Avatar avatar = avatarRepository.findByStudentId(studentId).orElseGet(Avatar::new);
        avatar.setStudent(student);
        avatar.setFilePath(filePath.toString());
        avatar.setFileSize(file.getSize());
        avatar.setMediaType(file.getContentType());
        avatar.setData(file.getBytes());

        avatarRepository.save(avatar);
    }

    @Override
    public Integer getCountOfAllStudents() {
        logger.info("Was invoked method for get count of all students");
        return studentRepository.getCountOfAllStudents();
    }

    @Override
    public Double getAverageAge() {
        logger.info("Was invoked method for get average age of students");
        return studentRepository.getAverageAge();
    }

    @Override
    public Collection<Student> getLastFiveStudents() {
        logger.info("Was invoked method for get last five students");
        return studentRepository.getLastFiveStudents();
    }

    @Override
    public Collection<Avatar> getAllAvatars(Integer pageNumber, Integer pageSize) {
        logger.info("Was invoked method for get all avatars with pagination");
        PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize);
        return avatarRepository.findAll(pageRequest).getContent();
    }

    @Override
    public Collection<String> getAllNamesStartingWithA() {
        logger.info("Was invoked method for get all names starting with A");

        return studentRepository.findAll().parallelStream()
                .map(Student::getName)
                .map(String::toUpperCase)
                .filter(name -> name.startsWith("A") || name.startsWith("А"))
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Double findAverageAgeOfAllStudents() {
        logger.info("Was invoked method for calculation of the average age of all students");

        return studentRepository.findAll().parallelStream()
                .mapToInt(Student::getAge).average().orElse(0.0);
    }

    @Override
    public Integer getFastSum() {
        logger.info("Was invoked method for getting sum fast");

        return IntStream.rangeClosed(1, 1_000_000)
                .parallel()
                .sum();
    }

    private String getExtension(String fileName) {
        logger.debug("Parsing extension for file name: {}", fileName);
        if (fileName == null || !fileName.contains(".")) {
            return "png";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
