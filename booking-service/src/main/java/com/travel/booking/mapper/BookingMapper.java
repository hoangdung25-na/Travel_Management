package com.travel.booking.mapper;

import com.travel.booking.entity.Booking;
import com.travel.booking.entity.BookingPassenger;
import com.travel.booking.viewmodel.BookingDetailVm;
import com.travel.booking.viewmodel.BookingPassengerVm;
import com.travel.booking.viewmodel.BookingVm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "bookingId", source = "id")
    BookingVm toBookingVm(Booking booking);

    @Mapping(target = "bookingId", source = "id")
    BookingDetailVm toBookingDetailVm(Booking booking);

    BookingPassengerVm toPassengerVm(BookingPassenger passenger);
}
