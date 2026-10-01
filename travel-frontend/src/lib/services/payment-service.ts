import { apiClient, unwrap } from "../api-client";
import {
  ApiResponse,
  CreatePaymentUrlRequest,
  PaymentUrlVm,
  PaymentVm,
  RefundPaymentRequest,
} from "../types";

export const paymentService = {
  createUrl: (payload: CreatePaymentUrlRequest): Promise<PaymentUrlVm> =>
    unwrap<PaymentUrlVm>(
      apiClient.post<ApiResponse<PaymentUrlVm>>("/api/v1/payments/create-url", payload)
    ),

  getByBookingId: (bookingId: string): Promise<PaymentVm | null> =>
    unwrap<PaymentVm>(
      apiClient.get<ApiResponse<PaymentVm>>(`/api/v1/payments/booking/${bookingId}`)
    ),

  refund: (id: string, payload: RefundPaymentRequest): Promise<PaymentVm> =>
    unwrap<PaymentVm>(
      apiClient.post<ApiResponse<PaymentVm>>(`/api/v1/payments/${id}/refund`, payload)
    ),
};
