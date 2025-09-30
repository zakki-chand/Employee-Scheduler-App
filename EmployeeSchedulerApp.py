import tkinter as tk
from tkinter import messagebox
from tkinter import ttk
import random

class EmployeeSchedulerApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Employee Shift Scheduler")
        self.root.geometry("800x600")
        
        # Constants for shift types and days of the week
        self.shift_types = ["Morning", "Afternoon", "Evening"]
        self.days_of_week = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        
        # Data structures to store employee shifts and work count
        self.employee_shifts = {}
        self.employee_work_count = {}
        self.weekly_schedule = {day: {shift: [] for shift in self.shift_types} for day in self.days_of_week}
        
        self.num_employees = 0
        self.create_widgets()

    def create_widgets(self):
        # Frame for the number of employees input
        self.frame_input = tk.Frame(self.root)
        self.frame_input.pack(pady=20)
        
        self.label_num_employees = tk.Label(self.frame_input, text="Enter number of employees:")
        self.label_num_employees.grid(row=0, column=0, padx=10)
        
        self.entry_num_employees = tk.Entry(self.frame_input)
        self.entry_num_employees.grid(row=0, column=1)
        
        self.submit_button = tk.Button(self.frame_input, text="Submit", command=self.submit_employee_count)
        self.submit_button.grid(row=0, column=2, padx=10)

        # Frame for employee data input (this will be dynamically created later)
        self.frame_employee_data = tk.Frame(self.root)
        self.frame_employee_data.pack(pady=20)

        # Frame for showing the final schedule (this will be dynamically created later)
        self.frame_schedule = tk.Frame(self.root)
        self.frame_schedule.pack(pady=20)

    def submit_employee_count(self):
        try:
            self.num_employees = int(self.entry_num_employees.get())
            if self.num_employees <= 0:
                raise ValueError
        except ValueError:
            messagebox.showerror("Invalid Input", "Please enter a valid number of employees.")
            return
        
        # Clear previous data and create new employee input fields
        self.employee_shifts.clear()
        self.employee_work_count.clear()
        self.clear_frame(self.frame_employee_data)
        self.create_employee_input_fields()

    def create_employee_input_fields(self):
        # Dynamically create input fields for employee names and shift preferences
        self.employee_input_fields = []
        for i in range(self.num_employees):
            frame_employee = tk.Frame(self.frame_employee_data)
            frame_employee.pack(pady=10, fill=tk.X)
            
            label_name = tk.Label(frame_employee, text=f"Employee {i+1} Name:")
            label_name.grid(row=0, column=0, padx=10)
            entry_name = tk.Entry(frame_employee)
            entry_name.grid(row=0, column=1)
            
            shift_combos = []
            for day in self.days_of_week:
                label_day = tk.Label(frame_employee, text=day)
                label_day.grid(row=1, column=self.days_of_week.index(day), padx=10)
                shift_combo = ttk.Combobox(frame_employee, values=self.shift_types)
                shift_combo.set(self.shift_types[0])  # Default value
                shift_combo.grid(row=2, column=self.days_of_week.index(day))
                shift_combos.append(shift_combo)
            
            self.employee_input_fields.append((entry_name, shift_combos))

        submit_button = tk.Button(self.frame_employee_data, text="Submit Employee Data", command=self.submit_employee_data)
        submit_button.pack(pady=10)

    def submit_employee_data(self):
        # Collect employee data and shift preferences
        self.employee_shifts.clear()
        self.employee_work_count.clear()
        valid = True

        for i, (entry_name, shift_combos) in enumerate(self.employee_input_fields):
            employee_name = entry_name.get().strip()
            if not employee_name:
                messagebox.showerror("Invalid Input", f"Please enter a name for Employee {i + 1}.")
                valid = False
                break

            shifts = [combo.get() for combo in shift_combos]
            self.employee_shifts[employee_name] = shifts
            self.employee_work_count[employee_name] = 0

        if valid:
            self.assign_shifts()
            self.show_schedule()

    def assign_shifts(self):
        # Reset the weekly schedule
        self.weekly_schedule = {day: {shift: [] for shift in self.shift_types} for day in self.days_of_week}

        # Assign shifts based on employee preferences
        for day_index, day in enumerate(self.days_of_week):
            shift_counts = {shift: 0 for shift in self.shift_types}

            for employee_name, shifts in self.employee_shifts.items():
                if self.employee_work_count[employee_name] < 5:
                    preferred_shift = shifts[day_index]
                    if shift_counts[preferred_shift] < 2:
                        self.weekly_schedule[day][preferred_shift].append(employee_name)
                        self.employee_work_count[employee_name] += 1
                        shift_counts[preferred_shift] += 1

            # Ensure each shift has at least 2 employees
            for shift in self.shift_types:
                while len(self.weekly_schedule[day][shift]) < 2:
                    for employee_name, shifts in self.employee_shifts.items():
                        if self.employee_work_count[employee_name] < 5:
                            self.weekly_schedule[day][shift].append(employee_name)
                            self.employee_work_count[employee_name] += 1
                            shift_counts[shift] += 1
                            break

    def show_schedule(self):
        # Clear previous schedule output
        self.clear_frame(self.frame_schedule)

        # Create a scrollable canvas for displaying the schedule
        canvas = tk.Canvas(self.frame_schedule)
        scroll_y = tk.Scrollbar(self.frame_schedule, orient="vertical", command=canvas.yview)
        canvas.configure(yscrollcommand=scroll_y.set)

        frame_schedule_display = tk.Frame(canvas)

        # Display the schedule
        for day in self.days_of_week:
            day_label = tk.Label(frame_schedule_display, text=day, font=('Arial', 12, 'bold'))
            day_label.grid(row=self.days_of_week.index(day), column=0, sticky="w", padx=10, pady=5)
            row = 1
            for shift in self.shift_types:
                shift_label = tk.Label(frame_schedule_display, text=f"{shift}:", font=('Arial', 10))
                shift_label.grid(row=row, column=0, sticky="w", padx=20)
                employees = ', '.join(self.weekly_schedule[day][shift])
                employees_label = tk.Label(frame_schedule_display, text=employees, font=('Arial', 10))
                employees_label.grid(row=row, column=1, sticky="w", padx=20)
                row += 1

        # Add scroll functionality
        canvas.create_window((0, 0), window=frame_schedule_display, anchor="nw")
        frame_schedule_display.update_idletasks()
        canvas.config(scrollregion=canvas.bbox("all"))

        # Pack everything into the frame
        canvas.pack(side="left", fill="both", expand=True)
        scroll_y.pack(side="right", fill="y")

    def clear_frame(self, frame):
        # Clear all widgets from a given frame
        for widget in frame.winfo_children():
            widget.destroy()


if __name__ == "__main__":
    root = tk.Tk()
    app = EmployeeSchedulerApp(root)
    root.mainloop()
