import { apiClient, unwrap } from "../api-client";
import { GuideAssignedTourVm, ApiResponse } from "../types";

export const guideService = {
  getAssignedTours: (guideId?: string): Promise<GuideAssignedTourVm[]> =>
    unwrap<GuideAssignedTourVm[]>(
      apiClient.get<ApiResponse<GuideAssignedTourVm[]>>(`/api/v1/tours/guide/assigned`, {
        params: { guideId },
      })
    ),

  toggleCheckIn: (scheduleId: string, passengerId: string): Promise<boolean> =>
    unwrap<boolean>(
      apiClient.post<ApiResponse<boolean>>(`/api/v1/bookings/check-in`, {
        scheduleId,
        passengerId,
      })
    ),
};
