import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

class EmployeeShift {
    String name;
    String positionId;
    LocalDateTime startTime;
    LocalDateTime endTime;

    public EmployeeShift(String name, String positionId, LocalDateTime startTime, LocalDateTime endTime) {
        this.name = name;
        this.positionId = positionId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}

public class intern_task {

    public static void main(String[] args) {
        String file_path = "/Users/nihalmahansaria/Downloads/Assignment_Timecard.xlsx - Sheet1.csv";  // Replace with the actual file path
        List<EmployeeShift> shifts = readCsv(file_path);

        Set<String> consecutive7Days = new HashSet<>();
        Set<String> lessThan10Hours = new HashSet<>();
        Set<String> moreThan14Hours = new HashSet<>();

        Map<String, LocalDateTime> lastShiftEndTimes = new HashMap<>();
        Map<String, Integer> consecutiveDaysCount = new HashMap<>();
        LocalDateTime prevDay = null;

        for (EmployeeShift shift : shifts) {
            String employeeDetails = shift.name + " - " + shift.positionId; // Concatenate the name and position ID
        
            // Check for more than 14 hours shift
            if (shift.endTime != null && shift.startTime != null && shift.endTime.minusHours(14).isAfter(shift.startTime)) {
                moreThan14Hours.add(employeeDetails);
            }

            // Check for consecutive days
            if (prevDay != null && shift.startTime != null && shift.startTime.toLocalDate().minusDays(1).isEqual(prevDay.toLocalDate())) {
                consecutiveDaysCount.put(shift.name, consecutiveDaysCount.getOrDefault(shift.name, 1) + 1);
                if (consecutiveDaysCount.get(shift.name) >= 7) {
                    consecutive7Days.add(shift.name + " - " + shift.positionId);
                    consecutiveDaysCount.put(shift.name, 0);
                }
            } else {
                consecutiveDaysCount.put(shift.name, 1);
            }
            prevDay = shift.startTime;

            // Check for less than 10 but more than 1 hour between shifts
            LocalDateTime lastShiftEnd = lastShiftEndTimes.get(shift.name);
            if (lastShiftEnd != null && shift.startTime != null && shift.startTime.minusHours(10).isBefore(lastShiftEnd) && shift.startTime.minusHours(1).isAfter(lastShiftEnd)) {
                lessThan10Hours.add(shift.name + " - " + shift.positionId);
            }

            if (shift.endTime != null) {
                lastShiftEndTimes.put(shift.name, shift.endTime);
            }
        }

        // Print results
        printResults("Employees with 7 consecutive days:", consecutive7Days);
        printResults("Employees with less than 10 hours but more than 1 hour between shifts:", lessThan10Hours);
        printResults("Employees with shifts longer than 14 hours:", moreThan14Hours);
    }

    private static List<EmployeeShift> readCsv(String filePath) {
        List<EmployeeShift> shifts = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { // Skip the header row
                    firstLine = false;
                    continue;
                }
                String[] values = line.split(",");
                // Ensure the row has enough columns
                if (values.length >= 4 && !values[2].trim().isEmpty() && !values[3].trim().isEmpty()) {
                    try {
                        String name = values[0].trim();
                        String positionId = values[1].trim();
                        LocalDateTime startTime = LocalDateTime.parse(values[2].trim(), DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"));
                        LocalDateTime endTime = LocalDateTime.parse(values[3].trim(), DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"));
                        shifts.add(new EmployeeShift(name, positionId, startTime, endTime));
                    } catch (DateTimeParseException e) {
                        // Handle rows with invalid date-time format
                        System.out.println("Skipping row with invalid date-time format: " + Arrays.toString(values));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return shifts;
    }

    private static void printResults(String message, Set<String> results) {
        System.out.println(message);
        if (results.isEmpty()) {
            System.out.println("None");
        } else {
            for (String result : results) {
                System.out.println(result);
            }
        }
    }
}
