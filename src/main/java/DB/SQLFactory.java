package DB;


import java.sql.SQLException;

public class SQLFactory extends AbstractFactory {
    public SQLEmployee getEmployee() throws SQLException, ClassNotFoundException {
        return SQLEmployee.getInstance();
    }

    public SQLConsumer getConsumer() throws SQLException, ClassNotFoundException {
        return SQLConsumer.getInstance();
    }

    public SQLAuthorization getRole() throws SQLException, ClassNotFoundException {
        return SQLAuthorization.getInstance();
    }

    @Override
    public SQLBeautyService getBeautyService() throws SQLException, ClassNotFoundException {
        return SQLBeautyService.getInstance();
    }

    @Override
    public SQLStaffTimetable getStaffTimetable() throws SQLException, ClassNotFoundException {
        return SQLStaffTimetable.getInstance();
    }

    @Override
    public SQLOrder getOrder() throws SQLException, ClassNotFoundException {
        return SQLOrder.getInstance();
    }

    public SQLAdmin getAdmin() throws SQLException, ClassNotFoundException {
        return SQLAdmin.getInstance();
    }


}

