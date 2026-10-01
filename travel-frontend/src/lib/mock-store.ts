import {
  BookingDetailVm,
  BookingStatus,
  CreateBookingRequest,
  CreateTourRequest,
  CreateScheduleRequest,
  DestinationVm,
  PageResponse,
  PaymentMethod,
  PaymentUrlVm,
  PaymentVm,
  RegisterRequest,
  TourDetailVm,
  TourSearchCriteria,
  TourVm,
  TripRecommendationVm,
  UserProfileVm,
  UserVm,
  GuideAssignedTourVm,
} from "./types";
import {
  MOCK_BOOKINGS,
  MOCK_DESTINATIONS,
  MOCK_PAYMENTS,
  MOCK_TOURS,
  MOCK_USERS,
  SAMPLE_AI_RECOMMENDATION,
} from "./mock-data";

const STORAGE_KEYS = {
  TOURS: "tm_mock_tours",
  BOOKINGS: "tm_mock_bookings",
  PAYMENTS: "tm_mock_payments",
  DESTINATIONS: "tm_mock_destinations",
  USERS: "tm_mock_users",
  CURRENT_USER: "tm_mock_current_user",
};

function getItem<T>(key: string, defaultVal: T): T {
  if (typeof window === "undefined") return defaultVal;
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : defaultVal;
  } catch {
    return defaultVal;
  }
}

function setItem<T>(key: string, val: T): void {
  if (typeof window === "undefined") return;
  try {
    localStorage.setItem(key, JSON.stringify(val));
  } catch {}
}

export class MockStore {
  // Tour operations
  static getTours(criteria: TourSearchCriteria): PageResponse<TourVm> {
    const allTours = getItem<TourDetailVm[]>(STORAGE_KEYS.TOURS, MOCK_TOURS);
    let filtered = allTours.filter((t) => t.status === "PUBLISHED" || criteria.keyword?.includes("admin"));

    if (criteria.keyword) {
      const kw = criteria.keyword.toLowerCase();
      filtered = filtered.filter(
        (t) =>
          t.title.toLowerCase().includes(kw) ||
          t.code.toLowerCase().includes(kw) ||
          t.description.toLowerCase().includes(kw)
      );
    }

    if (criteria.destination) {
      const dest = criteria.destination.toLowerCase();
      filtered = filtered.filter((t) =>
        t.destinations.some(
          (d) =>
            d.city.toLowerCase().includes(dest) ||
            d.name.toLowerCase().includes(dest)
        )
      );
    }

    if (criteria.minPrice !== undefined) {
      filtered = filtered.filter((t) => {
        const minP = Math.min(...t.schedules.map((s) => s.priceAdult), 99999999);
        return minP >= (criteria.minPrice ?? 0);
      });
    }

    if (criteria.maxPrice !== undefined) {
      filtered = filtered.filter((t) => {
        const minP = Math.min(...t.schedules.map((s) => s.priceAdult), 99999999);
        return minP <= (criteria.maxPrice ?? 99999999);
      });
    }

    const page = criteria.page ?? 0;
    const size = criteria.size ?? 10;
    const totalElements = filtered.length;
    const totalPages = Math.ceil(totalElements / size) || 1;
    const start = page * size;
    const paged = filtered.slice(start, start + size);

    const content: TourVm[] = paged.map((t) => ({
      tourId: t.tourId,
      code: t.code,
      title: t.title,
      minPrice: t.schedules.length > 0 ? Math.min(...t.schedules.map((s) => s.priceAdult)) : 0,
      availableSeats: t.schedules.reduce((acc, s) => acc + s.availableSeats, 0),
      status: t.status,
      createdAt: t.createdAt,
      duration: t.duration,
      imageUrl: t.imageUrl,
      rating: t.rating,
      reviewCount: t.reviewCount,
      destinationCity: t.destinations[0]?.city,
    }));

    return {
      content,
      totalElements,
      totalPages,
      size,
      number: page,
      first: page === 0,
      last: page >= totalPages - 1,
      numberOfElements: content.length,
      empty: content.length === 0,
    };
  }

  static getTourById(id: string): TourDetailVm | null {
    const allTours = getItem<TourDetailVm[]>(STORAGE_KEYS.TOURS, MOCK_TOURS);
    const cleanId = id.trim().toLowerCase();
    return (
      allTours.find(
        (t) =>
          t.tourId === id ||
          t.code.toLowerCase() === cleanId ||
          t.tourId.toLowerCase() === cleanId ||
          cleanId.includes(t.code.toLowerCase()) ||
          t.code.toLowerCase().includes(cleanId)
      ) ??
      allTours[0] ??
      null
    );
  }

