package ru.hogwarts.school;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.hogwarts.school.controller.StudentController;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testFindStudentsWithoutAge() throws Exception {
        Mockito.when(studentService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testCreateStudent() throws Exception {
        Student student = new Student();
        student.setId(1L);
        student.setName("Harry");
        student.setAge(20);

        Mockito.when(studentService.addStudent(any(Student.class))).thenReturn(student);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/student")
                        .content(objectMapper.writeValueAsString(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Harry"))
                .andExpect(jsonPath("$.age").value(20));
    }

    @Test
    public void testGetStudentInfoFound() throws Exception {
        Student student = new Student();
        student.setId(1L);
        student.setName("Harry");

        Mockito.when(studentService.findStudent(1L)).thenReturn(student);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Harry"));
    }

    @Test
    public void testGetStudentInfoNotFound() throws Exception {
        Mockito.when(studentService.findStudent(1L)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteStudent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/student/1"))
                .andExpect(status().isOk());

        Mockito.verify(studentService, Mockito.times(1)).deleteStudent(1L);
    }

    @Test
    public void testUploadAvatarTooBig() throws Exception {
        byte[] largeContent = new byte[1024 * 400];
        MockMultipartFile file = new MockMultipartFile("avatar", "test.jpg", MediaType.IMAGE_JPEG_VALUE, largeContent);

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/student/1/avatar")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File is too big"));
    }
}
