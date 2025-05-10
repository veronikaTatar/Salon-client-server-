package DB;

import java.sql.SQLException;

public abstract class AbstractFactory {
   public abstract SQLEmployee getEmployee() throws SQLException, ClassNotFoundException;
   public abstract SQLConsumer getConsumer() throws SQLException, ClassNotFoundException;
    public abstract SQLAuthorization getRole() throws SQLException, ClassNotFoundException;
    public abstract SQLBeautyService getBeautyService() throws SQLException, ClassNotFoundException;
    public abstract SQLStaffTimetable getStaffTimetable() throws SQLException, ClassNotFoundException;
   public abstract SQLOrder getOrder() throws SQLException, ClassNotFoundException;
    public abstract SQLAdmin getAdmin() throws SQLException, ClassNotFoundException;

}
