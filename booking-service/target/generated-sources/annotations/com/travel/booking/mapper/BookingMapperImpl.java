package com.travel.booking.mapper;

import com.travel.booking.entity.Booking;
import com.travel.booking.entity.BookingPassenger;
import com.travel.booking.viewmodel.BookingDetailVm;
import com.travel.booking.viewmodel.BookingPassengerVm;
import com.travel.booking.viewmodel.BookingVm;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-19T00:25:08+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class BookingMapperImpl implements BookingMapper {

    @Override
    public BookingVm toBookingVm(Booking booking) {
        if ( booking == null ) {
            return null;
        }

        BookingVm.BookingVmBuilder bookingVm = BookingVm.builder();

        bookingVm.bookingId( booking.getId() );
        bookingVm.bookingCode( booking.getBookingCode() );
        bookingVm.createdAt( booking.getCreatedAt() );
        bookingVm.status( booking.getStatus() );
        bookingVm.totalAmount( booking.getTotalAmount() );
        bookingVm.tourScheduleId( booking.getTourScheduleId() );
        bookingVm.userId( booking.getUserId() );

        return bookingVm.build();
    }

    @Override
    public BookingDetailVm toBookingDetailVm(Booking booking) {
        if ( booking == null ) {
            return null;
        }

        BookingDetailVm.BookingDetailVmBuilder bookingDetailVm = BookingDetailVm.builder();

        bookingDetailVm.bookingId( booking.getId() );
        bookingDetailVm.bookingCode( booking.getBookingCode() );
        bookingDetailVm.createdAt( booking.getCreatedAt() );
        bookingDetailVm.passengers( bookingPassengerListToBookingPassengerVmList( booking.getPassengers() ) );
        bookingDetailVm.status( booking.getStatus() );
        bookingDetailVm.totalAmount( booking.getTotalAmount() );
        bookingDetailVm.tourScheduleId( booking.getTourScheduleId() );
        bookingDetailVm.updatedAt( booking.getUpdatedAt() );
        bookingDetailVm.userId( booking.getUserId() );

        return bookingDetailVm.build();
    }

    @Override
    public BookingPassengerVm toPassengerVm(BookingPassenger passenger) {
        if ( passenger == null ) {
            return null;
        }

        BookingPassengerVm.BookingPassengerVmBuilder bookingPassengerVm = BookingPassengerVm.builder();

        bookingPassengerVm.dateOfBirth( passenger.getDateOfBirth() );
        bookingPassengerVm.fullName( passenger.getFullName() );
        bookingPassengerVm.id( passenger.getId() );
        bookingPassengerVm.idCardNumber( passenger.getIdCardNumber() );
        bookingPassengerVm.passengerType( passenger.getPassengerType() );
        bookingPassengerVm.price( passenger.getPrice() );

        return bookingPassengerVm.build();
    }

    protected List<BookingPassengerVm> bookingPassengerListToBookingPassengerVmList(List<BookingPassenger> list) {
        if ( list == null ) {
            return null;
        }

        List<BookingPassengerVm> list1 = new ArrayList<BookingPassengerVm>( list.size() );
        for ( BookingPassenger bookingPassenger : list ) {
            list1.add( toPassengerVm( bookingPassenger ) );
        }

        return list1;
    }
}
