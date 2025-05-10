package DB;

import Salon.Employee;
import Salon.Role;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public interface ISQLEmployee {
    void findEmployee(Employee obj);

    boolean insert(Employee obj);

    boolean update(Employee obj);

    Employee findEmployeeByUserId(int idUser) throws SQLException;

    boolean employeeUpdateEmployee(Employee obj) throws SQLException;

    List<Employee> getAllEmployee() throws SQLException;

    ArrayList<Employee> getEmployee(Role r);

    boolean deleteEmployee(Employee obj);

    Role getIdByConsumer(Role obj);
}