  static createTour(req: CreateTourRequest): TourDetailVm {
    const allTours = getItem<TourDetailVm[]>(STORAGE_KEYS.TOURS, MOCK_TOURS);
    const dests = getItem<DestinationVm[]>(STORAGE_KEYS.DESTINATIONS, MOCK_DESTINATIONS);
    const selectedDests = dests.filter((d) => req.destinationIds?.includes(d.id));

    const newTour: TourDetailVm = {
      tourId: `tour-${Date.now()}`,
      code: req.code.toUpperCase(),
      title: req.title,
      description: req.description,
      status: "PUBLISHED",
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      duration: req.duration ?? "3 Ngày 2 Đêm",
      imageUrl: req.imageUrl ?? "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&w=800&q=80",
      rating: 5.0,
      reviewCount: 1,
      includedServices: ["Xe du lịch đời mới", "Khách sạn 4 sao", "Hướng dẫn viên", "Các bữa ăn theo chương trình"],
      excludedServices: ["Chi phí cá nhân", "Vé máy bay"],
      destinations: selectedDests.length > 0 ? selectedDests : [MOCK_DESTINATIONS[0]],
      itineraries: req.itineraries.map((it, idx) => ({
        id: `itin-new-${idx}`,
        dayNumber: it.dayNumber,
        title: it.title,
        content: it.content,
      })),
      schedules: [
        {
          id: `sch-new-${Date.now()}`,
          departureTime: new Date(Date.now() + 7 * 86400000).toISOString(),
          arrivalTime: new Date(Date.now() + 10 * 86400000).toISOString(),
          priceAdult: 4500000,
          priceChild: 3150000,
          totalSeats: 30,
          availableSeats: 30,
          assignedGuideId: "user-guide-01",
          assignedGuideName: "Trần Văn Hướng Dẫn",
        },
      ],
    };

    allTours.unshift(newTour);
    setItem(STORAGE_KEYS.TOURS, allTours);
    return newTour;
  }

  static addSchedule(tourId: string, req: CreateScheduleRequest): TourDetailVm {
    const allTours = getItem<TourDetailVm[]>(STORAGE_KEYS.TOURS, MOCK_TOURS);
    const tour = allTours.find((t) => t.tourId === tourId);
    if (!tour) throw new Error("Tour không tồn tại");

    const newSch = {
      id: `sch-${Date.now()}`,
      departureTime: req.departureTime,
      arrivalTime: req.arrivalTime,
      priceAdult: req.priceAdult,
      priceChild: req.priceChild,
      totalSeats: req.totalSeats,
      availableSeats: req.totalSeats,
      assignedGuideId: req.assignedGuideId ?? "user-guide-01",
      assignedGuideName: "Trần Văn Hướng Dẫn",
    };

    tour.schedules.push(newSch);
    setItem(STORAGE_KEYS.TOURS, allTours);
    return tour;
  }

