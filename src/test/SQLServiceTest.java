package test;

import DB.SQLFactory;
import Salon.Authorization;
import Salon.BeautyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

class SQLServiceTest {
    @Test
    void testAddService() throws SQLException, ClassNotFoundException {
        // Создание объекта для добавления услуги
        SQLFactory sqlFactory = new SQLFactory();
        BeautyService service = new BeautyService();
        service.setName("Мужская стрижка");
        service.setTime("2023-05-03 10:00:00");
        service.setType("Стрижка");
        service.setPrice(15);
        service.setEmployeeName("Иван Иванович Степанов");
        service.setLengthOfTime(30);

        boolean isInserted = sqlFactory.getBeautyService().insert(service);

        Assertions.assertTrue(isInserted, "Услуга должна быть успешно добавлена.");
        BeautyService retrievedService = sqlFactory.getBeautyService().getServiceByName("Мужская стрижка");
        Assertions.assertNotNull(retrievedService, "Услуга должна быть найдена в базе данных.");
        Assertions.assertEquals(service.getName(), retrievedService.getName(), "Имена услуг должны совпадать.");
        Assertions.assertEquals(service.getPrice(), retrievedService.getPrice(), "Цены услуг должны совпадать.");
        Assertions.assertEquals(service.getTime(), retrievedService.getTime(), "Время услуг должно совпадать.");
        Assertions.assertEquals(service.getType(), retrievedService.getType(), "Типы услуг должны совпадать.");
        Assertions.assertEquals(service.getEmployeeName(), retrievedService.getEmployeeName(), "Имена сотрудников должны совпадать.");
        Assertions.assertEquals(service.getLengthOfTime(), retrievedService.getLengthOfTime(), "Продолжительности услуг должны совпадать.");
    }
}