package dev.samasignal.api;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.samasignal.api.dto.ia.ExtractionIaDto;
import dev.samasignal.api.enumeration.Categorie;
import dev.samasignal.api.enumeration.NiveauUrgence;
import dev.samasignal.api.repository.*;
import dev.samasignal.api.service.ExtractionIaService;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SignalementIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired SignalementRepository repository;
    @MockitoBean ExtractionIaService extraction;

    @BeforeEach
    void preparer() {
        repository.deleteAll();
        when(extraction.extraire(anyString(), isNull(), isNull()))
                .thenReturn(
                        new ExtractionIaDto(
                                Categorie.dechet,
                                "Dépôt sauvage",
                                null,
                                NiveauUrgence.haute,
                                List.of(),
                                List.of(),
                                new BigDecimal("0.85")));
    }

    private String analyser() throws Exception {
        var reponse =
                mvc.perform(
                                multipart("/api/v2/signalements/analyser")
                                        .param("texteBrut", "Des déchets près du marché"))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.statut").value("EN_ATTENTE"))
                        .andExpect(jsonPath("$.champsManquants.length()").value(3))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return mapper.readTree(reponse).get("id").asText();
    }

    private String confirmation() {
        return """
{"categorie":"dechet","resume":"Déchets au marché","localisationDeclaree":"Grand Yoff, Dakar",
 "niveauUrgence":"haute","latitude":14.741,"longitude":-17.455}
""";
    }

    private void confirmer(String id) throws Exception {
        mvc.perform(
                        post("/api/v2/signalements/" + id + "/confirmer")
                                .contentType("application/json")
                                .content(confirmation()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("CONFIRME"));
    }

    @Test
    void parcoursCompletEtFiltres() throws Exception {
        String id = analyser();
        mvc.perform(get("/api/v2/signalements")).andExpect(content().json("[]"));
        confirmer(id);
        mvc.perform(
                        get("/api/v2/signalements")
                                .param("urgence", "haute")
                                .param("categorie", "dechet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].champsManquants").isEmpty());
        mvc.perform(get("/api/v2/signalements").param("urgence", "basse"))
                .andExpect(content().json("[]"));
        mvc.perform(post("/api/v2/signalements/" + id + "/traiter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TRAITE"));
    }

    @Test
    void confirmationIdempotenteEtCorrectionInterdite() throws Exception {
        String id = analyser();
        confirmer(id);
        confirmer(id);
        mvc.perform(
                        post("/api/v2/signalements/" + id + "/confirmer")
                                .contentType("application/json")
                                .content(confirmation().replace("Grand Yoff", "Plateau")))
                .andExpect(status().isConflict());
        assertThat(repository.count()).isEqualTo(1);
        mvc.perform(delete("/api/v2/signalements/" + id)).andExpect(status().isConflict());
        mvc.perform(post("/api/v2/signalements/" + id + "/traiter"))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/v2/signalements/" + id)).andExpect(status().isConflict());
        mvc.perform(post("/api/v2/signalements/" + id + "/confirmer")
                        .contentType("application/json").content(confirmation()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TRAITE"));
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void suppressionBrouillonSansPhotoIdempotente() throws Exception {
        String id = analyser();
        mvc.perform(delete("/api/v2/signalements/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/v2/signalements/" + id)).andExpect(status().isNoContent());
        assertThat(repository.count()).isZero();
    }

    @Test
    void validationAvantAppelIa() throws Exception {
        mvc.perform(multipart("/api/v2/signalements/analyser").param("texteBrut", "   "))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        multipart("/api/v2/signalements/analyser")
                                .param("texteBrut", "Fuite")
                                .param("latitude", "14"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(extraction);
    }

    @Test
    void refusFaussePhoto() throws Exception {
        var photo =
                new MockMultipartFile(
                        "photo", "image.png", "image/png", "pas une image".getBytes());
        mvc.perform(
                        multipart("/api/v2/signalements/analyser")
                                .file(photo)
                                .param("texteBrut", "Fuite"))
                .andExpect(status().isUnsupportedMediaType());
        verifyNoInteractions(extraction);
    }

    @Test
    void confirmationIncompleteEtIdentifiantInconnu() throws Exception {
        String id = analyser();
        mvc.perform(
                        post("/api/v2/signalements/" + id + "/confirmer")
                                .contentType("application/json")
                                .content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/v2/signalements/" + UUID.randomUUID() + "/confirmer")
                                .contentType("application/json")
                                .content(confirmation()))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/v2/signalements/" + id + "/traiter"))
                .andExpect(status().isConflict());
    }

    @Test
    void echecIaSansCreation() throws Exception {
        when(extraction.extraire(anyString(), isNull(), isNull()))
                .thenThrow(
                        new dev.samasignal.api.exception.ExceptionMetier(
                                org.springframework.http.HttpStatus.GATEWAY_TIMEOUT,
                                "IA_DELAI_DEPASSE",
                                "Délai dépassé."));
        mvc.perform(multipart("/api/v2/signalements/analyser").param("texteBrut", "Fuite"))
                .andExpect(status().isGatewayTimeout());
        assertThat(repository.count()).isZero();
    }

    @Test
    void photoPersisteeEtConsultable() throws Exception {
        byte[] image =
                Base64.getDecoder()
                        .decode(
                                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+afoQAAAAASUVORK5CYII=");
        when(extraction.extraire(anyString(), any(byte[].class), eq("image/png")))
                .thenReturn(
                        new ExtractionIaDto(
                                Categorie.eau_usee,
                                "Fuite",
                                "Dakar",
                                NiveauUrgence.moyenne,
                                List.of("eau"),
                                List.of(),
                                new BigDecimal("0.9")));
        var reponse =
                mvc.perform(
                                multipart("/api/v2/signalements/analyser")
                                        .file(
                                                new MockMultipartFile(
                                                        "photo", "photo.png", "image/png", image))
                                        .param("texteBrut", "Fuite à Dakar")
                                        .param("latitude", "14.7")
                                        .param("longitude", "-17.4"))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        String url = mapper.readTree(reponse).get("photoUrl").asText();
        mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(image));
        String id = mapper.readTree(reponse).get("id").asText();
        mvc.perform(delete("/api/v2/signalements/" + id)).andExpect(status().isNoContent());
        mvc.perform(get(url)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/v2/signalements/" + id)).andExpect(status().isNoContent());
        assertThat(repository.count()).isZero();
    }

    @Test
    void champsSystemeEtCoordonneesInvalidesRefuses() throws Exception {
        String id = analyser();
        mvc.perform(
                        post("/api/v2/signalements/" + id + "/confirmer")
                                .contentType("application/json")
                                .content(confirmation().replace("14.741", "91")))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/v2/signalements/" + id + "/confirmer")
                                .contentType("application/json")
                                .content(confirmation().replace("{", "{\"statut\":\"TRAITE\",")))
                .andExpect(status().isBadRequest());
    }
}