  // Booking operations
  static createBooking(userId: string, req: CreateBookingRequest): BookingDetailVm {
    const allTours = getItem<TourDetailVm[]>(STORAGE_KEYS.TOURS, MOCK_TOURS);
    let targetTour: TourDetailVm | null = null;
    let targetSch: any = null;

    for (const t of allTours) {
      const s = t.schedules.find((sc) => sc.id === req.tourScheduleId);
      if (s) {
        targetTour = t;
        targetSch = s;
        break;
      }
    }

    if (!targetTour || !targetSch) {
      throw new Error("Lịch khởi hành không tồn tại");
    }

    if (targetSch.availableSeats < req.passengers.length) {
      throw new Error(`Lịch khởi hành không đủ số chỗ khả dụng (Còn ${targetSch.availableSeats} chỗ)`);
    }

    // Deduct seats
    targetSch.availableSeats -= req.passengers.length;
    setItem(STORAGE_KEYS.TOURS, allTours);

    // Calculate total amount
    let totalAmount = 0;
    const passengersWithPrice = req.passengers.map((p, idx) => {
      const price = p.passengerType === "CHILD" ? targetSch.priceChild : p.passengerType === "INFANT" ? 0 : targetSch.priceAdult;
      totalAmount += price;
      return {
        id: `pass-${Date.now()}-${idx}`,
        fullName: p.fullName,
        dateOfBirth: p.dateOfBirth,
        passengerType: p.passengerType,
        idCardNumber: p.idCardNumber,
        price,
        checkedIn: false,
      };
    });

    const now = new Date();
    const deadline = new Date(now.getTime() + 15 * 60 * 1000).toISOString();

    const users = getItem<UserVm[]>(STORAGE_KEYS.USERS, MOCK_USERS);
    const user = users.find((u) => u.userId === userId) ?? MOCK_USERS[0];

    const newBooking: BookingDetailVm = {
      bookingId: `bk-${Date.now()}`,
      bookingCode: `BK${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, "0")}${String(now.getDate()).padStart(2, "0")}-${Math.random().toString(36).substring(2, 6).toUpperCase()}`,
      userId,
      userEmail: user.email,
      userFullName: user.fullName,
      tourScheduleId: req.tourScheduleId,
      tourId: targetTour.tourId,
      tourTitle: targetTour.title,
      tourCode: targetTour.code,
      departureTime: targetSch.departureTime,
      arrivalTime: targetSch.arrivalTime,
      totalAmount,
      status: "PENDING",
      passengers: passengersWithPrice,
      paymentDeadline: deadline,
      createdAt: now.toISOString(),
      updatedAt: now.toISOString(),
    };

    const bookings = getItem<BookingDetailVm[]>(STORAGE_KEYS.BOOKINGS, MOCK_BOOKINGS);
    bookings.unshift(newBooking);
    setItem(STORAGE_KEYS.BOOKINGS, bookings);

    return newBooking;
  }

  static getBookingById(bookingId: string): BookingDetailVm | null {
    const bookings = getItem<BookingDetailVm[]>(STORAGE_KEYS.BOOKINGS, MOCK_BOOKINGS);
    return bookings.find((b) => b.bookingId === bookingId || b.bookingCode === bookingId) ?? null;
  }

  static getMyBookings(userId: string): BookingDetailVm[] {
    const bookings = getItem<BookingDetailVm[]>(STORAGE_KEYS.BOOKINGS, MOCK_BOOKINGS);
    return bookings.filter((b) => b.userId === userId);
  }

  static getAllBookings(): BookingDetailVm[] {
    return getItem<BookingDetailVm[]>(STORAGE_KEYS.BOOKINGS, MOCK_BOOKINGS);
  }

  static cancelBooking(bookingId: string, reason: string): BookingDetailVm {
    const bookings = getItem<BookingDetailVm[]>(STORAGE_KEYS.BOOKINGS, MOCK_BOOKINGS);
    const booking = bookings.find((b) => b.bookingId === bookingId || b.bookingCode === bookingId);
    if (!booking) throw new Error("Đơn đặt tour không tồn tại");

    booking.status = "CANCELLED";
    booking.updatedAt = new Date().toISOString();

    // Restore seats
    const allTours = getItem<TourDetailVm[]>(STORAGE_KEYS.TOURS, MOCK_TOURS);
    for (const t of allTours) {
      const s = t.schedules.find((sc) => sc.id === booking.tourScheduleId);
      if (s) {
        s.availableSeats += booking.passengers.length;
        break;
      }
    }
    setItem(STORAGE_KEYS.TOURS, allTours);
    setItem(STORAGE_KEYS.BOOKINGS, bookings);

    return booking;
  }

  // Payment operations
  static createPaymentUrl(bookingId: string, method: PaymentMethod): PaymentUrlVm {
    const booking = this.getBookingById(bookingId);
    if (!booking) throw new Error("Đơn hàng không tồn tại");

    const paymentId = `pay-${Date.now()}`;
    const paymentUrl = `/checkout/${booking.bookingId}?method=${method}&paymentId=${paymentId}`;

    return {
      paymentId,
      bookingId: booking.bookingId,
      amount: booking.totalAmount,
      paymentUrl,
    };
  }

  static simulatePaymentSuccess(bookingId: string, method: PaymentMethod): BookingDetailVm {
    const bookings = getItem<BookingDetailVm[]>(STORAGE_KEYS.BOOKINGS, MOCK_BOOKINGS);
    const booking = bookings.find((b) => b.bookingId === bookingId || b.bookingCode === bookingId);
    if (!booking) throw new Error("Đơn hàng không tồn tại");

    booking.status = "CONFIRMED";
    booking.updatedAt = new Date().toISOString();
    setItem(STORAGE_KEYS.BOOKINGS, bookings);

    const payments = getItem<PaymentVm[]>(STORAGE_KEYS.PAYMENTS, MOCK_PAYMENTS);
    payments.unshift({
      id: `pay-${Date.now()}`,
      bookingId: booking.bookingId,
      amount: booking.totalAmount,
      paymentMethod: method,
      status: "SUCCESS",
      transactionRef: `${method}-${Date.now().toString().slice(-6)}`,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    });
    setItem(STORAGE_KEYS.PAYMENTS, payments);

    return booking;
  }

