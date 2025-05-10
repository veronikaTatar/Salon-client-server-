package DB;

import Salon.EmployeeWorkTime;
import Salon.StaffTimetable;
import java.sql.SQLException;
import java.util.List;

public interface ISQLStaffTimetable {

    List<StaffTimetable> getSchedule() throws SQLException;

    List<EmployeeWorkTime> getDataDiagram(StaffTimetable filtr);

}
