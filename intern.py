import pandas as pd

def read_file(file_path):
    return pd.read_csv(file_path)

def parse_datetime(time_str):
    # Assumption: The datetime format in the CSV is consistent.
    return pd.to_datetime(time_str, format='%m/%d/%Y %I:%M %p')

def analyze_shifts(data):
    # Sorting by Employee Name and Time
    data.sort_values(by=['Employee Name', 'Time'], inplace=True)

    # Initialising dictionaries to store results
    consecutive_7_days = set()
    less_than_10_hours = set()
    more_than_14_hours = set()

    # Iterating through each row in the DataFrame
    for name, group in data.groupby('Employee Name'):
        prev_shift_end = None
        consecutive_days = 0
        prev_day = None

        for _, row in group.iterrows():
            # Parse the Time and Time Out strings to datetime
            shift_start = parse_datetime(row['Time'])
            shift_end = parse_datetime(row['Time Out'])

            # Calculate shift length
            shift_length = shift_end - shift_start

            # Check for shifts longer than 14 hours
            if shift_length.total_seconds() > 14 * 3600:
                more_than_14_hours.add((row['Employee Name'], row['Position ID']))

            # Check for consecutive days
            if prev_day is not None:
                if (shift_start - prev_day).days == 1:
                    consecutive_days += 1
                else:
                    consecutive_days = 1
            prev_day = shift_start

            if consecutive_days >= 7:
                consecutive_7_days.add((row['Employee Name'], row['Position ID']))

            # Check for time between shifts
            if prev_shift_end is not None:
                time_between_shifts = shift_start - prev_shift_end
                if 1 * 3600 < time_between_shifts.total_seconds() < 10 * 3600:
                    less_than_10_hours.add((row['Employee Name'], row['Position ID']))

            prev_shift_end = shift_end

    return consecutive_7_days, less_than_10_hours, more_than_14_hours

def main():
    file_path = '/Users/nihalmahansaria/Downloads/Assignment_Timecard.xlsx - Sheet1.csv'  # Replace 'path_to_your_file.csv' with the actual file path
    data = read_file(file_path)
    consecutive_7_days, less_than_10_hours, more_than_14_hours = analyze_shifts(data)

    # Print the results
    print("Employees with 7 consecutive days:")
    for employee, position in consecutive_7_days:
        print(f"{employee} - {position}")

    print("\nEmployees with less than 10 hours but more than 1 hour between shifts:")
    for employee, position in less_than_10_hours:
        print(f"{employee} - {position}")

    print("\nEmployees with shifts longer than 14 hours:")
    for employee, position in more_than_14_hours:
        print(f"{employee} - {position}")

if __name__ == "__main__":
    main()