  // Guide operations
  static getGuideAssignedTours(guideId: string = "user-guide-01"): GuideAssignedTourVm[] {
    const allTours = getItem<TourDetailVm[]>(STORAGE_KEYS.TOURS, MOCK_TOURS);
    const bookings = getItem<BookingDetailVm[]>(STORAGE_KEYS.BOOKINGS, MOCK_BOOKINGS);

    const result: GuideAssignedTourVm[] = [];

    for (const t of allTours) {
      for (const s of t.schedules) {
        if (s.assignedGuideId === guideId || guideId === "user-guide-01") {
          const scheduleBookings = bookings.filter(
            (b) => b.tourScheduleId === s.id && b.status === "CONFIRMED"
          );
          const passengersWithCode: any[] = [];
          scheduleBookings.forEach((b) => {
            b.passengers.forEach((p) => {
              passengersWithCode.push({ ...p, bookingCode: b.bookingCode });
            });
          });

          const checkedInCount = passengersWithCode.filter((p) => p.checkedIn).length;

          result.push({
            scheduleId: s.id,
            tourId: t.tourId,
            tourCode: t.code,
            tourTitle: t.title,
            departureTime: s.departureTime,
            arrivalTime: s.arrivalTime,
            totalPassengers: passengersWithCode.length,
            checkedInCount,
            status: "UPCOMING",
            passengers: passengersWithCode,
          });
        }
      }
    }
    return result;
  }

  static togglePassengerCheckIn(scheduleId: string, passengerId: string): boolean {
    const bookings = getItem<BookingDetailVm[]>(STORAGE_KEYS.BOOKINGS, MOCK_BOOKINGS);
    let updated = false;
    let newStatus = false;

    for (const b of bookings) {
      if (b.tourScheduleId === scheduleId) {
        const p = b.passengers.find((pass) => pass.id === passengerId);
        if (p) {
          p.checkedIn = !p.checkedIn;
          newStatus = p.checkedIn;
          updated = true;
          break;
        }
      }
    }

    if (updated) {
      setItem(STORAGE_KEYS.BOOKINGS, bookings);
    }
    return newStatus;
  }

  // Auth & Admin operations
  static register(req: RegisterRequest): UserVm {
    const users = getItem<UserVm[]>(STORAGE_KEYS.USERS, MOCK_USERS);
    if (users.some((u) => u.email === req.email)) {
      throw new Error("Email này đã được sử dụng");
    }

    const isGuide = req.accountType === "GUIDE";
    const newUser: UserVm = {
      userId: `user-${Date.now()}`,
      email: req.email,
      fullName: req.fullName,
      status: isGuide ? "PENDING_APPROVAL" : "ACTIVE",
      roles: isGuide ? ["ROLE_GUIDE"] : ["ROLE_TOURIST"],
      phoneNumber: req.phoneNumber,
      createdAt: new Date().toISOString(),
    };

    users.push(newUser);
    setItem(STORAGE_KEYS.USERS, users);
    return newUser;
  }

  static getUsers(): UserVm[] {
    return getItem<UserVm[]>(STORAGE_KEYS.USERS, MOCK_USERS);
  }

  static approveGuide(userId: string): UserVm {
    const users = getItem<UserVm[]>(STORAGE_KEYS.USERS, MOCK_USERS);
    const u = users.find((usr) => usr.userId === userId);
    if (!u) throw new Error("Người dùng không tồn tại");

    u.status = "ACTIVE";
    setItem(STORAGE_KEYS.USERS, users);
    return u;
  }

  static getDestinations(): DestinationVm[] {
    return getItem<DestinationVm[]>(STORAGE_KEYS.DESTINATIONS, MOCK_DESTINATIONS);
  }

