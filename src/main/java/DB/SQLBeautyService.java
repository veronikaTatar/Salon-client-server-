package DB;
import Salon.BeautyService;
import Salon.Comment;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLBeautyService implements ISQLBeautyService {
    private static SQLBeautyService instance;
    private ConnectionDB dbConnection;
    private SQLBeautyService() throws SQLException, ClassNotFoundException {
        dbConnection = ConnectionDB.getInstance();
    }
    public static synchronized SQLBeautyService getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null) {
            instance = new SQLBeautyService();
        }
        return instance;
    }
    public boolean insert(BeautyService beautyService) {
        String proc = "{call InsertService3(?, ?, ?, ?, ?, ?)}";
        try (CallableStatement callableStatement = dbConnection.prepareCall(proc)) {
            // Установка параметров хранимой процедуры
            callableStatement.setTimestamp(1, Timestamp.valueOf(beautyService.getTime()));
            callableStatement.setInt(2, beautyService.getLengthOfTime());
            callableStatement.setInt(3, Integer.parseInt(beautyService.getEmployeeName()));
            callableStatement.setString(4, beautyService.getName());
            callableStatement.setDouble(5, beautyService.getPrice());
            callableStatement.setString(6, beautyService.getType());
            callableStatement.execute();
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Ошибки в данных: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean checkIfServiceExists(int serviceId) {
        String query = "SELECT COUNT(*) FROM recordingservice WHERE idRecordingService = ?";
        try (PreparedStatement preparedStatement = ConnectionDB.dbConnection.prepareStatement(query)) {
            preparedStatement.setInt(1, serviceId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean update(BeautyService beautyService) {
        int serviceId = beautyService.getId();
        boolean serviceExists = checkIfServiceExists(serviceId);
        if (!serviceExists) {
            System.out.println("Услуга с ID " + serviceId + " не существует.");
            return false;
        }
        String proc = "{call UpdateService3(?,?,?,?,?,?,?)}";
        try (CallableStatement callableStatement = ConnectionDB.dbConnection.prepareCall(proc)) {
            // Установка параметров хранимой процедуры
            callableStatement.setInt(1, beautyService.getId());
            callableStatement.setTimestamp(2, Timestamp.valueOf(beautyService.getTime()));
            callableStatement.setInt(3, beautyService.getLengthOfTime());
            callableStatement.setInt(4, Integer.parseInt(beautyService.getEmployeeName()));
            callableStatement.setString(5, beautyService.getName());
            callableStatement.setDouble(6, beautyService.getPrice());
            callableStatement.setString(7, beautyService.getType());
            callableStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public List<BeautyService> getAllBeautyServices() throws SQLException {
        List<BeautyService> beautyServices = new ArrayList<>();
        String query = "SELECT rs.idRecordingService, s.name, s.price, s.type, s.time, " +
                "CONCAT(e.surname, ' ', e.name) AS employeeFullName " +
                "FROM recordingservice rs " +
                "JOIN service s ON rs.service_idService = s.idService " +
                "JOIN employee e ON rs.employee_idEmployee = e.idEmployee " +
                "WHERE rs.idRecordingService NOT IN (" +
                "SELECT recordingservice_idRecordingService FROM `order` WHERE confirmation = false);";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        try (PreparedStatement stmt = dbConnection.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                BeautyService beautyService = new BeautyService();
                beautyService.setId(resultSet.getInt("idRecordingService"));
                beautyService.setName(resultSet.getString("name"));
                beautyService.setPrice(resultSet.getDouble("price"));
                beautyService.setType(resultSet.getString("type"));
                LocalDateTime time = resultSet.getTimestamp("time").toLocalDateTime();
                String formattedTime = time.format(formatter);
                beautyService.setTime(formattedTime);
                beautyService.setEmployeeName(resultSet.getString("employeeFullName"));
                beautyServices.add(beautyService);
            }
        }
        return beautyServices;
    }
    public List<Comment> getAllComments() throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT c.idComment AS id, c.mark, c.allMarks, " +
                "s.idService, s.name, s.type " +
                "FROM comment c " +
                "JOIN service s ON c.service_idService = s.idService";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                Comment comment = new Comment();
                comment.setId(resultSet.getInt("id"));
                comment.setIdService(resultSet.getInt("idService"));
                comment.setName(resultSet.getString("name"));
                comment.setType(resultSet.getString("type"));
                comment.setResult_mark(resultSet.getDouble("mark"));
                comment.setMarkHistory(resultSet.getString("allMarks"));
                comments.add(comment);
            }
        }
        return comments;
    }

    private boolean checkIfCommentExists(int commentId) {
        String query = "SELECT COUNT(*) FROM comment WHERE idComment = ?";
        try (PreparedStatement preparedStatement = ConnectionDB.dbConnection.prepareStatement(query)) {
            preparedStatement.setInt(1, commentId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Возвращаем true, если запись существует
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateComment(Comment comment) {
        if (!checkIfCommentExists(comment.getId())) {
            System.out.println("Комментарий с ID " + comment.getId() + " не существует.");
            return false;
        }
        String query = "UPDATE comment SET mark = ?, allMarks = ? WHERE idComment = ?";
        try (PreparedStatement preparedStatement = ConnectionDB.dbConnection.prepareStatement(query)) {
            preparedStatement.setDouble(1, comment.getResult_mark()); // Устанавливаем новую оценку
            preparedStatement.setString(2, comment.getMarkHistory()); // История оценок
            preparedStatement.setInt(3, comment.getId()); // ID комментария
            int rowsAffected = preparedStatement.executeUpdate(); // Выполнение запроса
            return rowsAffected > 0; // Возвращаем true, если обновление прошло успешно
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteServiceRecord(BeautyService obj) {
        int recordingServiceId = obj.getId();
        System.out.println("Начинаем удаление записи из recordingservice с ID: " + recordingServiceId);
        String deleteTimetableSql = "DELETE FROM timetable WHERE order_idOrder IN (SELECT idOrder FROM `order` WHERE recordingservice_idRecordingService = ?)";
        String deleteOrderSql = "DELETE FROM `order` WHERE recordingservice_idRecordingService = ?";
        String deleteCommentSql = "DELETE FROM comment WHERE service_idService IN (SELECT service_idService FROM recordingservice WHERE idRecordingService = ?)";
        String deleteLengthTimeSql = "DELETE FROM lengthtime WHERE service_idService IN (SELECT service_idService FROM recordingservice WHERE idRecordingService = ?)";
        String deleteRecordingServiceSql = "DELETE FROM recordingservice WHERE idRecordingService = ?";
        String getServiceIdSql = "SELECT service_idService FROM recordingservice WHERE idRecordingService = ?";
        String deleteServiceSql = "DELETE FROM service WHERE idService = ?";
        String countRecordingsForServiceSql = "SELECT COUNT(*) FROM recordingservice WHERE service_idService = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        int serviceId = -1;
        try {
            try {
                ps = ConnectionDB.dbConnection.prepareStatement(getServiceIdSql);
                ps.setInt(1, recordingServiceId);
                System.out.println("SQL (getServiceId): " + getServiceIdSql + ", recordingServiceId: " + recordingServiceId);
                rs = ps.executeQuery();
                if (rs.next()) {
                    serviceId = rs.getInt("service_idService");
                    System.out.println("Найден serviceId: " + serviceId);
                } else {
                    System.err.println("Ошибка: Не найден service_idService для idRecordingService: " + recordingServiceId);
                    return false;
                }
            } catch (SQLException e) {
                System.err.println("Ошибка при получении service_idService: " + e.getMessage());
                return false;
            } finally {closeResultSet(rs);closeStatement(ps);}
            try {
                ps = ConnectionDB.dbConnection.prepareStatement(deleteTimetableSql);
                ps.setInt(1, recordingServiceId);
                System.out.println("SQL (timetable): " + deleteTimetableSql + ", recordingServiceId: " + recordingServiceId);
                int rowsAffected = ps.executeUpdate();
                System.out.println("Удалено записей из timetable: " + rowsAffected);
            } catch (SQLException e) {
                System.err.println("Ошибка при удалении из timetable: " + e.getMessage());
                return false;
            } finally {closeStatement(ps);}
            try {
                ps = ConnectionDB.dbConnection.prepareStatement(deleteOrderSql);
                ps.setInt(1, recordingServiceId);
                System.out.println("SQL (order): " + deleteOrderSql + ", recordingServiceId: " + recordingServiceId);
                int rowsAffected = ps.executeUpdate();
                System.out.println("Удалено заказов: " + rowsAffected);
            } catch (SQLException e) {
                System.err.println("Ошибка при удалении из order: " + e.getMessage());
                return false;
            } finally {closeStatement(ps);}
            try {
                ps = ConnectionDB.dbConnection.prepareStatement(deleteCommentSql);
                ps.setInt(1, recordingServiceId);
                System.out.println("SQL (comment): " + deleteCommentSql + ", recordingServiceId: " + recordingServiceId);
                int rowsAffected = ps.executeUpdate();
                System.out.println("Удалено комментариев: " + rowsAffected);
            } catch (SQLException e) {
                System.err.println("Ошибка при удалении из comment: " + e.getMessage());
                return false;
            } finally {closeStatement(ps);}
            try {
                ps = ConnectionDB.dbConnection.prepareStatement(deleteLengthTimeSql);
                ps.setInt(1, recordingServiceId);
                System.out.println("SQL (lengthtime): " + deleteLengthTimeSql + ", recordingServiceId: " + recordingServiceId);
                int rowsAffected = ps.executeUpdate();
                System.out.println("Удалено записей из lengthtime: " + rowsAffected);
            } catch (SQLException e) {
                System.err.println("Ошибка при удалении из lengthtime: " + e.getMessage());
                return false;
            } finally {closeStatement(ps);}
            try {
                ps = ConnectionDB.dbConnection.prepareStatement(deleteRecordingServiceSql);
                ps.setInt(1, recordingServiceId);
                System.out.println("SQL (recordingservice): " + deleteRecordingServiceSql + ", recordingServiceId: " + recordingServiceId);
                int rowsAffected = ps.executeUpdate();
                System.out.println("Удалено записей из recordingservice: " + rowsAffected);
                if (rowsAffected == 0) {
                    System.err.println("Ошибка: Запись с ID " + recordingServiceId + " не найдена в recordingservice для удаления.");
                    return false;
                }
            } catch (SQLException e) {
                System.err.println("Ошибка при удалении из recordingservice: " + e.getMessage());
                return false;
            } finally {closeStatement(ps);}
            boolean deleteService = false;
            try {
                ps = ConnectionDB.dbConnection.prepareStatement(countRecordingsForServiceSql);
                ps.setInt(1, serviceId);
                System.out.println("SQL (countRecordingsForService): " + countRecordingsForServiceSql + ", serviceId: " + serviceId);
                rs = ps.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("Количество записей в recordingservice для serviceId " + serviceId + ": " + count);
                    deleteService = (count == 0);
                }
            } catch (SQLException e) {
                System.err.println("Ошибка при подсчете записей в recordingservice: " + e.getMessage());
                return false;
            } finally {
                closeResultSet(rs);
                closeStatement(ps);
            }
            if (deleteService && serviceId != -1) {
                try {
                    ps = ConnectionDB.dbConnection.prepareStatement(deleteServiceSql);
                    ps.setInt(1, serviceId);
                    System.out.println("SQL (service): " + deleteServiceSql + ", serviceId: " + serviceId);
                    int rowsAffected = ps.executeUpdate();
                    System.out.println("Удалено услуг: " + rowsAffected);

                    if (rowsAffected == 0) {
                        System.err.println("Ошибка: Услуга с ID " + serviceId + " не найдена для удаления.");
                        return false;
                    }

                } catch (SQLException e) {System.err.println("Ошибка при удалении из service: " + e.getMessage());
                    return false;
                } finally {closeStatement(ps);}
            } else {
                System.out.println("Услуга с ID " + serviceId + " не будет удалена, так как есть связанные записи.");
            }
            System.out.println("Запись из recordingservice с ID " + recordingServiceId + " успешно удалена.");
            return true;
        } catch (Exception e) {
            System.err.println("Неизвестная ошибка: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            closeStatement(ps);
        }
    }
    private void closeStatement(PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при закрытии PreparedStatement: " + e.getMessage());
        }
    }
    private void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при закрытии ResultSet: " + e.getMessage());
        }
    }

    public BeautyService getServiceByName(String serviceName) {
        String query = "SELECT rs.idRecordingService, s.name, s.price, s.type, s.time, " +
                "CONCAT(e.surname, ' ', e.name) AS employeeFullName " +
                "FROM recordingservice rs " +
                "JOIN service s ON rs.service_idService = s.idService " +
                "JOIN employee e ON rs.employee_idEmployee = e.idEmployee " +
                "WHERE s.name = ?";

        try (PreparedStatement preparedStatement = ConnectionDB.dbConnection.prepareStatement(query)) {
            preparedStatement.setString(1, serviceName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                BeautyService beautyService = new BeautyService();
                beautyService.setId(resultSet.getInt("idRecordingService"));
                beautyService.setName(resultSet.getString("name"));
                beautyService.setPrice(resultSet.getDouble("price"));
                beautyService.setType(resultSet.getString("type"));


                LocalDateTime time = resultSet.getTimestamp("time").toLocalDateTime();
                beautyService.setTime(time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

                beautyService.setEmployeeName(resultSet.getString("employeeFullName"));
                return beautyService;
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении услуги по имени: " + e.getMessage());
        }
        return null;
    }
}
