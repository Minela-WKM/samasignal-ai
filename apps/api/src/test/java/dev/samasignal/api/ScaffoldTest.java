package dev.samasignal.api;

import dev.samasignal.api.contract.ExtractionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ScaffoldTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired Validator validator;

    @Test
    void applicationStartsWithDatabaseAndHealthcheck() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void swaggerDescribesTheScaffoldWithoutInventedRoutes() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.info.version").value("0.1.0"));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/api/reports")).andExpect(status().isNotFound());
    }

    @Test
    void extractionPreservesCamelCaseEnumsAndRejectsInvalidConfidence() throws Exception {
        String json = """
                {"categorie":"dechet","resume":"Dépôt sauvage","localisationDeclaree":null,
                "niveauUrgence":"moyenne","elementsVisuelsDetectes":["sacs"],
                "champsManquants":["localisationDeclaree"],"scoreConfiance":0.8}
                """;
        var extraction = mapper.readValue(json, ExtractionResult.class);
        assertThat(validator.validate(extraction)).isEmpty();
        assertThat(mapper.readTree(mapper.writeValueAsString(extraction)))
                .isEqualTo(mapper.readTree(json));
        var invalid = mapper.readValue(json.replace("0.8", "1.2"), ExtractionResult.class);
        assertThat(validator.validate(invalid)).hasSize(1);
    }
}