  // AI recommendations & Chatbot
  static getAiRecommendation(prompt: string): TripRecommendationVm {
    const q = (prompt || "").toLowerCase();

    if (q.includes("phú quốc") || q.includes("phu quoc")) {
      const tour = MOCK_TOURS.find((t) => t.tourId === "tour-02") || MOCK_TOURS[1];
      return {
        introduction: `Chào bạn! Dựa trên nhu cầu "${prompt}", AI tư vấn đề xuất Tour Đảo Ngọc Phú Quốc 4N3Đ trọn gói nghỉ dưỡng resort biển, cáp treo Hòn Thơm và VinWonders.`,
        recommendedTours: [
          {
            tourId: tour.tourId,
            code: tour.code,
            title: tour.title,
            pricePerAdult: 6200000,
            bookingUrl: `/tours/${tour.tourId}`,
            reason: "Trải nghiệm cáp treo Hòn Thơm vượt biển dài nhất thế giới, lặn ngắm san hô 4 đảo và vui chơi VinWonders Safari.",
          },
        ],
        detailedItinerary: [
          { day: 1, title: "Đón Sân Bay Phú Quốc - Check-in Sunset Sanato - Dinh Cậu", description: "Nghỉ dưỡng resort 4 sao sát biển và ngắm hoàng hôn rực rỡ." },
          { day: 2, title: "VinWonders & Công Viên Bán Hoang Dã Safari", description: "Khám phá thế giới động vật bán hoang dã và Thủy Cung Cung Điện Hải Vương." },
          { day: 3, title: "Tour Cano 4 Đảo Nam Đảo - Lặn San Hô Hòn Mây Rút", description: "Cano vượt sóng lặn ngắm san hô tự nhiên và tắm biển bãi Sao." },
          { day: 4, title: "Mua Sắm Đặc Sản Ngọc Trai, Nước Mắm - Tiễn Sân Bay", description: "Tham quan cơ sở nước mắm truyền thống Khải Hoàn." },
        ],
      };
    }

    if (q.includes("hà giang") || q.includes("ha giang")) {
      const tour = MOCK_TOURS.find((t) => t.tourId === "tour-03") || MOCK_TOURS[2];
      return {
        introduction: `Chào bạn! Dựa trên nhu cầu "${prompt}", AI tư vấn đề xuất Tour Phượt Hà Giang Loop 3N2Đ chinh phục Đèo Mã Pí Lèng và Sông Nho Quế.`,
        recommendedTours: [
          {
            tourId: tour.tourId,
            code: tour.code,
            title: tour.title,
            pricePerAdult: 3850000,
            bookingUrl: `/tours/${tour.tourId}`,
            reason: "Chinh phục cung đường đèo hùng vĩ nhất Việt Nam, đi thuyền hẻm Tu Sản và giao lưu văn hóa H'Mông bản địa.",
          },
        ],
        detailedItinerary: [
          { day: 1, title: "Hà Nội - Hà Giang - Quản Bạ - Yên Minh", description: "Vượt dốc Bắc Sum, ngắm Cổng Trời Quản Bạ và Núi Đôi Cô Tiên." },
          { day: 2, title: "Đèo Mã Pí Lèng - Sông Nho Quế - Cột Cờ Lũng Cú", description: "Săn mây cực Bắc Lũng Cú và chèo thuyền sông Nho Quế xanh ngọc." },
          { day: 3, title: "Chợ Phiên Đồng Văn - Dinh Thự Vua Mèo - Hà Nội", description: "Trải nghiệm không khí chợ phiên vùng cao rực rỡ sắc màu." },
        ],
      };
    }

    if (q.includes("hạ long") || q.includes("ha long") || q.includes("quảng ninh")) {
      const tour = MOCK_TOURS.find((t) => t.tourId === "tour-05") || MOCK_TOURS[4];
      return {
        introduction: `Chào bạn! Dựa trên nhu cầu "${prompt}", AI tư vấn đề xuất Tour Du Thuyền 5 Sao Vịnh Hạ Long 2N1Đ sang trọng.`,
        recommendedTours: [
          {
            tourId: tour.tourId,
            code: tour.code,
            title: tour.title,
            pricePerAdult: 3500000,
            bookingUrl: `/tours/${tour.tourId}`,
            reason: "Nghỉ dưỡng trên du thuyền 5 sao, chèo Kayak khám phá Hang Sửng Sốt và ngắm hoàng hôn kỳ vĩ.",
          },
        ],
        detailedItinerary: [
          { day: 1, title: "Check-in Du Thuyền 5 Star - Hang Sửng Sốt - Sunset Party", description: "Thưởng thức tiệc trà chiều ngắm hoàng hôn buông xuống vịnh." },
          { day: 2, title: "Tập Taichi - Chèo Kayak Đảo Titop - Cảng Tuần Châu", description: "Tắm biển bãi Titop và chinh phục đỉnh núi ngắm toàn cảnh vịnh." },
        ],
      };
    }

    if (q.includes("sapa") || q.includes("sa pa") || q.includes("lào cai")) {
      const tour = MOCK_TOURS.find((t) => t.tourId === "tour-06") || MOCK_TOURS[5];
      return {
        introduction: `Chào bạn! Dựa trên nhu cầu "${prompt}", AI tư vấn đề xuất Tour Sa Pa Săn Mây Fansipan 3N2Đ - Bản Cát Cát.`,
        recommendedTours: [
          {
            tourId: tour.tourId,
            code: tour.code,
            title: tour.title,
            pricePerAdult: 3290000,
            bookingUrl: `/tours/${tour.tourId}`,
            reason: "Săn mây trên 'Nóc nhà Đông Dương' Fansipan 3.143m và check-in bản Cát Cát sống ảo.",
          },
        ],
        detailedItinerary: [
          { day: 1, title: "Hà Nội - Sa Pa - Tham Quan Bản Cát Cát", description: "Trải nghiệm chụp ảnh trang phục dân tộc tại thung lũng Mường Hoa." },
          { day: 2, title: "Chinh Phục Đỉnh Fansipan 3.143m - Moana Check-in", description: "Đi cáp treo ngắm thung lũng mây bồng bềnh." },
          { day: 3, title: "Nhà Thờ Đá Sa Pa - Mua Sắm Chợ Sa Pa - Về Hà Nội", description: "Thưởng thức đồ nướng Sa Pa đặc sắc." },
        ],
      };
    }

    if (q.includes("nha trang")) {
      const ntTour = MOCK_TOURS.find((t) => t.tourId === "tour-04") || MOCK_TOURS[3];
      return {
        introduction: `Chào bạn! Dựa trên nhu cầu "${prompt}", AI tư vấn đề xuất Tour Nha Trang biển đảo 3N2Đ với lịch trình hải sản & nghỉ dưỡng resort cực đỉnh.`,
        recommendedTours: [
          {
            tourId: ntTour.tourId,
            code: ntTour.code,
            title: ntTour.title,
            pricePerAdult: 4200000,
            bookingUrl: `/tours/${ntTour.tourId}`,
            reason: "Trải nghiệm lặn ngắm san hô Hòn Mun và công viên nước VinWonders sôi động.",
          },
        ],
        detailedItinerary: [
          { day: 1, title: "Tắm bùn khoáng I-Resort & Tháp Bà Ponagar", description: "Thư giãn chăm sóc sức khỏe." },
          { day: 2, title: "Lặn biển Hòn Mun & Cano 3 Đảo", description: "Ngắm thế giới sinh vật biển phong phú." },
          { day: 3, title: "Vui chơi VinWonders Hòn Tre & Tiễn Sân Bay", description: "Giải trí hàng đầu." },
        ],
      };
    }

    return SAMPLE_AI_RECOMMENDATION;
  }

