import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class EmployeeSchedulerGUI {

    // Constants for shift types and days of the week
    private static final String[] SHIFT_TYPES = {"Morning", "Afternoon", "Evening"};
    private static final String[] DAYS_OF_WEEK = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    // Maps for scheduling data
    private static Map<String, List<String>> employeeShifts = new HashMap<>();
    private static Map<String, Integer> employeeWorkCount = new HashMap<>();
    private static Map<String, Map<String, String>> weeklySchedule = new HashMap<>();

    public static void main(String[] args) {
        // Set up the main frame for the application
        JFrame frame = new JFrame("Employee Scheduler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);
        frame.setLocationRelativeTo(null);  // Center the window

        // Set the layout of the frame
        frame.setLayout(new BorderLayout());

        // Create the input panel for employee data
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(0, 2));

        JLabel labelNumEmployees = new JLabel("Enter the number of employees:");
        JTextField numEmployeesField = new JTextField(10);
        inputPanel.add(labelNumEmployees);
        inputPanel.add(numEmployeesField);

        JButton submitButton = new JButton("Submit");

        // List to dynamically generate employee input fields
        JPanel employeeInputPanel = new JPanel();
        employeeInputPanel.setLayout(new BoxLayout(employeeInputPanel, BoxLayout.Y_AXIS));

        // Add input panel and buttons to the frame
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(employeeInputPanel, BorderLayout.CENTER);
        frame.add(submitButton, BorderLayout.SOUTH);

        // Action for the submit button
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int numEmployees = Integer.parseInt(numEmployeesField.getText());
                    if (numEmployees <= 0) {
                        JOptionPane.showMessageDialog(frame, "Please enter a valid number of employees.");
                        return;
                    }
                    generateEmployeeInputs(numEmployees, employeeInputPanel, frame);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input for number of employees.");
                }
            }
        });

        frame.setVisible(true);
    }

    /**
     * This method generates the input fields for employee names and shift preferences.
     */
    private static void generateEmployeeInputs(int numEmployees, JPanel employeeInputPanel, JFrame frame) {
        // Clear the existing inputs
        employeeInputPanel.removeAll();
        employeeShifts.clear();  // Clear the previous employee shifts
        employeeWorkCount.clear();  // Clear the previous employee work count

        for (int i = 0; i < numEmployees; i++) {
            JPanel employeePanel = new JPanel();
            employeePanel.setLayout(new GridLayout(0, 2));
            JLabel employeeLabel = new JLabel("Employee " + (i + 1) + " Name:");
            JTextField employeeNameField = new JTextField();
            employeePanel.add(employeeLabel);
            employeePanel.add(employeeNameField);

            JPanel shiftPanel = new JPanel();
            shiftPanel.setLayout(new GridLayout(0, 3));
            JLabel shiftLabel = new JLabel("Shift Preferences (Morning, Afternoon, Evening):");
            JComboBox<String> mondayComboBox = new JComboBox<>(SHIFT_TYPES);
            JComboBox<String> tuesdayComboBox = new JComboBox<>(SHIFT_TYPES);
            JComboBox<String> wednesdayComboBox = new JComboBox<>(SHIFT_TYPES);
            JComboBox<String> thursdayComboBox = new JComboBox<>(SHIFT_TYPES);
            JComboBox<String> fridayComboBox = new JComboBox<>(SHIFT_TYPES);
            JComboBox<String> saturdayComboBox = new JComboBox<>(SHIFT_TYPES);
            JComboBox<String> sundayComboBox = new JComboBox<>(SHIFT_TYPES);

            shiftPanel.add(new JLabel("Monday:"));
            shiftPanel.add(mondayComboBox);
            shiftPanel.add(new JLabel("Tuesday:"));
            shiftPanel.add(tuesdayComboBox);
            shiftPanel.add(new JLabel("Wednesday:"));
            shiftPanel.add(wednesdayComboBox);
            shiftPanel.add(new JLabel("Thursday:"));
            shiftPanel.add(thursdayComboBox);
            shiftPanel.add(new JLabel("Friday:"));
            shiftPanel.add(fridayComboBox);
            shiftPanel.add(new JLabel("Saturday:"));
            shiftPanel.add(saturdayComboBox);
            shiftPanel.add(new JLabel("Sunday:"));
            shiftPanel.add(sundayComboBox);

            employeePanel.add(shiftPanel);
            employeeInputPanel.add(employeePanel);

            // Store the ComboBoxes for each employee
            employeeInputPanel.revalidate();
            employeeInputPanel.repaint();

            // When submitting the employee data
            JButton submitEmployeeButton = new JButton("Submit Employee Data");
            submitEmployeeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String employeeName = employeeNameField.getText();
                    if (employeeName.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Please enter a valid name for the employee.");
                        return;
                    }

                    // Collecting the shifts for each day
                    List<String> shifts = new ArrayList<>();
                    shifts.add((String) mondayComboBox.getSelectedItem());
                    shifts.add((String) tuesdayComboBox.getSelectedItem());
                    shifts.add((String) wednesdayComboBox.getSelectedItem());
                    shifts.add((String) thursdayComboBox.getSelectedItem());
                    shifts.add((String) fridayComboBox.getSelectedItem());
                    shifts.add((String) saturdayComboBox.getSelectedItem());
                    shifts.add((String) sundayComboBox.getSelectedItem());

                    employeeShifts.put(employeeName, shifts);
                    employeeWorkCount.put(employeeName, 0);  // Initialize work count

                    // When all employees' data has been collected, assign shifts and show the schedule
                    if (employeeShifts.size() == numEmployees) {
                        assignShifts();
                        showFinalSchedule(frame);
                    }
                }
            });
            employeeInputPanel.add(submitEmployeeButton);
        }
    }

    /**
     * Assign shifts to employees based on the given preferences and constraints.
     */
    private static void assignShifts() {
        // Initialize weekly schedule structure
        for (String day : DAYS_OF_WEEK) {
            weeklySchedule.put(day, new HashMap<>());
        }

        // Iterate over each day of the week and assign shifts
        for (String day : DAYS_OF_WEEK) {
            Map<String, Integer> shiftCounts = new HashMap<>();
            for (String shift : SHIFT_TYPES) {
                shiftCounts.put(shift, 0);
            }

            for (Map.Entry<String, List<String>> entry : employeeShifts.entrySet()) {
                String employeeName = entry.getKey();
                List<String> preferences = entry.getValue();

                if (employeeWorkCount.get(employeeName) < 5) {
                    String preferredShift = preferences.get(Arrays.asList(DAYS_OF_WEEK).indexOf(day));
                    if (shiftCounts.get(preferredShift) < 2) {
                        weeklySchedule.get(day).put(preferredShift + " " + employeeName, preferredShift);
                        employeeWorkCount.put(employeeName, employeeWorkCount.get(employeeName) + 1);
                        shiftCounts.put(preferredShift, shiftCounts.get(preferredShift) + 1);
                    }
                }
            }

            // Ensure each shift has at least two employees
            for (String shift : SHIFT_TYPES) {
                while (shiftCounts.get(shift) < 2) {
                    for (Map.Entry<String, List<String>> entry : employeeShifts.entrySet()) {
                        String employeeName = entry.getKey();
                        if (employeeWorkCount.get(employeeName) < 5) {
                            weeklySchedule.get(day).put(shift + " " + employeeName, shift);
                            employeeWorkCount.put(employeeName, employeeWorkCount.get(employeeName) + 1);
                            shiftCounts.put(shift, shiftCounts.get(shift) + 1);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Displays the final schedule in the GUI.
     */
    private static void showFinalSchedule(JFrame frame) {
        JPanel schedulePanel = new JPanel();
        schedulePanel.setLayout(new GridLayout(0, 1));

        // Display the final schedule in the panel
        for (String day : DAYS_OF_WEEK) {
            schedulePanel.add(new JLabel(day + ":"));
            Map<String, String> dailySchedule = weeklySchedule.get(day);
            for (Map.Entry<String, String> entry : dailySchedule.entrySet()) {
                schedulePanel.add(new JLabel(entry.getKey() + " assigned to " + entry.getValue() + " shift."));
            }
        }

        JScrollPane scrollPane = new JScrollPane(schedulePanel);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }
}
