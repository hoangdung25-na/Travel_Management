import { apiClient, unwrap } from "../api-client";
import {
  ApiResponse,
  BookingDetailVm,
  BookingVm,
  CancelBookingRequest,
  CreateBookingRequest,
  PageResponse,
} from "../types";

export const bookingService = {
  create: (payload: CreateBookingRequest, userId?: string): Promise<BookingDetailVm> =>
    unwrap<BookingDetailVm>(
      apiClient.post<ApiResponse<BookingDetailVm>>("/api/v1/bookings", payload)
    ),

  cancel: (id: string, payload: CancelBookingRequest): Promise<BookingDetailVm> =>
    unwrap<BookingDetailVm>(
      apiClient.post<ApiResponse<BookingDetailVm>>(`/api/v1/bookings/${id}/cancel`, payload)
    ),

  getById: (id: string): Promise<BookingDetailVm> =>
    unwrap<BookingDetailVm>(
      apiClient.get<ApiResponse<BookingDetailVm>>(`/api/v1/bookings/${id}`)
    ),

  getMyBookings: (userId?: string, page = 0, size = 10): Promise<PageResponse<BookingDetailVm>> =>
    unwrap<PageResponse<BookingDetailVm>>(
      apiClient.get<ApiResponse<PageResponse<BookingDetailVm>>>("/api/v1/bookings/my-bookings", {
        params: { page, size },
      })
    ),

  getAllBookings: (): Promise<BookingDetailVm[]> =>
    unwrap<BookingDetailVm[]>(
      apiClient.get<ApiResponse<BookingDetailVm[]>>("/api/v1/bookings/admin/all")
    ),
};
