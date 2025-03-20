package ui;

import api.AdminResource;
import model.*;

import java.util.*;

public class AdminMenu {
    private static final AdminResource adminResource = AdminResource.getInstance();
    private static final Scanner scanner = new Scanner(System.in);

    public static void displayAdminMenu() {
        boolean keepRunning = true;

        while (keepRunning) {
            try {
                System.out.println("\nAdmin Menu");
                System.out.println("1. See all Customers");
                System.out.println("2. See all Rooms");
                System.out.println("3. See all Reservations");
                System.out.println("4. Add a Room");
                System.out.println("5. Back to Main Menu");
                System.out.print("Please select an option: ");

                int selection = Integer.parseInt(scanner.nextLine());

                switch (selection) {
                    case 1:
                        seeAllCustomers();
                        break;
                    case 2:
                        seeAllRooms();
                        break;
                    case 3:
                        seeAllReservations();
                        break;
                    case 4:
                        addARoom();
                        break;
                    case 5:
                        keepRunning = false;
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

    private static void seeAllCustomers() {
        Collection<Customer> customers = adminResource.getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }

        System.out.println("\nAll Customers:");
        for (Customer customer : customers) {
            System.out.println(customer);
        }
    }

    private static void seeAllRooms() {
        Collection<IRoom> rooms = adminResource.getAllRooms();

        if (rooms.isEmpty()) {
            System.out.println("No rooms found.");
            return;
        }

        System.out.println("\nAll Rooms:");
        for (IRoom room : rooms) {
            System.out.println(room);
        }
    }

    private static void seeAllReservations() {
        System.out.println("\nAll Reservations:");
        adminResource.displayAllReservations();
    }

    //Multiple same rooms error resolved (please test it reviewer)
    private static void addARoom() {
        try {
            System.out.println("\nAdd a Room");
            System.out.print("Enter room number: ");
            String roomNumber = scanner.nextLine();

            // Check if room number already exists
            if (adminResource.getAllRooms().stream().anyMatch(room -> room.getRoomNumber().equals(roomNumber))) {
                System.out.println("Room number already exists. Please choose a different number.");
                return;
            }
            RoomType roomType = null;
            boolean validRoomType = false;
            while (!validRoomType) {
                System.out.print("Enter room type (1 for SINGLE, 2 for DOUBLE): ");
                try {
                    int roomTypeSelection = Integer.parseInt(scanner.nextLine());
                    switch (roomTypeSelection) {
                        case 1:
                            roomType = RoomType.SINGLE;
                            validRoomType = true;
                            break;
                        case 2:
                            roomType = RoomType.DOUBLE;
                            validRoomType = true;
                            break;
                        default:
                            System.out.println("Invalid selection. Please enter 1 for SINGLE or 2 for DOUBLE.");
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Please enter a number");
                }
            }

            Double price = null;
            boolean validPrice = false;
            while (!validPrice) {
                System.out.print("Enter price per night ($0 for free room): $");
                try {
                    price = Double.parseDouble(scanner.nextLine());
                    if (price < 0) {
                        System.out.println("Price cannot be negative.");
                    } else {
                        validPrice = true;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Please enter a valid number");
                }
            }
            IRoom room;
            if (price == 0) {
                room = new FreeRoom(roomNumber, roomType);
            } else {
                room = new Room(roomNumber, price, roomType);
            }
            List<IRoom> rooms = new ArrayList<>();
            rooms.add(room);
            adminResource.addRoom(rooms);

            System.out.println("Room added successfully!");

        } catch (Exception ex) {
            System.out.println("Error adding room: " + ex.getMessage());
        }
    }
}
