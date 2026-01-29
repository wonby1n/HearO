package com.ssafy.hearo.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssafy.hearo.domain.ai.dto.AskResponse;
import com.ssafy.hearo.domain.ai.dto.AskResponse.RetrievedDoc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class RagServiceImpl implements RagService {

    private final WebClient webClient = WebClient.builder().build();

    // ===== Chroma (Docker, v2) =====

    @Value("${chromadb.url}")
    private String chromaBase;  //URL

    private static final String CHROMA_TENANT = "default_tenant";
    private static final String CHROMA_DB = "default_database";

    private static final String CHROMA_COLLECTION_ID = "e0611d78-5855-41e0-8e97-2e6dc2551080";
    ;

    private static final int TOP_K = 5;

    // ===== GMS (OpenAI 호환) =====
    @Value("${gms.url}")
    private String gmsBase;

    // 답변 생성 모델 (LLM)
    private static final String CHAT_MODEL = "gpt-4.1-mini";

    // 임베딩 모델 (업서트/검색 동일해야 함)
    private static final String EMBED_MODEL = "text-embedding-3-small";
    @Value("${gms.api-key}")
    private String gmsKey;

    //정확도 수치 현재는 0, 늘릴 수록 관련없는 것들은 지워짐.
    private static final double MIN_SCORE = 0.0;


    @Override
    public AskResponse ask(String question) {

        // 1) 질문 임베딩(GMS) -> Chroma query_embeddings
        List<RetrievedDoc> contexts = queryChromaByEmbedding(question);

        // 2) RAG input 생성
        String ragInput = buildRagInput(question, contexts);

        // 3) GMS Responses 호출(답변 생성)
        String answer = callGmsResponses(ragInput);

        AskResponse res = new AskResponse();
        res.setAnswer(answer);
        res.setContexts(contexts);
        return res;
    }

    private String clean(String s) {
        if (s == null) return "";
        String x = s.replace("\u0000", " ");
        x = x.replaceAll("[•·]+", " ");          // 불릿 제거
        x = x.replaceAll("\\s{2,}", " ").trim(); // 공백 정리
        return x;
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return (s.length() <= max) ? s : s.substring(0, max) + "...";
    }

    /* -----------------------------
       1) GMS embeddings
       ----------------------------- */
    private List<Double> embedWithGms(String text) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", EMBED_MODEL);
        body.put("input", text);

        JsonNode resp = webClient.post()
                .uri(gmsBase + "/embeddings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + gmsKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        JsonNode emb = (resp == null) ? null : resp.path("data").path(0).path("embedding");
        if (emb == null || !emb.isArray() || emb.size() == 0) {
            return List.of();
        }

        List<Double> vec = new ArrayList<>(emb.size());
        for (JsonNode v : emb) vec.add(v.asDouble());
        return vec;
    }

    /* -----------------------------
       2) Chroma query_embeddings (UUID 기반)
       ----------------------------- */
    private List<RetrievedDoc> queryChromaByEmbedding(String question) {
        String url = String.format(
                "%s/api/v2/tenants/%s/databases/%s/collections/%s/query",
                chromaBase, CHROMA_TENANT, CHROMA_DB, CHROMA_COLLECTION_ID
        );

        List<Double> qvec = embedWithGms(question);
        if (qvec.isEmpty()) return List.of();

        Map<String, Object> body = new HashMap<>();
        body.put("query_embeddings", List.of(qvec));
        body.put("n_results", TOP_K);

        body.put("include", List.of("documents", "metadatas", "distances"));

        JsonNode resp = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return toRetrievedDocs(resp);
    }

    private List<RetrievedDoc> toRetrievedDocs(JsonNode resp) {
        List<RetrievedDoc> out = new ArrayList<>();
        if (resp == null) return out;

        JsonNode documents = resp.get("documents");
        JsonNode metadatas = resp.get("metadatas");
        JsonNode distances = resp.get("distances");
        JsonNode ids = resp.get("ids"); // 보통 응답에 포함됨

        if (documents == null || !documents.isArray() || documents.size() == 0) return out;

        // query 1개 기준: [ [doc1, doc2, ...] ]
        JsonNode docs0  = documents.get(0);
        JsonNode metas0 = (metadatas != null && metadatas.isArray() && metadatas.size() > 0) ? metadatas.get(0) : null;
        JsonNode dist0  = (distances != null && distances.isArray() && distances.size() > 0) ? distances.get(0) : null;
        JsonNode ids0   = (ids != null && ids.isArray() && ids.size() > 0) ? ids.get(0) : null;

        for (int i = 0; i < docs0.size(); i++) {
            String text = docs0.get(i).asText("");

            String id = (ids0 != null && i < ids0.size()) ? ids0.get(i).asText("") : "doc-" + i;

            double distance = (dist0 != null && i < dist0.size()) ? dist0.get(i).asDouble() : -1.0;
            // cosine distance면 보통 0에 가까울수록 유사 (환경에 따라 1-score로 쓰기)
            double score = (distance >= 0) ? (1.0 - distance) : 0.0;

            String source = "";
            if (metas0 != null && i < metas0.size()) {
                JsonNode m = metas0.get(i);
                if (m != null && m.isObject() && m.get("source") != null) {
                    source = m.get("source").asText("");
                }
            }
            if (score < MIN_SCORE) continue;
            out.add(new RetrievedDoc(id, text, score, source));
        }

        return out;
    }

    /* -----------------------------
       3) RAG prompt 구성
       ----------------------------- */
    private String buildRagInput(String question, List<RetrievedDoc> contexts) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
                너는 고객지원 문서를 기반으로 답변하는 RAG 어시스턴트다.
                아래 [CONTEXT]에 포함된 정보만 사용해서 답변하라.
                컨텍스트에 없는 사실/수치/절차/코드/정책은 절대 추측하지 말고 반드시
                "컨텍스트에 정보가 없습니다." 라고 말하라.
                
                [핵심 규칙]
                1) 컨텍스트에 있는 내용만 인용/재구성해서 답하라. 상식/추측/외부지식 금지.
                2) 질문이 모호하면, 컨텍스트에서 확인 가능한 범위만 말하고 "추가로 필요한 정보"를 1~3개 질문하라.
                3) 답변을 만들기 전에, 컨텍스트가 질문을 해결하기에 충분한지 스스로 점검하라.
                   - 충분하지 않으면 답변 대신 "컨텍스트에 정보가 없습니다." 를 출력하라.
                4) 숫자, 모델명, 오류코드, 전화번호, 운영시간 등은 컨텍스트에 나온 그대로만 사용하라.
                5) 컨텍스트에 없는 항목을 나열하거나, "다음과 같습니다" 식으로 개수를 꾸며내지 말라.
                6) 답변은 한문단으로 하여라.
                
                [출력 형식 - 반드시 지켜라]
                아래 JSON 형식으로만 출력하라(마크다운 금지).
                
                {
                  "answer": "최종 답변(한국어). 짧고 명확하게.",
                  "evidence": [
                    {
                      "source": "<파일명 또는 source>",
                      "quote": "<컨텍스트에서 그대로 가져온 근거 문장(최대 25단어)>"
                    }
                  ],
                  "need_more_info": [
                    "컨텍스트가 부족할 때만: 추가로 필요한 질문 1~3개"
                  ]
                }
                
                [근거 작성 규칙]
                - evidence는 1~3개만 작성하라.
                - quote는 컨텍스트에서 발췌한 짧은 문장만. (최대 25단어)
                - 컨텍스트에 근거가 없으면 evidence는 빈 배열([])로 두어라.
                
                [CONTEXT]
                (아래는 검색된 문서 발췌다. 각 항목은 source와 text로 구성된다.)
                {{CONTEXT_BLOCK}}
                
                [QUESTION]
                {{QUESTION}}
                
                """);

        if (contexts == null || contexts.isEmpty()) {
            sb.append("- (컨텍스트 없음)\n");
        } else {
            for (RetrievedDoc doc : contexts) {
                String text = truncate(clean(doc.getText()), 700);
                sb.append("- source: ").append(doc.getSource()).append("\n");
                sb.append("  text: ").append(text).append("\n\n");
            }
        }

        sb.append("\n[QUESTION]\n").append(question);
        return sb.toString();
    }

    /* -----------------------------
       4) GMS Responses (답변 생성)
       ----------------------------- */
    private String callGmsResponses(String input) {

        Map<String, Object> body = new HashMap<>();
        body.put("model", CHAT_MODEL);
        body.put("input", input);

        JsonNode resp = webClient.post()
                .uri(gmsBase + "/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + gmsKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return extractOutputText(resp);
    }

    private String extractOutputText(JsonNode resp) {
        if (resp == null) return "응답 생성 실패";

        // 1) output_text 우선
        JsonNode outputText = resp.get("output_text");
        if (outputText != null && outputText.isTextual()) return outputText.asText();

        // 2) fallback: output[].content[].type=="output_text"
        JsonNode output = resp.get("output");
        if (output != null && output.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode item : output) {
                JsonNode content = item.get("content");
                if (content != null && content.isArray()) {
                    for (JsonNode c : content) {
                        if ("output_text".equals(c.path("type").asText())) {
                            sb.append(c.path("text").asText(""));
                        }
                    }
                }
            }
            String s = sb.toString().trim();
            if (!s.isEmpty()) return s;
        }

        return "응답 파싱 실패";
    }
}
