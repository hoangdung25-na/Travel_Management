import { apiClient, unwrap } from "../api-client";
import { AiQueryRequest, ApiResponse, TripRecommendationVm } from "../types";

export const aiService = {
  getRecommendations: (payload: AiQueryRequest): Promise<TripRecommendationVm> => {
    const text = payload.queryText || payload.promptText || "";
    const requestBody = {
      queryText: text,
      promptText: text,
      destination: payload.destination,
      maxBudget: payload.maxBudget,
      topK: payload.topK ?? 5,
    };
    return unwrap<TripRecommendationVm>(
      apiClient.post<ApiResponse<TripRecommendationVm>>(
        "/api/v1/ai/recommendations",
        requestBody
      )
    );
  },

  chat: (promptText: string): Promise<string> =>
    unwrap<string>(
      apiClient.post<ApiResponse<string>>("/api/v1/ai/chat", { promptText })
    ),
};
