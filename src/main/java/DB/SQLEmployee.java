package DB;

import Salon.Consumer;
import Salon.Role;
import Salon.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLEmployee implements ISQLEmployee {
    private static SQLEmployee instance;
    private ConnectionDB dbConnection;

    private SQLEmployee() throws SQLException, ClassNotFoundException {
        dbConnection = ConnectionDB.getInstance();
    }

    public static synchronized SQLEmployee getInstance() throws SQLException, ClassNotFoundException {
        if (instance == null) {
            instance = new SQLEmployee();
        }
        return instance;
    }


    public void findEmployee(Employee obj) {

    }

    public boolean insert(Employee obj) {
        String proc = "{call add_employee2(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement callableStatement = ConnectionDB.dbConnection.prepareCall(proc)) {

            // Обратите внимание на порядок параметров
            callableStatement.setString(1, obj.getSurname());
            callableStatement.setString(2, obj.getName());
            callableStatement.setString(3, obj.getPatronymic());   // Отчество
            callableStatement.setString(4, obj.getPhone());        // Телефон
            callableStatement.setString(5, obj.getPosition());     // Должность
            callableStatement.setDouble(6, obj.getSalary());       // Зарплата
            callableStatement.setString(7, obj.getLogin());        // Логин
            callableStatement.setString(8, obj.getPassword());     // Пароль

            // Выполняем вызов процедуры
            callableStatement.executeUpdate();
            return true; // Если процедура успешно выполнена

        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении данных: " + e.getMessage());
            return false; // Возвращаем false в случае ошибки
        }
    }


    public boolean update(Employee obj) {
        String proc = "{call update_employee3(?,?,?,?,?,?,?,?,?)}"; // Вызов хранимой процедуры
        try (CallableStatement callableStatement = ConnectionDB.dbConnection.prepareCall(proc)) {
            callableStatement.setInt(1, obj.getId());
            callableStatement.setString(2, obj.getName());
            callableStatement.setString(3, obj.getSurname());
            callableStatement.setString(4, obj.getPatronymic());
            callableStatement.setString(5, obj.getPhone());
            callableStatement.setString(6, obj.getPosition());
            callableStatement.setDouble(7, obj.getSalary());
            callableStatement.setString(8, obj.getLogin());
            callableStatement.setString(9, obj.getPassword());

            callableStatement.execute();   // Выполнение процедуры
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
    public Employee findEmployeeByUserId(int idUser) throws SQLException {
        String query = "SELECT e.*, u.login, u.password " +
                "FROM employee e " +
                "JOIN user u ON e.user_idUser = u.idUser " +
                "WHERE u.idUser = ?"; // Запрос к таблице user

        try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(query)) {
            stmt.setInt(1, idUser); // Устанавливаем параметр ID пользователя

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Полученные данные: " + rs.getInt("idEmployee") + ", " + rs.getString("surname"));
                   //rs.getInt("idEmployee") ЭТО id отрудника
                    // Создаем объект Employee и заполняем его данными из результата
                    Employee employee = new Employee();
                    employee.setId(rs.getInt("idEmployee")); // Получаем ID сотрудника
                    employee.setSurname(rs.getString("surname")); // Получаем фамилию
                    employee.setName(rs.getString("name")); // Получаем имя
                    employee.setPatronymic(rs.getString("patronymic")); // Получаем отчество
                    employee.setPosition(rs.getString("position")); // Получаем должность
                    employee.setPhone(rs.getString("phone")); // Получаем телефон
                    employee.setSalary(rs.getDouble("salary")); // Получаем зарплату
                    employee.setLogin(rs.getString("login")); // Получаем логин пользователя
                    employee.setPassword(rs.getString("password")); // Получаем пароль пользователя

                    return employee; // Возвращаем найденного сотрудника
                }
            }
        }
        return null;
    }

    public boolean employeeUpdateEmployee(Employee obj) throws SQLException {
        Employee currentEmployee = findEmployeeByUserId(obj.getId());
        System.out.println("Обновляем сотрудника с ID user: " + obj.getId());
        if (currentEmployee == null) {
            System.out.println("Сотрудник не найден.");
            return false;
        }

        // Запрос для обновления таблицы employee
        String query = "UPDATE employee SET "
                + "surname = ?, "
                + "name = ?, "
                + "patronymic = ?, "
                + "phone = ?, "
                + "position = ?, "
                + "salary = ? "
                + "WHERE idEmployee = ?;";

        // Запрос для обновления таблицы user
        String userQuery = "UPDATE user SET "
                + "login = ?, "
                + "password = ? "
                + "WHERE idUser = ?;"; // Здесь нужно использовать idUser

        try (PreparedStatement stmt = ConnectionDB.dbConnection.prepareStatement(query);
             PreparedStatement userStmt = ConnectionDB.dbConnection.prepareStatement(userQuery)) {

            // Обновление таблицы employee
            stmt.setString(1, obj.getSurname() != null ? obj.getSurname() : currentEmployee.getSurname());
            stmt.setString(2, obj.getName() != null ? obj.getName() : currentEmployee.getName());
            stmt.setString(3, obj.getPatronymic() != null ? obj.getPatronymic() : currentEmployee.getPatronymic());
            stmt.setString(4, obj.getPhone() != null ? obj.getPhone() : currentEmployee.getPhone());
            stmt.setString(5, currentEmployee.getPosition());
            stmt.setDouble(6, currentEmployee.getSalary());
            stmt.setInt(7, currentEmployee.getId()); // idEmployee
            stmt.executeUpdate();

            // Обновление таблицы user
            userStmt.setString(1, obj.getLogin() != null ? obj.getLogin() : currentEmployee.getLogin());
            userStmt.setString(2, obj.getPassword() != null ? obj.getPassword() : currentEmployee.getPassword());
            userStmt.setInt(3, obj.getId()); // Здесь нужно передать idUser, который пришел в obj
            userStmt.executeUpdate();

        } catch (SQLException e) {
            if (e.getSQLState().equals("45000")) {
                System.out.println("Ошибка: Данные не уникальны (возможно, логин уже существует).");
                return false;
            } else {
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<Employee> getAllEmployee() throws SQLException {
        List<Employee> employeeList = new ArrayList<>();
        String query = "SELECT e.idEmployee, u.login, e.surname, e.name, e.patronymic, e.position, e.phone, e.salary " +
                "FROM employee e " +
                "JOIN `user` u ON u.idUser = e.user_idUser " +
                "ORDER BY e.surname, e.name;"; // Добавлено упорядочивание по фамилии и имени

        try (PreparedStatement stmt = dbConnection.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                Employee employee = new Employee();
                employee.setId(resultSet.getInt("idEmployee"));
                employee.setLogin(resultSet.getString("login")); // Теперь получаем login из таблицы user
                employee.setSurname(resultSet.getString("surname"));
                employee.setName(resultSet.getString("name"));
                employee.setPatronymic(resultSet.getString("patronymic"));
                employee.setPosition(resultSet.getString("position"));
                employee.setPhone(resultSet.getString("phone"));
                employee.setSalary(resultSet.getDouble("salary")); // salary сразу получает значение типа double

                employeeList.add(employee);
            }
        }
        return employeeList;
    }


    public ArrayList<Employee> getEmployee(Role r) {//удалить
        String str = "SELECT e.surname, e.name, e.patronymic, e.position, e.phone, e.salary " +
                "FROM employee e " +
                "JOIN `keys` k ON k.id_keys = e.user_idUser " + // предположим, что это связь
                "WHERE k.id_keys = " + r.getId() + ";";

        ArrayList<String[]> result = dbConnection.getArrayResult(str);
        ArrayList<Employee> employeeList = new ArrayList<>();

        for (String[] items : result) {
            Employee employee = new Employee();
            employee.setSurname(items[0]);
            employee.setName(items[1]);
            employee.setPatronymic(items[2]);
            employee.setPosition(items[3]);
            employee.setPhone(items[4]);
            employee.setSalary(Double.parseDouble(items[5])); // предположим, что salary сохраняется как String

            employeeList.add(employee);
        }

        return employeeList;
    }


    public boolean deleteEmployee(Employee obj) {
        String deleteUserQuery = "DELETE FROM user WHERE idUser = ?";
        String deleteEmployeeQuery = "DELETE FROM employee WHERE idEmployee = ?";
        String deleteRecordingServiceQuery = "DELETE FROM recordingservice WHERE employee_idEmployee = ?";
        String deleteOrderQuery = "DELETE FROM `order` WHERE recordingservice_idRecordingService IN (SELECT idRecordingService FROM recordingservice WHERE employee_idEmployee = ?)";
        String deleteTimetableQuery = "DELETE FROM timetable WHERE order_idOrder IN (SELECT idOrder FROM `order` WHERE recordingservice_idRecordingService IN (SELECT idRecordingService FROM recordingservice WHERE employee_idEmployee = ?))";

        try {
            // Получаем id пользователя по логину
            String getUserIdQuery = "SELECT idUser FROM user WHERE login = ?";
            try (PreparedStatement getUserIdStmt = ConnectionDB.dbConnection.prepareStatement(getUserIdQuery)) {
                getUserIdStmt.setString(1, obj.getLogin());
                try (ResultSet rs = getUserIdStmt.executeQuery()) {
                    if (rs.next()) {
                        int userId = rs.getInt("idUser");
                        int employeeId = obj.getId(); // Получаем id сотрудника

                        // 1. Удаляем записи из timetable
                        try (PreparedStatement deleteTimetableStmt = ConnectionDB.dbConnection.prepareStatement(deleteTimetableQuery)) {
                            deleteTimetableStmt.setInt(1, employeeId);
                            deleteTimetableStmt.executeUpdate();
                        }

                        // 2. Удаляем связанные заказы
                        try (PreparedStatement deleteOrderStmt = ConnectionDB.dbConnection.prepareStatement(deleteOrderQuery)) {
                            deleteOrderStmt.setInt(1, employeeId);
                            deleteOrderStmt.executeUpdate();
                        }

                        // 3. Удаляем записи из recordingservice
                        try (PreparedStatement deleteServiceStmt = ConnectionDB.dbConnection.prepareStatement(deleteRecordingServiceQuery)) {
                            deleteServiceStmt.setInt(1, employeeId);
                            deleteServiceStmt.executeUpdate();
                        }

                        // 4. Удаляем запись из employee
                        try (PreparedStatement deleteEmployeeStmt = ConnectionDB.dbConnection.prepareStatement(deleteEmployeeQuery)) {
                            deleteEmployeeStmt.setInt(1, employeeId);
                            deleteEmployeeStmt.executeUpdate();
                        }

                        // 5. Удаляем пользователя
                        try (PreparedStatement deleteUserStmt = ConnectionDB.dbConnection.prepareStatement(deleteUserQuery)) {
                            deleteUserStmt.setInt(1, userId);
                            int userRowsAffected = deleteUserStmt.executeUpdate();

                            return userRowsAffected > 0; // Возвращаем true, если удаление пользователя прошло успешно
                        }
                    } else {
                        System.out.println("Пользователь не найден.");
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка SQL: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public Role getIdByConsumer(Role obj) {//не используется
        String proc = "{call get_idkeys_bystudents(?,?)}";
        try (CallableStatement callableStatement = ConnectionDB.dbConnection.prepareCall(proc)) {
            callableStatement.setInt(1, obj.getId());
            callableStatement.registerOutParameter(2, Types.INTEGER);
            callableStatement.execute();
            obj.setId(callableStatement.getInt(2));
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("ошибка");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
