import { apiClient, unwrap } from "../api-client";
import {
  ApiResponse,
  CreateDestinationRequest,
  CreateScheduleRequest,
  CreateTourRequest,
  DestinationVm,
  PageResponse,
  TourDetailVm,
  TourScheduleVm,
  TourSearchCriteria,
  TourVm,
  UpdateTourRequest,
} from "../types";

export const tourService = {
  search: (criteria: TourSearchCriteria): Promise<PageResponse<TourVm>> =>
    unwrap<PageResponse<TourVm>>(
      apiClient.get<ApiResponse<PageResponse<TourVm>>>("/api/v1/tours", {
        params: criteria,
      })
    ),

  getById: (id: string): Promise<TourDetailVm> =>
    unwrap<TourDetailVm>(
      apiClient.get<ApiResponse<TourDetailVm>>(`/api/v1/tours/${id}`)
    ),

  create: (payload: CreateTourRequest): Promise<TourDetailVm> =>
    unwrap<TourDetailVm>(
      apiClient.post<ApiResponse<TourDetailVm>>("/api/v1/tours", payload)
    ),

  update: (id: string, payload: UpdateTourRequest): Promise<TourDetailVm> =>
    unwrap<TourDetailVm>(
      apiClient.put<ApiResponse<TourDetailVm>>(`/api/v1/tours/${id}`, payload)
    ),

  addSchedule: (tourId: string, payload: CreateScheduleRequest): Promise<TourScheduleVm> =>
    unwrap<TourScheduleVm>(
      apiClient.post<ApiResponse<TourScheduleVm>>(
        `/api/v1/tours/${tourId}/schedules`,
        payload
      )
    ),

  listDestinations: (): Promise<DestinationVm[]> =>
    unwrap<DestinationVm[]>(
      apiClient.get<ApiResponse<DestinationVm[]>>("/api/v1/destinations")
    ),

  createDestination: (payload: CreateDestinationRequest): Promise<DestinationVm> =>
    unwrap<DestinationVm>(
      apiClient.post<ApiResponse<DestinationVm>>("/api/v1/destinations", payload)
    ),
};
