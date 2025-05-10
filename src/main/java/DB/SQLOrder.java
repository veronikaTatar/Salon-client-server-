package DB;

import Salon.Employee;
import Salon.Order;
import Salon.Role;
import Salon.StaffTimetable;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SQLOrder implements ISQLOrder {
    String query = null;
    public static PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    private static SQLOrder instance;
    private ConnectionDB dbConnection;

    private SQLOrder() throws SQLException, ClassNotFoundException {
        dbConnection = ConnectionDB.getInstance();
    }

    public static synchronized SQLOrder getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null) {
            instance = new SQLOrder();
        }
        return instance;
    }

    public Integer findClientIdByUserId(int idUser) throws SQLException {
        String query = "SELECT c.idClient " +
                "FROM consumer c " +
                "JOIN user u ON c.user_idUser = u.idUser " +
                "WHERE u.idUser = ?";

        try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(query)) {
            stmt.setInt(1, idUser); // Устанавливаем параметр ID пользователя

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idClient"); // Возвращаем ID клиента
                }
            }
        }
        return null; // Если клиент не найден, возвращаем null
    }

    public boolean addOrder(Order obj) throws SQLException {//ЗАПОЛН ТАБЛИЦЫ ДОП ПОЛЯ НЕ НУЖНЫ

        Integer idClient = findClientIdByUserId(obj.getIdUser());
        if (idClient == null) {
            System.out.println("Ошибка: Клиент не найден для пользователя с idUser " + obj.getIdUser());
            return false; // Если клиент не найден, возвращаем false
        }

        // Вставка заказа
        String sql = "INSERT INTO `order` (confirmation, recordingservice_idRecordingService, consumer_idClient) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = ConnectionDB.dbConnection.prepareStatement(sql)) {
            // Устанавливаем параметры запроса
            preparedStatement.setBoolean(1, obj.isConfirmation());
            preparedStatement.setInt(2, obj.getIdRecordingService());
            preparedStatement.setInt(3, idClient); // Используем найденный idClient

            // Выполняем вставку
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении данных: " + e.getMessage());
            return false; // Возвращаем false в случае ошибки
        }
    }


    // Метод для получения idUser на основе idClient
    public Integer findUserIdByClientId(int idClient) throws SQLException {
        String query = "SELECT u.idUser " +
                "FROM consumer c " +
                "JOIN user u ON c.user_idUser = u.idUser " +
                "WHERE c.idClient = ?";

        try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(query)) {
            stmt.setInt(1, idClient);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idUser");
                }
            }
        }
        return null; // Если пользователь не найден
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orderList = new ArrayList<>();
        String query = "SELECT o.idOrder, o.confirmation, " +
                "s.name AS serviceName, s.type AS serviceType, e.name AS employeeName, " +
                "s.time AS serviceTime, c.user_idUser " + // Получаем idUser клиента
                "FROM `order` o " +
                "JOIN recordingservice rs ON o.recordingservice_idRecordingService = rs.idRecordingService " +
                "JOIN service s ON rs.service_idService = s.idService " + // Присоединяем таблицу service
                "JOIN employee e ON rs.employee_idEmployee = e.idEmployee " + // Присоединяем таблицу employee
                "JOIN consumer c ON o.consumer_idClient = c.idClient"; // Присоединяем таблицу consumer для получения idUser

        try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("idOrder"));
                order.setConfirmation(rs.getBoolean("confirmation"));
                order.setName(rs.getString("serviceName")); // Название услуги
                order.setType(rs.getString("serviceType")); // Тип услуги
                order.setEmployeeName(rs.getString("employeeName")); // Имя мастера
                order.setTime(rs.getString("serviceTime")); // Время услуги
                order.setIdUser(rs.getInt("user_idUser")); // Устанавливаем idUser для заказа


                orderList.add(order);
            }
        }
        return orderList;
    }
    public List<Order> getOrdersByClientId(int userId) throws SQLException {
        List<Order> orderList = new ArrayList<>();

        // Изменяем запрос так, чтобы сначала получить idClient по userId
        String clientQuery = "SELECT idClient FROM consumer WHERE user_idUser = ?";
        int clientId = -1; // Инициализируем переменную для idClient

        try (PreparedStatement clientStmt = ConnectionDB.dbConnection.prepareStatement(clientQuery)) {
            clientStmt.setInt(1, userId); // Устанавливаем параметр для идентификатора пользователя
            try (ResultSet clientRs = clientStmt.executeQuery()) {
                if (clientRs.next()) {
                    clientId = clientRs.getInt("idClient"); // Получаем idClient
                }
            }
        }

        // Теперь используем clientId для получения заказов
        if (clientId != -1) { // Проверяем, что clientId был найден
            String query = "SELECT o.idOrder, o.confirmation, " +
                    "s.name AS serviceName, s.type AS serviceType, " +
                    "CONCAT(e.surname, ' ', e.name) AS employeeFullName, " + // Объединяем фамилию и имя
                    "s.time AS serviceTime " +
                    "FROM `order` o " +
                    "JOIN recordingservice rs ON o.recordingservice_idRecordingService = rs.idRecordingService " +
                    "JOIN service s ON rs.service_idService = s.idService " +
                    "JOIN employee e ON rs.employee_idEmployee = e.idEmployee " +
                    "WHERE o.consumer_idClient = ?"; // Фильтруем по idClient

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"); // Форматирование даты

            try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(query)) {
                stmt.setInt(1, clientId); // Устанавливаем параметр для идентификатора клиента
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Order order = new Order();
                        order.setId(rs.getInt("idOrder"));
                        order.setConfirmation(rs.getBoolean("confirmation"));
                        order.setName(rs.getString("serviceName")); // Название услуги
                        order.setType(rs.getString("serviceType")); // Тип услуги

                        // Получение и форматирование времени
                        LocalDateTime serviceTime = rs.getTimestamp("serviceTime").toLocalDateTime();
                        String formattedTime = serviceTime.format(formatter);
                        order.setTime(formattedTime); // Установить отформатированное время

                        // Устанавливаем полное имя сотрудника
                        order.setEmployeeName(rs.getString("employeeFullName")); // Фамилия и имя сотрудника

                        orderList.add(order);
                    }
                }
            }
        }
        return orderList;
    }


    public List<Order> getOrdersByEmployeeId(int userId) throws SQLException {
        List<Order> orderList = new ArrayList<>();

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

        if (employeeId != -1) {
            String query = "SELECT o.idOrder, o.confirmation, " +
                    "s.name AS serviceName, s.type AS serviceType, " +
                    "CONCAT(c.surname, ' ', c.name) AS clientFullName, " +
                    "s.time AS serviceTime " +
                    "FROM `order` o " +
                    "JOIN recordingservice rs ON o.recordingservice_idRecordingService = rs.idRecordingService " +
                    "JOIN service s ON rs.service_idService = s.idService " +
                    "JOIN consumer c ON o.consumer_idClient = c.idClient " +
                    "JOIN employee e ON rs.employee_idEmployee = e.idEmployee " +
                    "WHERE rs.employee_idEmployee = ?";

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(query)) {
                stmt.setInt(1, employeeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Order order = new Order();
                        order.setId(rs.getInt("idOrder"));
                        order.setConfirmation(rs.getBoolean("confirmation"));
                        order.setName(rs.getString("serviceName"));
                        order.setType(rs.getString("serviceType"));
                        order.setEmployeeName(rs.getString("clientFullName"));

                        // Форматирование времени
                        LocalDateTime serviceTime = rs.getTimestamp("serviceTime").toLocalDateTime();
                        String formattedTime = serviceTime.format(formatter);
                        order.setTime(formattedTime); // Установить отформатированное время

                        orderList.add(order);
                    }
                }
            }
        }
        return orderList;
    }

    public boolean changeOrderStatus(Order obj) {
        String updateQuery = "UPDATE `order` SET confirmation = ? WHERE idOrder = ?";
        String lengthTimeQuery = "SELECT lt.lengthTimeMin " +
                "FROM lengthtime lt " +
                "WHERE lt.service_idService = (SELECT rs.service_idService " +
                "FROM recordingservice rs " +
                "JOIN `order` o ON rs.idRecordingService = o.recordingservice_idRecordingService " +
                "WHERE o.idOrder = ?)";

        try (PreparedStatement updateStmt = ConnectionDB.dbConnection.prepareStatement(updateQuery)) {
            updateStmt.setBoolean(1, obj.isConfirmation());
            updateStmt.setInt(2, obj.getId());

            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected > 0) {
                if (obj.isConfirmation()) {
                    System.out.println("Статус true: добавл" +
                            "ем в расписание.");
                    return handleAddingToTimetable(obj, lengthTimeQuery);
                } else {
                    System.out.println("Статус false: удаляем из расписания.");
                    return handleDeletingFromTimetable(obj.getId());
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении статуса заявки: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean handleAddingToTimetable(Order obj, String lengthTimeQuery) {
        StaffTimetable timetableEntry = new StaffTimetable();
        try (PreparedStatement lengthTimeStmt = ConnectionDB.dbConnection.prepareStatement(lengthTimeQuery)) {
            lengthTimeStmt.setInt(1, obj.getId());
            ResultSet rs = lengthTimeStmt.executeQuery();

            if (rs.next()) {
                timetableEntry.setLengthOfTime(rs.getInt("lengthTimeMin"));
                timetableEntry.setIdOrder(obj.getId());
                boolean added = SQLStaffTimetable.getInstance().addTimetable(timetableEntry);
                if (!added) {
                    System.out.println("Ошибка при добавлении в расписание.");
                    return false;
                }
            } else {
                System.out.println("Ошибка: не удалось получить lengthOfTime для заказа " + obj.getId());
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении lengthTimeMin: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private boolean handleDeletingFromTimetable(int orderId) throws SQLException, ClassNotFoundException {
        boolean deleted = SQLStaffTimetable.getInstance().deleteTimetable(orderId);
        if (!deleted) {
            System.out.println("Ошибка при удалении из расписания.");
        }
        return deleted;
    }


    public boolean deleteOrder(Order obj) {
        String deleteOrderSql = "DELETE FROM `order` WHERE idOrder = ?";
        String deleteRecordingServiceSql = "DELETE FROM recordingservice WHERE idRecordingService = ?";
        String deleteTimetableSql = "DELETE FROM timetable WHERE order_idOrder = ?";

        try {
            // Удаляем записи из timetable
            try (PreparedStatement deleteTimetableStmt = ConnectionDB.dbConnection.prepareStatement(deleteTimetableSql)) {
                deleteTimetableStmt.setInt(1, obj.getId());
                deleteTimetableStmt.executeUpdate();
            }

            // Сначала удаляем связанные записи в recordingservice
            try (PreparedStatement deleteRecordingServiceStmt = ConnectionDB.dbConnection.prepareStatement(deleteRecordingServiceSql)) {
                deleteRecordingServiceStmt.setInt(1, obj.getIdRecordingService());
                deleteRecordingServiceStmt.executeUpdate();
            }

            // Затем удаляем сам заказ
            try (PreparedStatement deleteOrderStmt = ConnectionDB.dbConnection.prepareStatement(deleteOrderSql)) {
                deleteOrderStmt.setInt(1, obj.getId());
                deleteOrderStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Ошибка: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
