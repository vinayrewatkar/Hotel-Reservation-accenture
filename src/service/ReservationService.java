package service;

import model.IRoom;
import model.Room;
import model.Reservation;
import model.Customer;

import java.util.*;
import java.util.stream.Collectors;

public class ReservationService {
    private static final ReservationService instance = new ReservationService();
    private final Map<String, IRoom> rooms = new HashMap<>();
    private final Map<String, Collection<Reservation>> reservations = new HashMap<>();

    private ReservationService() {}

    public static ReservationService getInstance() {
        return instance;
    }

    public void addRoom(IRoom room) {
        if (room == null || room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Room and room number cannot be null or empty");
        }

        if (rooms.containsKey(room.getRoomNumber())) {
            throw new IllegalArgumentException("Room with number " + room.getRoomNumber() + " already exists");
        }

        rooms.put(room.getRoomNumber(), room);
    }

    public IRoom getARoom(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        return rooms.get(roomId);
    }

    public Reservation reserveARoom(Customer customer, IRoom room, Date checkInDate, Date checkOutDate) {
        if (customer == null || room == null || checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }

        if (checkInDate.after(checkOutDate) || checkInDate.equals(checkOutDate)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }

        Date today = new Date();
        if (checkInDate.before(today)) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        // Check if room is available for the given date range
        if (!isRoomAvailable(room.getRoomNumber(), checkInDate, checkOutDate)) {
            throw new IllegalArgumentException("Room is not available for the selected dates");
        }

        Reservation reservation = new Reservation(customer, room, checkInDate, checkOutDate);

        Collection<Reservation> customerReservations = reservations.getOrDefault(customer.getEmail(), new ArrayList<>());
        customerReservations.add(reservation);
        reservations.put(customer.getEmail(), customerReservations);

        return reservation;
    }

    public Collection<IRoom> findRooms(Date checkInDate, Date checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("Check-in and check-out dates cannot be null");
        }

        if (checkInDate.after(checkOutDate) || checkInDate.equals(checkOutDate)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }

        Set<String> bookedRoomNumbers = getBookedRoomNumbers(checkInDate, checkOutDate);

        return rooms.values().stream()
                .filter(room -> !bookedRoomNumbers.contains(room.getRoomNumber()))
                .collect(Collectors.toList());
    }

    public Collection<IRoom> findAlternativeRooms(Date checkInDate, Date checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("Check-in and check-out dates cannot be null");
        }

        // Calculate the duration between check-in and check-out
        long duration = checkOutDate.getTime() - checkInDate.getTime();

        // Iterate through the next 7 days to find available rooms
        for (int i = 1; i <= 7; i++) {
            // Create new check-in date by adding i days to original check-in
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(checkInDate);
            calendar.add(Calendar.DATE, i);
            Date alternateCheckIn = calendar.getTime();

            // Create new check-out date by adding the same duration
            Date alternateCheckOut = new Date(alternateCheckIn.getTime() + duration);

            // Check for available rooms with these new dates
            Collection<IRoom> availableRooms = findRooms(alternateCheckIn, alternateCheckOut);

            // If rooms are available for these dates, return them
            if (!availableRooms.isEmpty()) {
                return availableRooms;
            }
        }

        // No available rooms found within the 7-day window
        return new ArrayList<>();
    }

    public Collection<Reservation> getCustomersReservation(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        return reservations.getOrDefault(customer.getEmail(), new ArrayList<>());
    }

    public void printAllReservation() {
        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }

        for (Collection<Reservation> customerReservations : reservations.values()) {
            for (Reservation reservation : customerReservations) {
                System.out.println(reservation);
                System.out.println("------------------------");
            }
        }
    }

    private Set<String> getBookedRoomNumbers(Date checkInDate, Date checkOutDate) {
        Set<String> bookedRoomNumbers = new HashSet<>();

        for (Collection<Reservation> customerReservations : reservations.values()) {
            for (Reservation reservation : customerReservations) {
                if (isDateRangeOverlap(reservation.getCheckInDate(), reservation.getCheckOutDate(), checkInDate, checkOutDate)) {
                    bookedRoomNumbers.add(reservation.getRoom().getRoomNumber());
                }
            }
        }

        return bookedRoomNumbers;
    }

    private boolean isDateRangeOverlap(Date existingCheckIn, Date existingCheckOut, Date requestedCheckIn, Date requestedCheckOut) {
        return existingCheckIn.before(requestedCheckOut) && existingCheckOut.after(requestedCheckIn);
    }

    public boolean isRoomAvailable(String roomNumber, Date checkInDate, Date checkOutDate) {
        if (roomNumber == null || checkInDate == null || checkOutDate == null) {
            return false;
        }

        for (Collection<Reservation> customerReservations : reservations.values()) {
            for (Reservation reservation : customerReservations) {
                if (reservation.getRoom().getRoomNumber().equals(roomNumber) &&
                        isDateRangeOverlap(reservation.getCheckInDate(), reservation.getCheckOutDate(), checkInDate, checkOutDate)) {
                    return false;
                }
            }
        }

        return true;
    }

    public Collection<IRoom> getAllRooms() {
        return rooms.values();
    }

    public Collection<Reservation> getAllReservations() {
        List<Reservation> allReservations = new ArrayList<>();
        for (Collection<Reservation> customerReservations : reservations.values()) {
            allReservations.addAll(customerReservations);
        }
        return allReservations;
    }
}