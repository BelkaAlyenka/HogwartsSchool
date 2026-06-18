package ru.hogwarts.school.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class FacultyControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private StudentRepository studentRepository;

    private String baseUrl;
    private Faculty savedFaculty;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/faculty";

        studentRepository.deleteAll();
        facultyRepository.deleteAll();

        Faculty faculty = new Faculty();
        faculty.setName("Gryffindor");
        faculty.setColor("Red");
        savedFaculty = facultyRepository.save(faculty);
    }

    @AfterEach
    void tearDown() {
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
    }

    @Test
    void shouldFindAllFaculties() {
        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(baseUrl, Faculty[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getName()).isEqualTo("Gryffindor");
    }

    @Test
    void shouldFindFacultiesBySearchQuery() {
        ResponseEntity<Faculty[]> responseByColor = restTemplate.getForEntity(baseUrl + "?search=Red", Faculty[].class);
        assertThat(responseByColor.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseByColor.getBody()).hasSize(1);

        ResponseEntity<Faculty[]> responseByName = restTemplate.getForEntity(baseUrl + "?search=Gryffindor", Faculty[].class);
        assertThat(responseByName.getBody()).hasSize(1);

        ResponseEntity<Faculty[]> responseEmpty = restTemplate.getForEntity(baseUrl + "?search=Hufflepuff", Faculty[].class);
        assertThat(responseEmpty.getBody()).isEmpty();
    }

    @Test
    void shouldCreateFaculty() {
        Faculty newFaculty = new Faculty();
        newFaculty.setName("Slytherin");
        newFaculty.setColor("Green");

        ResponseEntity<Faculty> response = restTemplate.postForEntity(baseUrl, newFaculty, Faculty.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Slytherin");
    }

    @Test
    void shouldGetFacultyInfo() {
        ResponseEntity<Faculty> response = restTemplate.getForEntity(baseUrl + "/" + savedFaculty.getId(), Faculty.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Gryffindor");
    }

    @Test
    void shouldReturnNotFoundWhenFacultyDoesNotExist() {
        long nonExistingId = savedFaculty.getId() + 999;
        ResponseEntity<Faculty> response = restTemplate.getForEntity(baseUrl + "/" + nonExistingId, Faculty.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldEditFaculty() {
        savedFaculty.setColor("Gold");

        HttpEntity<Faculty> entity = new HttpEntity<>(savedFaculty);
        ResponseEntity<Faculty> response = restTemplate.exchange(baseUrl, HttpMethod.PUT, entity, Faculty.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getColor()).isEqualTo("Gold");
    }

    @Test
    void shouldDeleteFaculty() {
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + savedFaculty.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(facultyRepository.findById(savedFaculty.getId())).isEmpty();
    }

    @Test
    void shouldGetStudentsByFacultyId() {
        Student student = new Student();
        student.setName("Harry Potter");
        student.setAge(11);
        student.setFaculty(savedFaculty);
        studentRepository.save(student);

        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                baseUrl + "/" + savedFaculty.getId() + "/students",
                Student[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getName()).isEqualTo("Harry Potter");
    }
}