  static getAiChatReply(text: string): string {
    const query = text.toLowerCase();
    if (query.includes("hủy") || query.includes("hoàn tiền")) {
      return "Quy định hủy tour: Quý khách được hoàn 100% chi phí nếu hủy trước 7 ngày departure. Hủy trước 3-6 ngày hoàn 50%. Hủy trong vòng 48h không được hoàn tiền trừ trường hợp bất khả kháng.";
    }
    if (query.includes("thanh toán") || query.includes("vnpay")) {
      return "Hệ thống hỗ trợ thanh toán trực tuyến qua VNPAY QR, Ví MoMo, Thẻ ATM Nội địa và Thẻ Quốc tế Visa/Mastercard. Quý khách có 15 phút giữ chỗ tạm thời sau khi tạo đơn.";
    }
    if (query.includes("trẻ em") || query.includes("vé")) {
      return "Giá vé trẻ em (từ 5 - 11 tuổi) được tính bằng 70% giá vé người lớn. Trẻ em dưới 5 tuổi được miễn phí (dùng chung dịch vụ với cha mẹ).";
    }
    return `Cảm ơn bạn đã hỏi! Về vấn đề "${text}", đội ngũ Chăm sóc khách hàng & AI Travel Bot luôn sẵn sàng hỗ trợ 24/7. Quý khách có thể xem trực tiếp các tour khuyến mãi trên trang chủ hoặc gửi yêu cầu chi tiết.`;
  }
}
