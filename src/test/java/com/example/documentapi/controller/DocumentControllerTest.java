package com.example.documentapi.controller;

import com.example.documentapi.model.Document;
import com.example.documentapi.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private DocumentService service;

    @InjectMocks
    private DocumentController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void upload_returnsAcceptedAndUuid() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "pdf-data".getBytes()
        );

        Document saved = new Document();
        UUID id = UUID.randomUUID();
        saved.setId(id);

        when(service.upload(any())).thenReturn(saved);

        mockMvc.perform(multipart("/documents").file(file).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isAccepted())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void get_whenFound_returnsDocument() throws Exception {
        UUID id = UUID.randomUUID();
        Document doc = new Document();
        doc.setId(id);
        doc.setFilename("test.pdf");

        when(service.getById(id)).thenReturn(Optional.of(doc));

        mockMvc.perform(get("/documents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(id.toString())))
                .andExpect(content().string(containsString("test.pdf")));
    }

    @Test
    void get_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/documents/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(id.toString())));
    }
}
