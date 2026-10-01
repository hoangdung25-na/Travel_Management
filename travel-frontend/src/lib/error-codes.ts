// Ánh xạ ErrorCode (từ GlobalExceptionHandler backend) sang message tiếng Việt
// thân thiện để hiển thị cho người dùng cuối. Field `message` trả từ API vẫn
// được ưu tiên hiển thị trước — bảng này chỉ dùng làm fallback / để phân loại
// hành vi xử lý (ví dụ: AUTH-1002 -> tự refresh token).
export const ERROR_MESSAGES: Record<string, string> = {
  "ERR-0400": "Yêu cầu không hợp lệ.",
  "ERR-0401": "Bạn cần đăng nhập để tiếp tục.",
  "ERR-0403": "Bạn không có quyền thực hiện thao tác này.",
  "ERR-0404": "Không tìm thấy dữ liệu yêu cầu.",
  "ERR-0422-V": "Thông tin nhập chưa hợp lệ, vui lòng kiểm tra lại.",
  "ERR-0500": "Hệ thống đang gặp sự cố, vui lòng thử lại sau.",
  "AUTH-1001": "Phiên đăng nhập không hợp lệ.",
  "AUTH-1002": "Phiên đăng nhập đã hết hạn.",
  "AUTH-1003": "Email này đã được đăng ký.",
  "AUTH-1006": "Email hoặc mật khẩu không đúng.",
  "TOUR-2001": "Không tìm thấy tour du lịch này.",
  "TOUR-2002": "Mã tour đã tồn tại.",
  "TOUR-2005": "Số chỗ còn lại không đủ cho yêu cầu của bạn.",
  "BKG-3001": "Không tìm thấy đơn đặt tour.",
  "BKG-3002": "Đơn đặt tour đã hết hạn giữ chỗ.",
  "BKG-3003": "Đơn đặt tour đã bị hủy.",
  "PAY-4001": "Thanh toán thất bại, vui lòng thử lại.",
  "PAY-4002": "Không tìm thấy giao dịch thanh toán.",
  "AI-5001": "Trợ lý AI hiện chưa sẵn sàng, vui lòng thử lại sau.",
};

export function resolveErrorMessage(code?: string | null, fallback?: string | null): string {
  if (code && ERROR_MESSAGES[code]) return ERROR_MESSAGES[code];
  if (fallback) return fallback;
  return "Đã có lỗi xảy ra, vui lòng thử lại.";
}

export const TOKEN_EXPIRED_CODE = "AUTH-1002";
