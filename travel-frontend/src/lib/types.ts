// ===== Các type này được sinh thủ công, khớp CHÍNH XÁC với OpenAPI spec
// của 5 service: auth(8081) / tour(8082) / booking(8083) / payment(8084) / ai(8085)
// Mọi field/enum đều lấy nguyên từ schema thật, không suy đoán thêm. =====

export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  data: T | null;
  errors: string[] | null;
  path: string;
  traceId: string;
  timestamp: string;
}

export interface PageResponse<T> {
  totalElements: number;
  totalPages: number;
  size: number;
  content: T[];
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

// ---------- Auth Service ----------
export type UserStatus = "PENDING_APPROVAL" | "ACTIVE" | "BLOCKED";
export type UserRole = "ROLE_TOURIST" | "ROLE_GUIDE" | "ROLE_ADMIN";
export type AccountType = "TOURIST" | "GUIDE";

export interface UserVm {
  userId: string;
  email: string;
  fullName: string;
  status: UserStatus;
  roles: UserRole[];
  phoneNumber?: string;
  createdAt?: string;
}

export interface UserProfileVm extends UserVm {
  phoneNumber?: string;
  dateOfBirth?: string;
  avatarUrl?: string;
  emergencyContact?: string;
}

export interface AuthTokenVm {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phoneNumber?: string;
  accountType: AccountType;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface UpdateProfileRequest {
  fullName?: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  emergencyContact?: string;
}

// ---------- Tour Service ----------
export type TourStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";

export interface DestinationVm {
  id: string;
  name: string;
  city: string;
  country: string;
  imageUrl?: string;
  tourCount?: number;
}

export interface ItineraryVm {
  id: string;
  dayNumber: number;
  title: string;
  content: string;
}

export interface TourScheduleVm {
  id: string;
  departureTime: string;
  arrivalTime: string;
  priceAdult: number;
  priceChild: number;
  totalSeats: number;
  availableSeats: number;
  assignedGuideId?: string;
  assignedGuideName?: string;
}

export interface TourVm {
  tourId: string;
  code: string;
  title: string;
  minPrice: number;
  availableSeats: number;
  status: TourStatus;
  createdAt: string;
  duration?: string;
  imageUrl?: string;
  rating?: number;
  reviewCount?: number;
  destinationCity?: string;
}

export interface TourDetailVm {
  tourId: string;
  code: string;
  title: string;
  description: string;
  status: TourStatus;
  createdAt: string;
  updatedAt: string;
  itineraries: ItineraryVm[];
  schedules: TourScheduleVm[];
  destinations: DestinationVm[];
  imageUrl?: string;
  galleryUrls?: string[];
  rating?: number;
  reviewCount?: number;
  duration?: string;
  includedServices?: string[];
  excludedServices?: string[];
}

export interface TourSearchCriteria {
  keyword?: string;
  destination?: string;
  minPrice?: number;
  maxPrice?: number;
  page?: number;
  size?: number;
}

export interface CreateItineraryRequest {
  dayNumber: number;
  title: string;
  content: string;
}

export interface CreateTourRequest {
  code: string;
  title: string;
  description: string;
  itineraries: CreateItineraryRequest[];
  destinationIds?: string[];
  imageUrl?: string;
  duration?: string;
}

export interface UpdateTourRequest {
  title: string;
  description: string;
  status?: TourStatus;
  destinationIds?: string[];
}

export interface CreateScheduleRequest {
  departureTime: string;
  arrivalTime: string;
  priceAdult: number;
  priceChild: number;
  totalSeats: number;
  assignedGuideId?: string;
}

export interface CreateDestinationRequest {
  name: string;
  city: string;
  country: string;
  imageUrl?: string;
}

// ---------- Booking Service ----------
export type BookingStatus =
  | "PENDING"
  | "SEATS_RESERVED"
  | "PAYMENT_PENDING"
  | "CONFIRMED"
  | "CANCELLED";
export type PassengerType = "ADULT" | "CHILD" | "INFANT";

export interface PassengerRequest {
  fullName: string;
  dateOfBirth?: string;
  passengerType: PassengerType;
  idCardNumber?: string;
}

export interface CreateBookingRequest {
  tourScheduleId: string;
  passengers: PassengerRequest[];
}

export interface BookingVm {
  bookingId: string;
  bookingCode: string;
  userId: string;
  tourScheduleId: string;
  totalAmount: number;
  status: BookingStatus;
  createdAt: string;
  paymentDeadline?: string;
  tourTitle?: string;
  departureTime?: string;
}

export interface BookingPassengerVm {
  id: string;
  fullName: string;
  dateOfBirth?: string;
  passengerType: PassengerType;
  idCardNumber?: string;
  price: number;
  checkedIn?: boolean;
}

export interface BookingDetailVm {
  bookingId: string;
  bookingCode: string;
  userId: string;
  userEmail?: string;
  userFullName?: string;
  tourScheduleId: string;
  tourId?: string;
  tourTitle?: string;
  tourCode?: string;
  departureTime?: string;
  arrivalTime?: string;
  totalAmount: number;
  status: BookingStatus;
  passengers: BookingPassengerVm[];
  paymentDeadline?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CancelBookingRequest {
  reason: string;
}

// ---------- Payment Service ----------
export type PaymentMethod = "VNPAY" | "MOMO" | "STRIPE";
export type PaymentStatus = "PENDING" | "SUCCESS" | "FAILED" | "REFUNDED";

export interface PaymentVm {
  id: string;
  bookingId: string;
  amount: number;
  paymentMethod: PaymentMethod;
  status: PaymentStatus;
  createdAt: string;
  updatedAt: string;
  transactionRef?: string;
}

export interface CreatePaymentUrlRequest {
  bookingId: string;
  paymentMethod: PaymentMethod;
  amount?: number;
}

export interface PaymentUrlVm {
  paymentId: string;
  bookingId: string;
  amount: number;
  paymentUrl: string;
}

export interface RefundPaymentRequest {
  reason: string;
}

// ---------- AI Service ----------
export interface AiQueryRequest {
  promptText?: string;
  queryText?: string;
  maxBudget?: number;
  destination?: string;
  topK?: number;
}

export interface RecommendedTourVm {
  tourId: string;
  code: string;
  title: string;
  pricePerAdult: number;
  bookingUrl: string;
  reason?: string;
}

export interface DayItineraryVm {
  day: number;
  title: string;
  description: string;
}

export interface TripRecommendationVm {
  introduction: string;
  recommendedTours: RecommendedTourVm[];
  detailedItinerary: DayItineraryVm[];
  cached?: boolean;
}

export interface ChatMessage {
  id: string;
  sender: "user" | "ai";
  text: string;
  timestamp: string;
  recommendedTours?: RecommendedTourVm[];
}

// ---------- Tour Guide Portal ----------
export interface GuideAssignedTourVm {
  scheduleId: string;
  tourId: string;
  tourCode: string;
  tourTitle: string;
  departureTime: string;
  arrivalTime: string;
  totalPassengers: number;
  checkedInCount: number;
  status: "UPCOMING" | "IN_PROGRESS" | "COMPLETED";
  passengers: (BookingPassengerVm & { bookingCode: string })[];
}

