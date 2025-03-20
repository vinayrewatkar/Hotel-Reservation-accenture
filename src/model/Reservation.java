package model;

import java.util.Date;
import java.util.Objects;

public class Reservation {
    private final Customer customer;
    private final IRoom room;
    private final Date checkInDate;
    private final Date checkOutDate;

    public Reservation(Customer customer, IRoom room, Date checkInDate, Date checkOutDate) {
        this.customer = customer;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public IRoom getRoom() {
        return room;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    @Override
    public String toString() {
        return "Reservation Details:" +
                "\nCustomer: " + customer.getFirstName() + " " + customer.getLastName() +
                "\nRoom: " + room.getRoomNumber() + " - " + room.getRoomType() +
                "\nPrice: " + (room.isFree() ? "FREE" : "$" + room.getRoomPrice()) +
                "\nCheck-In Date: " + checkInDate +
                "\nCheck-Out Date: " + checkOutDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Reservation that = (Reservation) obj;
        return Objects.equals(room.getRoomNumber(), that.room.getRoomNumber()) &&
                isDateRangeOverlap(this.checkInDate, this.checkOutDate, that.checkInDate, that.checkOutDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(room.getRoomNumber(), checkInDate, checkOutDate);
    }

    private boolean isDateRangeOverlap(Date checkIn1, Date checkOut1, Date checkIn2, Date checkOut2) {
        return checkIn1.before(checkOut2) && checkOut1.after(checkIn2);
    }
}
