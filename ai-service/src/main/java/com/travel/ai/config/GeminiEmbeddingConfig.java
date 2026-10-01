package com.travel.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Configuration
@Slf4j
public class GeminiEmbeddingConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://generativelanguage.googleapis.com/v1beta/openai/}")
    private String baseUrl;

    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        return new CustomGeminiEmbeddingModel(apiKey, baseUrl);
    }

    public static class CustomGeminiEmbeddingModel implements EmbeddingModel {

        private final String apiKey;
        private final String endpointUrl;
        private final RestTemplate restTemplate;

        public CustomGeminiEmbeddingModel(String apiKey, String baseUrl) {
            this.apiKey = apiKey;
            String cleanBase = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
            this.endpointUrl = cleanBase + "embeddings";
            this.restTemplate = new RestTemplate();
        }

        @Override
        public float[] embed(String text) {
            List<float[]> results = embedTexts(Collections.singletonList(text));
            return (results != null && !results.isEmpty()) ? results.get(0) : new float[768];
        }

        @Override
        public float[] embed(Document document) {
            return embed(document.getContent());
        }

        @Override
        public List<float[]> embed(List<String> texts) {
            return embedTexts(texts);
        }

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            List<String> instructions = request.getInstructions();
            List<float[]> vectors = embedTexts(instructions);
            List<Embedding> embeddings = new ArrayList<>();
            for (int i = 0; i < vectors.size(); i++) {
                embeddings.add(new Embedding(vectors.get(i), i));
            }
            return new EmbeddingResponse(embeddings, new EmbeddingResponseMetadata("gemini-embedding-001", new EmptyUsage()));
        }

        @SuppressWarnings("unchecked")
        private List<float[]> embedTexts(List<String> texts) {
            List<float[]> resultList = new ArrayList<>();
            if (texts == null || texts.isEmpty()) {
                return resultList;
            }

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                for (String text : texts) {
                    Map<String, Object> body = new HashMap<>();
                    body.put("model", "gemini-embedding-001");
                    body.put("input", text);

                    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                    Map<String, Object> response = restTemplate.postForObject(endpointUrl, entity, Map.class);

                    if (response != null && response.containsKey("data")) {
                        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                        if (data != null && !data.isEmpty() && data.get(0).containsKey("embedding")) {
                            List<Number> rawVec = (List<Number>) data.get(0).get("embedding");

                            int targetDim = 768;
                            float[] vec768 = new float[targetDim];
                            if (rawVec != null) {
                                for (int i = 0; i < Math.min(rawVec.size(), targetDim); i++) {
                                    vec768[i] = rawVec.get(i).floatValue();
                                }
                            }
                            resultList.add(vec768);
                            continue;
                        }
                    }
                    resultList.add(new float[768]);
                }
            } catch (Exception e) {
                log.error("Lỗi khi tạo Embedding bằng CustomGeminiEmbeddingModel: {}", e.getMessage());
                for (int i = 0; i < texts.size(); i++) {
                    resultList.add(new float[768]);
                }
            }
            return resultList;
        }
    }
}
