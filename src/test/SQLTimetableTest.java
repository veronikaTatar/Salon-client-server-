package test;

import DB.SQLFactory;
import Salon.Authorization;
import Salon.StaffTimetable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

public class SQLTimetableTest {
    @Test
    void testGetSchedule() throws SQLException, ClassNotFoundException {
        SQLFactory sqlFactory = new SQLFactory();

        List<StaffTimetable> scheduleList = sqlFactory.getStaffTimetable().getSchedule();

        Assertions.assertFalse(scheduleList.isEmpty(), "Расписание должно содержать данные.");

        StaffTimetable firstSchedule = scheduleList.get(0);
        Assertions.assertEquals(1, firstSchedule.getIdOrder(), "ID заказа должен совпадать.");
        Assertions.assertEquals("ваша_услуга", firstSchedule.getNameService(), "Имя услуги должно совпадать.");
        Assertions.assertEquals("Имя сотрудника", firstSchedule.getNameEmployee(), "Имя сотрудника должно совпадать.");
        Assertions.assertEquals("Фамилия сотрудника", firstSchedule.getLastnameEmployee(), "Фамилия сотрудника должна совпадать.");
        Assertions.assertEquals(60, firstSchedule.getLengthOfTime(), "Длительность услуги должна совпадать.");
    }
}
