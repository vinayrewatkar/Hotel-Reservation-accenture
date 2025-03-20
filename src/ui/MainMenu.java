package ui;

import api.AdminResource;
import api.HotelResource;
import model.IRoom;
import model.Reservation;
import model.Room;
import model.RoomType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainMenu {
    private static final HotelResource hotelResource = HotelResource.getInstance();
    private static final Scanner scanner = new Scanner(System.in);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public static void displayMainMenu() {
        boolean keepRunning = true;

        while (keepRunning) {
            try {
                System.out.println("\nMain Menu");
                System.out.println("1. Find and reserve a room");
                System.out.println("2. See my reservations");
                System.out.println("3. Create an account");
                System.out.println("4. Admin");
                System.out.println("5. Exit");
                System.out.print("Please select an option: ");

                int selection = Integer.parseInt(scanner.nextLine());

                switch (selection) {
                    case 1:
                        findAndReserveRoom();
                        break;
                    case 2:
                        seeMyReservations();
                        break;
                    case 3:
                        createAccount();
                        break;
                    case 4:
                        AdminMenu.displayAdminMenu();
                        break;
                    case 5:
                        keepRunning = false;
                        System.out.println("Exiting the application. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid selection. Please try again.");
                }
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a number");
            } catch (Exception ex) {
                System.out.println("Unexpected error: " + ex.getMessage());
            }
        }
    }

    private static void findAndReserveRoom() {
        try {
            System.out.println("\nFind and Reserve a Room");

            // Get check-in date
            Date checkInDate = getValidFutureDate("Enter check-in date (MM/dd/yyyy): ");

            // Get check-out date
            Date checkOutDate = getValidFutureDate("Enter check-out date (MM/dd/yyyy): ");

            // Validate dates
            if (checkInDate.after(checkOutDate) || checkInDate.equals(checkOutDate)) {
                System.out.println("Error: Check-in date must be before check-out date");
                return;
            }

            // Find available rooms
            Collection<IRoom> availableRooms = hotelResource.findARoom(checkInDate, checkOutDate);

            // If no rooms available, try recommended dates
            if (availableRooms.isEmpty()) {
                System.out.println("No rooms available for the selected dates.");

                // Add 7 days to check-in and check-out dates
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(checkInDate);
                calendar.add(Calendar.DATE, 7);
                Date alternateCheckIn = calendar.getTime();

                calendar.setTime(checkOutDate);
                calendar.add(Calendar.DATE, 7);
                Date alternateCheckOut = calendar.getTime();

                System.out.println("Searching for rooms with alternate dates: " +
                        dateFormat.format(alternateCheckIn) + " to " +
                        dateFormat.format(alternateCheckOut));

                Collection<IRoom> alternativeRooms = hotelResource.findAlternativeRooms(checkInDate, checkOutDate);

                if (alternativeRooms.isEmpty()) {
                    System.out.println("No rooms available for the alternate dates either.");
                    return;
                } else {
                    System.out.println("\nAlternative rooms available for dates: " +
                            dateFormat.format(alternateCheckIn) + " to " +
                            dateFormat.format(alternateCheckOut));

                    displayRooms(alternativeRooms);

                    System.out.print("\nWould you like to book one of these rooms for the alternative dates? (y/n): ");
                    String bookAlternative = scanner.nextLine();

                    if (bookAlternative.equalsIgnoreCase("y")) {
                        checkInDate = alternateCheckIn;
                        checkOutDate = alternateCheckOut;
                        availableRooms = alternativeRooms;
                    } else {
                        return;
                    }
                }
            } else {
                System.out.println("\nAvailable Rooms for " + dateFormat.format(checkInDate) +
                        " to " + dateFormat.format(checkOutDate) + ":");
                displayRooms(availableRooms);
            }

            // Ask if user wants to book a room
            System.out.print("\nWould you like to book a room? (y/n): ");
            String bookChoice = scanner.nextLine();

            if (bookChoice.equalsIgnoreCase("y")) {
                // Ask for account email
                System.out.print("Enter your email: ");
                String email = scanner.nextLine();

                // Check if customer exists
                if (hotelResource.getCustomer(email) == null) {
                    System.out.println("Customer not found. Please create an account first.");
                    return;
                }

                // Ask which room to book
                System.out.print("Enter the room number you would like to book: ");
                String roomNumber = scanner.nextLine();

                // Check if room exists
                IRoom selectedRoom = hotelResource.getRoom(roomNumber);
                if (selectedRoom == null) {
                    System.out.println("Invalid room number.");
                    return;
                }

                // Check if room is available for the selected dates
                if (!hotelResource.isRoomAvailable(roomNumber, checkInDate, checkOutDate)) {
                    System.out.println("The selected room is not available for the chosen dates.");
                    return;
                }

                // Book the room
                try {
                    Reservation reservation = hotelResource.bookARoom(email, selectedRoom, checkInDate, checkOutDate);
                    System.out.println("\nReservation created successfully!");
                    System.out.println(reservation);
                } catch (Exception ex) {
                    System.out.println("Error booking room: " + ex.getMessage());
                }
            }
        } catch (ParseException ex) {
            System.out.println("Invalid date format. Please use MM/dd/yyyy");
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void displayRooms(Collection<IRoom> rooms) {
        for (IRoom room : rooms) {
            System.out.println(room);
        }
    }

    private static Date getValidFutureDate(String prompt) throws ParseException {
        Date date;
        Date today = new Date();
        boolean validDate = false;

        do {
            System.out.print(prompt);
            String dateString = scanner.nextLine();
            date = dateFormat.parse(dateString);

            if (date.before(today)) {
                System.out.println("Error: Date cannot be in the past. Please enter a future date.");
            } else {
                validDate = true;
            }
        } while (!validDate);

        return date;
    }

    private static void seeMyReservations() {
        System.out.println("\nSee My Reservations");
        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        try {
            // Check if customer exists
            if (hotelResource.getCustomer(email) == null) {
                System.out.println("Customer not found. Please create an account first.");
                return;
            }

            // Get and display reservations
            Collection<Reservation> reservations = hotelResource.getCustomersReservations(email);

            if (reservations == null || reservations.isEmpty()) {
                System.out.println("You have no reservations.");
                return;
            }

            System.out.println("\nYour Reservations:");
            for (Reservation reservation : reservations) {
                System.out.println("\n" + reservation);
                System.out.println("------------------------");
            }
        } catch (Exception ex) {
            System.out.println("Error retrieving reservations: " + ex.getMessage());
        }
    }

    private static void createAccount() {
        try {
            System.out.println("\nCreate an Account");
            System.out.print("Enter your email (format: name@domain.com): ");
            String email = scanner.nextLine();

            // Check if account already exists
            if (hotelResource.getCustomer(email) != null) {
                System.out.println("An account with this email already exists.");
                return;
            }

            System.out.print("Enter your first name: ");
            String firstName = scanner.nextLine();

            System.out.print("Enter your last name: ");
            String lastName = scanner.nextLine();

            hotelResource.createACustomer(email, firstName, lastName);
            System.out.println("Account created successfully!");
        } catch (IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Error creating account: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        displayMainMenu();
    }
}