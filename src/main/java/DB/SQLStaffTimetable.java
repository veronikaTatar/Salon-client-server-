package DB;

import Salon.EmployeeWorkTime;
import Salon.StaffTimetable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SQLStaffTimetable implements ISQLStaffTimetable{
    private static SQLStaffTimetable instance;
    private ConnectionDB dbConnection;

    private SQLStaffTimetable() throws SQLException, ClassNotFoundException {
        dbConnection = ConnectionDB.getInstance();
    }

    public static synchronized SQLStaffTimetable getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null) {
            instance = new SQLStaffTimetable();
        }
        return instance;
    }


    public boolean addTimetable(StaffTimetable obj) throws SQLException {
        System.out.println("Добавляем в график работы");
        // Вставка в таблицу timetable
        String sql = "INSERT INTO timetable (lengthOfTime, order_idOrder) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = ConnectionDB.dbConnection.prepareStatement(sql)) {
            preparedStatement.setInt(1, obj.getLengthOfTime()); // Установка времени
            preparedStatement.setInt(2, obj.getIdOrder()); // ID заказа

            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении в расписание: " + e.getMessage());
            return false;
        }
    }


    public boolean deleteTimetable(int orderId) throws SQLException {
        System.out.println("Удаляем из графика работы (только timetable) полученный id "+ orderId);
        String deleteTimetableSql = "DELETE FROM timetable WHERE order_idOrder = ?";

        try (PreparedStatement deleteTimetableStmt = ConnectionDB.dbConnection.prepareStatement(deleteTimetableSql)) {
            deleteTimetableStmt.setInt(1, orderId);
            int affectedRows = deleteTimetableStmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении из timetable: " + e.getMessage());
            throw e;
        }
    }

    public List<StaffTimetable> getSchedule() throws SQLException {
        List<StaffTimetable> scheduleList = new ArrayList<>();
        String query = "SELECT o.idOrder, o.confirmation, " +
                "s.name AS serviceName, s.type AS serviceType, e.name AS employeeName, " +
                "e.surname AS employeeSurname, " +
                "s.time AS serviceTime, c.user_idUser, " +
                "MAX(t.lengthOfTime) AS lengthOfTime " +
                "FROM `order` o " +
                "JOIN recordingservice rs ON o.recordingservice_idRecordingService = rs.idRecordingService " +
                "JOIN service s ON rs.service_idService = s.idService " +
                "JOIN employee e ON rs.employee_idEmployee = e.idEmployee " +
                "JOIN consumer c ON o.consumer_idClient = c.idClient " +
                "JOIN timetable t ON o.idOrder = t.order_idOrder " +  // <- Добавлен пробел здесь
                "GROUP BY o.idOrder, o.confirmation, s.name, s.type, e.name, e.surname, s.time, c.user_idUser";
        // Форматтер для даты (без времени)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        // Форматтер для времени (если нужно отдельно)
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                StaffTimetable schedule = new StaffTimetable();
                schedule.setId(rs.getInt("idOrder"));
                schedule.setIdOrder(rs.getInt("idOrder"));
                schedule.setNameService(rs.getString("serviceName"));
                schedule.setNameEmployee(rs.getString("employeeName"));
                schedule.setLastnameEmployee(rs.getString("employeeSurname"));

                // Обработка даты и времени
                Timestamp timestamp = rs.getTimestamp("serviceTime");
                if (timestamp != null) {
                    LocalDateTime serviceDateTime = timestamp.toLocalDateTime();

                    // Форматируем дату в нужный формат
                    String formattedDate = serviceDateTime.format(dateFormatter);
                    schedule.setDate(formattedDate); // Будет в формате "dd.MM.yyyy"

                    // Если нужно время отдельно
                    String formattedTime = serviceDateTime.format(timeFormatter);
                    schedule.setTime(formattedTime); // Будет в формате "HH:mm"
                }

                schedule.setLengthOfTime(rs.getInt("lengthOfTime"));
                scheduleList.add(schedule);
            }
        }
        return scheduleList;
    }

    public List<StaffTimetable> getScheduleByEmployeeId(int userId) throws SQLException {
        List<StaffTimetable> scheduleList = new ArrayList<>();
        System.out.println("Получаем id ");

        // Получаем idEmployee по userId
        String employeeQuery = "SELECT idEmployee FROM employee WHERE user_idUser = ?";
        int employeeId = -1;

        try (PreparedStatement employeeStmt = ConnectionDB.dbConnection.prepareStatement(employeeQuery)) {
            employeeStmt.setInt(1, userId);
            try (ResultSet employeeRs = employeeStmt.executeQuery()) {
                if (employeeRs.next()) {
                    employeeId = employeeRs.getInt("idEmployee");
                }
            }
        }

        if (employeeId == -1) {
            return scheduleList;
        }

        // Упрощённый SQL-запрос
        String query = "SELECT o.idOrder, s.name AS serviceName, e.name AS employeeName, " +
                "e.surname AS employeeSurname, t.lengthOfTime, s.time " +
                "FROM `order` o " +
                "JOIN recordingservice rs ON o.recordingservice_idRecordingService = rs.idRecordingService " +
                "JOIN service s ON rs.service_idService = s.idService " +
                "JOIN employee e ON rs.employee_idEmployee = e.idEmployee " +
                "JOIN timetable t ON o.idOrder = t.order_idOrder " +
                "WHERE e.idEmployee = ?";

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(query)) {
            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StaffTimetable schedule = new StaffTimetable();
                    schedule.setId(rs.getInt("idOrder"));
                    schedule.setIdOrder(rs.getInt("idOrder"));
                    schedule.setNameService(rs.getString("serviceName"));
                    schedule.setNameEmployee(rs.getString("employeeName"));
                    schedule.setLastnameEmployee(rs.getString("employeeSurname"));
                    schedule.setLengthOfTime(rs.getInt("lengthOfTime")); // Получаем значение lengthOfTime

                    Timestamp timestamp = rs.getTimestamp("time");
                    if (timestamp != null) {
                        LocalDateTime serviceDateTime = timestamp.toLocalDateTime();
                        schedule.setDate(serviceDateTime.format(dateFormatter));
                        schedule.setTime(serviceDateTime.format(timeFormatter));
                    }

                    scheduleList.add(schedule);
                }
            }
        }
        return scheduleList;
    }



    //ВИЗУАЛИЗАЦИЯ
    public List<EmployeeWorkTime> getDataDiagram(StaffTimetable filtr) {
        List<EmployeeWorkTime> dataFiltr = new ArrayList<>();
        try {
            String sql;
            String[] parts = filtr.getDate() != null ? filtr.getDate().split("-") : null;
            String year = parts != null ? parts[0] : null;
            String month = parts != null ? parts[1] : null;

            if (month != null && Integer.parseInt(month) != 0) {
                sql = "SELECT e.name AS employeeName, e.patronymic AS employeePatronymic, e.surname AS employeeSurname, " +
                        "SUM(t.lengthOfTime) AS totalMinutes " +
                        "FROM timetable t " +
                        "JOIN `order` o ON t.order_idOrder = o.idOrder " +
                        "JOIN recordingservice rs ON o.recordingservice_idRecordingService = rs.idRecordingService " +
                        "JOIN service s ON rs.service_idService = s.idService " +
                        "JOIN employee e ON rs.employee_idEmployee = e.idEmployee " +
                        "WHERE YEAR(s.time) = ? AND MONTH(s.time) = ? " +
                        "GROUP BY e.idEmployee";

                try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(year));
                    stmt.setInt(2, Integer.parseInt(month));
                    ResultSet resultSet = stmt.executeQuery();

                    while (resultSet.next()) {
                        EmployeeWorkTime entry = new EmployeeWorkTime();
                        String fio = resultSet.getString("employeeSurname") + " " +
                                resultSet.getString("employeeName").charAt(0) + ". " +
                                resultSet.getString("employeePatronymic").charAt(0) + ".";
                        entry.setFio(fio);
                        entry.setTotalHours(resultSet.getInt("totalMinutes") / 60.0);
                        dataFiltr.add(entry);
                    }
                }
            } else {
                sql = "SELECT e.name AS employeeName, e.patronymic AS employeePatronymic, e.surname AS employeeSurname, " +
                        "SUM(t.lengthOfTime) AS totalMinutes " +
                        "FROM timetable t " +
                        "JOIN `order` o ON t.order_idOrder = o.idOrder " +
                        "JOIN recordingservice rs ON o.recordingservice_idRecordingService = rs.idRecordingService " +
                        "JOIN service s ON rs.service_idService = s.idService " +
                        "JOIN employee e ON rs.employee_idEmployee = e.idEmployee " +
                        "WHERE YEAR(s.time) = ? " +
                        "GROUP BY e.idEmployee";
                try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(year)); // Здесь используем year вместо idOrder
                    ResultSet resultSet = stmt.executeQuery();
                    while (resultSet.next()) {
                        EmployeeWorkTime entry = new EmployeeWorkTime();
                        String fio = resultSet.getString("employeeSurname") + " " +
                                resultSet.getString("employeeName").charAt(0) + ". " +
                                resultSet.getString("employeePatronymic").charAt(0) + ".";
                        entry.setFio(fio);
                        entry.setTotalHours(resultSet.getInt("totalMinutes") / 60.0);
                        dataFiltr.add(entry);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataFiltr;
    }
    /*ЭКСПОРТ*/
    public boolean getDataList(StaffTimetable data) throws SQLException {
        String[] parts = data.getDate() != null ? data.getDate().split("-") : null;
        String year = parts != null ? parts[0] : null;
        String month = parts != null && parts.length > 1 ? parts[1] : null;
        String fileName = data.getNameService(); // Используем имя сервиса для имени файла

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Запись заголовков
            writer.write("Фамилия,Имя,Отчество,Должность,Зарплата,Общее количество часов\n");

            List<EmployeeWorkList> statistics = getDataForList(data); // Получаем данные для записи в CSV
            for (EmployeeWorkList entry : statistics) {
                writer.write(entry.getSurname() + "," +
                        entry.getName() + "," +
                        entry.getPatronymic() + "," +
                        entry.getPosition() + "," +
                        entry.getSalary() + "," +
                        (entry.getTotalMinutes() / 60.0) + "\n"); // Конвертация минут в часы
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<EmployeeWorkList> getDataForList(StaffTimetable filtr) {
        List<EmployeeWorkList> dataFiltr = new ArrayList<>();

        try {
            String sql;
            String[] parts = filtr.getDate() != null ? filtr.getDate().split("-") : null;
            String year = parts != null ? parts[0] : null;
            String month = parts != null && parts.length > 1 ? parts[1] : null;

            if (month != null && Integer.parseInt(month) != 0) {
                sql = "SELECT e.name AS employeeName, e.patronymic AS employeePatronymic, e.surname AS employeeSurname, " +
                        "e.position AS employeePosition, e.salary AS employeeSalary, " + // Добавлено поле должности и зарплаты
                        "SUM(t.lengthOfTime) AS totalMinutes " +
                        "FROM timetable t " +
                        "JOIN `order` o ON t.order_idOrder = o.idOrder " +
                        "JOIN recordingservice rs ON o.recordingservice_idRecordingService = rs.idRecordingService " +
                        "JOIN service s ON rs.service_idService = s.idService " +
                        "JOIN employee e ON rs.employee_idEmployee = e.idEmployee " +
                        "WHERE YEAR(s.time) = ? AND MONTH(s.time) = ? " +
                        "GROUP BY e.idEmployee";

                try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(year));
                    stmt.setInt(2, Integer.parseInt(month));
                    ResultSet resultSet = stmt.executeQuery();

                    while (resultSet.next()) {
                        EmployeeWorkList entry = new EmployeeWorkList();
                        entry.setSurname(resultSet.getString("employeeSurname"));
                        entry.setName(resultSet.getString("employeeName"));
                        entry.setPatronymic(resultSet.getString("employeePatronymic"));
                        entry.setPosition(resultSet.getString("employeePosition"));
                        entry.setSalary(resultSet.getDouble("employeeSalary"));
                        entry.setTotalMinutes(resultSet.getInt("totalMinutes"));
                        dataFiltr.add(entry);
                    }
                }
            } else {
                sql = "SELECT e.name AS employeeName, e.patronymic AS employeePatronymic, e.surname AS employeeSurname, " +
                        "e.position AS employeePosition, e.salary AS employeeSalary, " + // Добавлено поле должности и зарплаты
                        "SUM(t.lengthOfTime) AS totalMinutes " +
                        "FROM timetable t " +
                        "JOIN `order` o ON t.order_idOrder = o.idOrder " +
                        "JOIN recordingservice rs ON o.recordingservice_idRecordingService = rs.idRecordingService " +
                        "JOIN service s ON rs.service_idService = s.idService " +
                        "JOIN employee e ON rs.employee_idEmployee = e.idEmployee " +
                        "WHERE YEAR(s.time) = ? " +
                        "GROUP BY e.idEmployee";

                try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(sql)) {
                    stmt.setInt(1, Integer.parseInt(year)); // Здесь используем year
                    ResultSet resultSet = stmt.executeQuery();

                    while (resultSet.next()) {
                        EmployeeWorkList entry = new EmployeeWorkList();
                        entry.setSurname(resultSet.getString("employeeSurname"));
                        entry.setName(resultSet.getString("employeeName"));
                        entry.setPatronymic(resultSet.getString("employeePatronymic"));
                        entry.setPosition(resultSet.getString("employeePosition"));
                        entry.setSalary(resultSet.getDouble("employeeSalary"));
                        entry.setTotalMinutes(resultSet.getInt("totalMinutes"));
                        dataFiltr.add(entry);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataFiltr;
    }


    class EmployeeWorkList {
        private String surname;
        private String name;
        private String patronymic;
        private String position;  // Должность (например, косметолог, маникюрша и т.д.)
        private double salary;

        private double totalMinutes;

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPatronymic() {
            return patronymic;
        }

        public void setPatronymic(String patronymic) {
            this.patronymic = patronymic;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public double getSalary() {
            return salary;
        }

        public void setSalary(double salary) {
            this.salary = salary;
        }

        public double getTotalMinutes() {
            return totalMinutes;
        }

        public void setTotalMinutes(double totalMinutes) {
            this.totalMinutes = totalMinutes;
        }
    }
}

