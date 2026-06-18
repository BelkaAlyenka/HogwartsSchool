package ru.hogwarts.school.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class StudentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @MockitoBean
    private AvatarRepository avatarRepository;

    private String baseUrl;
    private Student savedStudent;
    private Faculty savedFaculty;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/student";

        savedFaculty = new Faculty();
        savedFaculty.setName("Ravenclaw");
        savedFaculty.setColor("Blue and Bronze");
        savedFaculty = facultyRepository.save(savedFaculty);

        Student student = new Student();
        student.setName("Luna Lovegood");
        student.setAge(14);
        student.setFaculty(savedFaculty);
        savedStudent = studentRepository.save(student);
    }

    @AfterEach
    void tearDown() {
        avatarRepository.deleteAll();
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
    }

    @Test
    void shouldFindAllStudents() {
        ResponseEntity<Student[]> response = restTemplate.getForEntity(baseUrl, Student[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getName()).isEqualTo("Luna Lovegood");
    }

    @Test
    void shouldFindStudentsByAge() {
        ResponseEntity<Student[]> response = restTemplate.getForEntity(baseUrl + "?age=14", Student[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getName()).isEqualTo("Luna Lovegood");

        ResponseEntity<Student[]> responseEmpty = restTemplate.getForEntity(baseUrl + "?age=25", Student[].class);
        assertThat(responseEmpty.getBody()).isEmpty();
    }

    @Test
    void shouldCreateStudent() {
        Student newStudent = new Student();
        newStudent.setName("Cho Chang");
        newStudent.setAge(16);

        ResponseEntity<Student> response = restTemplate.postForEntity(baseUrl, newStudent, Student.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Cho Chang");
    }

    @Test
    void shouldGetStudentInfo() {
        ResponseEntity<Student> response = restTemplate.getForEntity(baseUrl + "/" + savedStudent.getId(), Student.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Luna Lovegood");
    }

    @Test
    void shouldReturnNotFoundWhenStudentDoesNotExist() {
        ResponseEntity<Student> response = restTemplate.getForEntity(baseUrl + "/999", Student.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldEditStudent() {
        savedStudent.setName("Luna Scamander");

        HttpEntity<Student> entity = new HttpEntity<>(savedStudent);
        ResponseEntity<Student> response = restTemplate.exchange(baseUrl, HttpMethod.PUT, entity, Student.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Luna Scamander");
    }

    @Test
    void shouldDeleteStudent() {
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + savedStudent.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(studentRepository.findById(savedStudent.getId())).isEmpty();
    }

    @Test
    void shouldFindByAgeBetween() {
        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                baseUrl + "/age-between?min=10&max=15",
                Student[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getName()).isEqualTo("Luna Lovegood");
    }

    @Test
    void shouldGetFacultyOfStudent() {
        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                baseUrl + "/" + savedStudent.getId() + "/faculty",
                Faculty.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Ravenclaw");
    }

    @Test
    void shouldUploadAvatar() {
        Avatar avatar = new Avatar();
        avatar.setStudent(savedStudent);
        avatar.setMediaType(MediaType.IMAGE_PNG_VALUE);
        avatar.setFileSize(4);
        avatar.setData(new byte[]{1, 2, 3, 4});
        avatar.setFilePath("C:/avatars/test.png");

        // Обучаем заглушку репозитория возвращать наш аватар
        org.mockito.Mockito.when(avatarRepository.findByStudentId(savedStudent.getId()))
                .thenReturn(java.util.Optional.of(avatar));

        java.util.Optional<Avatar> saved = avatarRepository.findByStudentId(savedStudent.getId());
        assertThat(saved).isPresent();
    }

    @Test
    void shouldDownloadAvatarPreview() {
        Avatar avatar = new Avatar();
        avatar.setStudent(savedStudent);
        avatar.setMediaType(MediaType.IMAGE_PNG_VALUE);
        avatar.setData(new byte[]{5, 6, 7, 8});
        avatar.setFileSize(4);
        avatar.setFilePath("C:/avatars/preview.png");

        // Обучаем заглушку репозитория возвращать наш аватар
        org.mockito.Mockito.when(avatarRepository.findByStudentId(savedStudent.getId()))
                .thenReturn(java.util.Optional.of(avatar));

        ResponseEntity<byte[]> response = restTemplate.getForEntity(
                baseUrl + "/" + savedStudent.getId() + "/avatar/preview",
                byte[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);
        assertThat(response.getBody()).containsExactly(5, 6, 7, 8);
    }

    @Test
    void shouldDownloadAvatarFromFileSystem() throws Exception {
        Path tempFile = Files.createTempFile("avatar_test", ".png");
        Files.write(tempFile, new byte[]{9, 10, 11});

        Avatar avatar = new Avatar();
        avatar.setStudent(savedStudent);
        avatar.setMediaType(MediaType.IMAGE_PNG_VALUE);
        avatar.setFilePath(tempFile.toAbsolutePath().toString());
        avatar.setFileSize(3);
        avatar.setData(new byte[]{9, 10, 11});

        // Обучаем заглушку репозитория возвращать наш аватар
        org.mockito.Mockito.when(avatarRepository.findByStudentId(savedStudent.getId()))
                .thenReturn(java.util.Optional.of(avatar));

        ResponseEntity<byte[]> response = restTemplate.getForEntity(
                baseUrl + "/" + savedStudent.getId() + "/avatar",
                byte[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(9, 10, 11);

        Files.deleteIfExists(tempFile);
    }
}


