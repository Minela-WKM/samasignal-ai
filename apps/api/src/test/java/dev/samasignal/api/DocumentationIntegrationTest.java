package dev.samasignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.samasignal.api.dto.ia.ExtractionIaDto;

import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class DocumentationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired Validator validator;
    @Autowired dev.samasignal.api.repository.JournalExtractionRepository journaux;

    @Test
    void applicationDemarreAvecBaseEtSante() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void swaggerDecritLesRoutesFrancaises() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.version").value("0.1.0"))
                .andExpect(jsonPath("$.paths['/api/v2/signalements/analyser'].post").exists())
                .andExpect(
                        jsonPath("$.paths['/api/v2/signalements/{id}/confirmer'].post").exists());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/api/reports")).andExpect(status().isNotFound());
    }

    @Test
    void extractionConserveLeContratEtRefuseUnScoreInvalide() throws Exception {
        String json =
                """
                {"categorie":"dechet","resume":"Dépôt sauvage","localisationDeclaree":null,
                "niveauUrgence":"moyenne","elementsVisuelsDetectes":["sacs"],
                "champsManquants":["localisationDeclaree"],"scoreConfiance":0.8}
                """;
        var extraction = mapper.readValue(json, ExtractionIaDto.class);
        assertThat(validator.validate(extraction)).isEmpty();
        assertThat(mapper.readTree(mapper.writeValueAsString(extraction)))
                .isEqualTo(mapper.readTree(json));
        var invalid = mapper.readValue(json.replace("0.8", "1.2"), ExtractionIaDto.class);
        assertThat(validator.validate(invalid)).hasSize(1);
    }

    @Test
    void analyseDesactiveeJournaliseeSansFausseReponse() throws Exception {
        long avant = journaux.count();
        mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .multipart("/api/v2/signalements/analyser")
                                .param("texteBrut", "Fuite près du marché"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("IA_INDISPONIBLE"));
        assertThat(journaux.count()).isEqualTo(avant + 1);
    }
}
