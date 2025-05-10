package DB;

import Salon.Employee;
import Salon.Role;
import Salon.Consumer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLConsumer implements ISQLConsumer {
    private static SQLConsumer instance;
    private ConnectionDB dbConnection;

    private SQLConsumer() throws SQLException, ClassNotFoundException {
        dbConnection = ConnectionDB.getInstance();
    }

    public static synchronized SQLConsumer getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null) {
            instance = new SQLConsumer();
        }
        return instance;
    }

    public SQLConsumer getConsumer() throws SQLException, ClassNotFoundException {
        return this; // Вернуть текущий экземпляр
    }
    public List<Consumer> getAllConsumer() throws SQLException {
        List<Consumer> consumerList = new ArrayList<>();
        String query = "SELECT c.idClient, c.name, c.surname, c.email, u.login " +
                "FROM consumer c " +
                "JOIN user u ON u.idUser = c.user_idUser " +
                "ORDER BY c.surname, c.name"; // Сортировка по фамилии и имени

        try (PreparedStatement stmt = dbConnection.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                Consumer consumer = new Consumer();
                consumer.setId(resultSet.getInt("idClient"));
                consumer.setName(resultSet.getString("name"));
                consumer.setSurname(resultSet.getString("surname"));
                consumer.setEmail(resultSet.getString("email"));
                consumer.setLogin(resultSet.getString("login")); // Логин из таблицы user

                consumerList.add(consumer);
            }
        }
        return consumerList;
    }

    public boolean isLoginExists(String login) throws SQLException {
        String query = "SELECT 1 FROM clients WHERE login = ? LIMIT 1";
        try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(query)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    public Role registerClient(Consumer client) throws SQLException {
        String checkLoginQuery = "SELECT COUNT(*) FROM user WHERE login = ?";
        String proc = "{call register_client(?,?,?,?,?,?)}";
        Role role = new Role();

        // Проверка логина
        try (PreparedStatement checkStmt = ConnectionDB.dbConnection.prepareStatement(checkLoginQuery)) {
            checkStmt.setString(1, client.getLogin());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Ошибка: Логин уже существует.");
                role.setId(0); // Установите ID в 0 для обозначения ошибки
                return role; // Возвращаем роль с ID 0
            }
        }

        // Основная логика регистрации
        try (CallableStatement callableStatement = ConnectionDB.dbConnection.prepareCall(proc)) {
            callableStatement.setString(1, client.getSurname());
            callableStatement.setString(2, client.getName());
            callableStatement.setString(3, client.getEmail());
            callableStatement.setString(4, client.getLogin());
            callableStatement.setString(5, client.getPassword());
            callableStatement.registerOutParameter(6, Types.INTEGER);
            callableStatement.execute();

            int userId = callableStatement.getInt(6);
            if (userId > 0) {
                client.setId(userId);
                role.setId(userId);
                role.setRole("Consumer");
            } else {
                role.setId(0);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка SQL: " + e.getMessage());
        }

        return role;
    }

    public Consumer findConsumerByUserId(int idUser) throws SQLException {
        String query = "SELECT c.*, u.login, u.password, u.role " +
                "FROM consumer c " +
                "JOIN user u ON c.user_idUser = u.idUser " +
                "WHERE u.idUser = ?"; // Запрос к таблице user

        try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(query)) {
            stmt.setInt(1, idUser); // Устанавливаем параметр ID пользователя

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Создаем объект Consumer и заполняем его данными из результата
                    Consumer consumer = new Consumer();
                    consumer.setId(rs.getInt("idClient")); // Получаем ID клиента
                    consumer.setName(rs.getString("name")); // Получаем имя
                    consumer.setSurname(rs.getString("surname")); // Получаем фамилию
                    consumer.setEmail(rs.getString("email")); // Получаем электронную почту
                    consumer.setLogin(rs.getString("login")); // Получаем логин пользователя
                    consumer.setPassword(rs.getString("password")); // Получаем пароль пользователя

                    return consumer; // Возвращаем найденного клиента
                }
            }
        }
        return null; // Если клиент не найден, возвращаем null
    }

  public boolean update(Consumer obj) throws SQLException {

      Consumer currentConsumer = findConsumerByUserId(obj.getId());

      if (currentConsumer == null) {
          System.out.println("Клиент не найден.");
          return false; // Если клиент не найден, ничего не обновляем
      }

      // Запрос для обновления таблицы consumer
      String query = "UPDATE consumer SET "
              + "name = ?, "
              + "surname = ?, "
              + "email = ? "
              + "WHERE idClient = ?;";

      // Запрос для обновления таблицы user
      String userQuery = "UPDATE user SET "
              + "login = ?, "
              + "password = ? "
              + "WHERE idUser = ?;";

      // Запрос для получения idUser из таблицы consumer
      String getUserIdQuery = "SELECT user_idUser FROM consumer WHERE idClient = ?";

      try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(query);
           PreparedStatement userStmt = ConnectionDB.dbConnection.prepareStatement(userQuery);
           PreparedStatement getUserIdStmt = ConnectionDB.dbConnection.prepareStatement(getUserIdQuery)) {

          // Устанавливаем значения для таблицы consumer
          stmt.setString(1, obj.getName() != null ? obj.getName() : currentConsumer.getName());
          stmt.setString(2, obj.getSurname() != null ? obj.getSurname() : currentConsumer.getSurname());
          stmt.setString(3, obj.getEmail() != null ? obj.getEmail() : currentConsumer.getEmail());
          stmt.setInt(4, currentConsumer.getId()); // ID клиента для обновления

          // Выполняем обновление для таблицы consumer
          stmt.executeUpdate();

          // Получаем idUser из таблицы consumer
          int userId = -1; // Значение по умолчанию, если не найдено
          getUserIdStmt.setInt(1, currentConsumer.getId());
          try (ResultSet rs = getUserIdStmt.executeQuery()) {
              if (rs.next()) {
                  userId = rs.getInt("user_idUser");
              } else {
                  System.out.println("Не удалось получить user_idUser для idClient: " + currentConsumer.getId());
                  return false; // Если не удалось получить user_idUser, возвращаем false
              }
          }

          // Устанавливаем значения для таблицы user
          userStmt.setString(1, obj.getLogin() != null ? obj.getLogin() : currentConsumer.getLogin());
          userStmt.setString(2, obj.getPassword() != null ? obj.getPassword() : currentConsumer.getPassword());
          userStmt.setInt(3, userId); // Используем userId, полученный из базы данных

          // Выполняем обновление для таблицы user
          userStmt.executeUpdate();

      } catch (SQLException e) {
          if (e.getSQLState().equals("45000")) {
              System.out.println("Ошибка: Данные не уникальны (возможно, логин уже существует).");
              return false; // Обработка ошибки уникальности
          } else {
              e.printStackTrace();
              return false;
          }
      } catch (Exception e) {
          e.printStackTrace();
          return false;
      }
      return true; // Возвращаем true, если обновление прошло успешно
  }

    public boolean deleteConsumer(Consumer obj) {
        int clientId = obj.getId(); // Получаем idClient из объекта Consumer
        System.out.println("Выполняется удаление клиента с idClient: " + clientId);

        String deleteTimetableSql = "DELETE FROM timetable WHERE order_idOrder IN (SELECT idOrder FROM `order` WHERE consumer_idClient = ?)";
        String deleteOrderSql = "DELETE FROM `order` WHERE consumer_idClient = ?";
        String deleteConsumerSql = "DELETE FROM consumer WHERE idClient = ?";
        String deleteUserSql = "DELETE FROM user WHERE idUser = ?"; // Упрощенный запрос

        PreparedStatement ps = null;
        ResultSet rs = null;
        int userId = -1;

        try {
            // 1. Получение user_idUser ПЕРЕД удалением consumer
            try {
                String getUserIdSql = "SELECT user_idUser FROM consumer WHERE idClient = ?";
                ps = ConnectionDB.dbConnection.prepareStatement(getUserIdSql);
                ps.setInt(1, clientId);
                rs = ps.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_idUser");
                } else {
                    System.err.println("Ошибка: Не найден user_idUser для idClient: " + clientId);
                    return false; // Если не нашли user_idUser, значит что-то не так
                }
            } catch (SQLException e) {
                System.err.println("Ошибка при получении user_idUser: " + e.getMessage());
                return false;
            } finally {
                closeResultSet(rs);
                closeStatement(ps);
            }

            // 2. Удаление записей из timetable
            try {
                ps = ConnectionDB.dbConnection.prepareStatement(deleteTimetableSql);
                ps.setInt(1, clientId);
                System.out.println("SQL (timetable): " + deleteTimetableSql + ", clientId: " + clientId);
                int rowsAffected = ps.executeUpdate();
                System.out.println("Удалено записей из timetable: " + rowsAffected);
            } catch (SQLException e) {
                System.err.println("Ошибка при удалении из timetable: " + e.getMessage());
                return false;
            } finally {
                closeStatement(ps);
            }

            // 3. Удаление записей из order
            try {
                ps = ConnectionDB.dbConnection.prepareStatement(deleteOrderSql);
                ps.setInt(1, clientId);
                System.out.println("SQL (order): " + deleteOrderSql + ", clientId: " + clientId);
                int rowsAffected = ps.executeUpdate();
                System.out.println("Удалено записей из order: " + rowsAffected);
            } catch (SQLException e) {
                System.err.println("Ошибка при удалении из order: " + e.getMessage());
                return false;
            } finally {
                closeStatement(ps);
            }

            // 4. Удаление записи из consumer
            try {
                ps = ConnectionDB.dbConnection.prepareStatement(deleteConsumerSql);
                ps.setInt(1, clientId);
                System.out.println("SQL (consumer): " + deleteConsumerSql + ", clientId: " + clientId);
                int rowsAffected = ps.executeUpdate();
                System.out.println("Удалено записей из consumer: " + rowsAffected);
                if (rowsAffected == 0) {
                    System.out.println("Предупреждение: Запись о consumer с idClient " + clientId + " не найдена.");
                    return true; // Если consumer не найден, считаем, что удаление прошло успешно
                }
            } catch (SQLException e) {
                System.err.println("Ошибка при удалении из consumer: " + e.getMessage());
                return false;
            } finally {
                closeStatement(ps);
            }

            // 5. Удаление записи из user
            try {
                ps = ConnectionDB.dbConnection.prepareStatement(deleteUserSql);
                ps.setInt(1, userId); // Используем userId, полученный ранее
                System.out.println("SQL (user): " + deleteUserSql + ", userId: " + userId);
                int rowsAffected = ps.executeUpdate();
                System.out.println("Удалено записей из user: " + rowsAffected);
                if (rowsAffected == 0) {
                    System.out.println("Предупреждение: Запись о user с idUser " + userId + " не найдена.");
                }
            } catch (SQLException e) {
                System.err.println("Ошибка при удалении из user: " + e.getMessage());
                return false;
            } finally {
                closeStatement(ps);
            }

            System.out.println("Клиент с idClient " + clientId + " успешно удален.");
            return true; // Успешное удаление

        } catch (Exception e) {
            System.err.println("Неизвестная ошибка: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeStatement(ps);
        }
    }

    // Helper method to close PreparedStatement
    private void closeStatement(PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при закрытии PreparedStatement: " + e.getMessage());
        }
    }

    // Helper method to close ResultSet
    private void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при закрытии ResultSet: " + e.getMessage());
        }
    }

}
