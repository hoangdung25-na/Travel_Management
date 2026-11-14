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

  chat: (promptText: string, conversationId?: string): Promise<{ conversationId: string; reply: string }> =>
    unwrap<{ conversationId: string; reply: string }>(
      apiClient.post<ApiResponse<{ conversationId: string; reply: string }>>("/api/v1/ai/chat", {
        promptText,
        conversationId,
      })
    ),

  syncFromTourService: (): Promise<{ syncedCount: number; message: string }> =>
    unwrap<{ syncedCount: number; message: string }>(
      apiClient.post<ApiResponse<{ syncedCount: number; message: string }>>(
        "/api/v1/ai/sync-from-tour-service"
      )
    ),

  clearConversation: (conversationId: string): Promise<string> =>
    unwrap<string>(
      apiClient.delete<ApiResponse<string>>(`/api/v1/ai/chat/${conversationId}`)
    ),
};
