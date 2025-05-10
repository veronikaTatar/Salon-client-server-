package DB;

import Salon.Order;
import Salon.Role;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface ISQLOrder {

    Integer findClientIdByUserId(int idUser) throws SQLException;

    boolean addOrder(Order obj) throws SQLException;

    Integer findUserIdByClientId(int idClient) throws SQLException;

    List<Order> getAllOrders() throws SQLException;

    List<Order> getOrdersByClientId(int userId) throws SQLException;

    List<Order> getOrdersByEmployeeId(int userId) throws SQLException;

    boolean changeOrderStatus(Order obj);

    boolean deleteOrder(Order obj);

}
