package ServerWork;
import DB.SQLFactory;
import Salon.*;


import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

public class Worker implements Runnable {
    protected Socket clientSocket = null;
    ObjectInputStream sois;
    ObjectOutputStream soos;

    public Worker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            soos = new ObjectOutputStream(clientSocket.getOutputStream());
            sois = new ObjectInputStream(clientSocket.getInputStream());
            while (true) {
                System.out.println("Получение команды от клиента...");
                String choice = sois.readObject().toString();
                System.out.println(choice);
                System.out.println("Команда получена");
                switch (choice) {

                    case "GET_SCHEDULE" -> {
                        System.out.println("Запрос к БД на получение графика работы: " + clientSocket.getInetAddress().toString());
                        SQLFactory sqlFactory = new SQLFactory();
                        List<StaffTimetable> scheduleList = sqlFactory.getStaffTimetable().getSchedule();

                        if (scheduleList.isEmpty()) {
                            System.out.println("Нет графика работы для отправки.");
                            // Можно отправить специальное сообщение клиенту, если нужно
                            soos.writeObject("NO_DATA"); // Или любое другое сообщение о том, что данных нет
                        } else {
                            System.out.println(scheduleList.toString());
                            soos.writeObject(scheduleList);
                            System.out.println("Отправлено " + scheduleList.size() + " записей в графике");
                        }
                    }
                        case "GET_SCHEDULE_BY_EMPLOYEE" -> {
                            System.out.println("Запрос к БД на графика работы для сотрудника: " + clientSocket.getInetAddress().toString());

                            // Получаем идентификатор клиента
                            int employeeId = (Integer) sois.readObject();

                            // Создаем экземпляр SQLFactory для работы с базой данных
                            SQLFactory sqlFactory = new SQLFactory();

                            // Получаем заказы для конкретного клиента
                            List<StaffTimetable> orders = sqlFactory.getStaffTimetable().getScheduleByEmployeeId(employeeId);

                            if (orders.isEmpty()) {
                                System.out.println("Нет доступного графика для сотрудника с ID: " + employeeId);
                                // Отправляем сообщение клиенту, если данных нет
                                soos.writeObject("NO_DATA");
                            } else {
                                System.out.println("Найден график для сотрудника: " + orders.toString());
                                // Отправляем список заказов клиенту
                                soos.writeObject(orders);
                                System.out.println("Отправлено " + orders.size() + " записей в графике для сотрудника с ID: " + employeeId);
                            }
                        }

                    case "GET_DATA_DIAGRAM" -> {//проверка
                        System.out.println("Запрос к БД на получение заявок для сотрудника: " + clientSocket.getInetAddress().toString());


                        Object receivedObject = sois.readObject();
                        if (receivedObject instanceof StaffTimetable) {
                            StaffTimetable filtr = (StaffTimetable) receivedObject;

                            // Создаем экземпляр SQLFactory для работы с базой данных
                            SQLFactory sqlFactory = new SQLFactory();


                            List<EmployeeWorkTime> dataFiltr = sqlFactory.getStaffTimetable().getDataDiagram(filtr);

                            if (dataFiltr.isEmpty()) {
                                System.out.println("Нет данных: " + filtr);
                                // Отправляем сообщение клиенту, если данных нет
                                soos.writeObject("NO_DATA");
                            } else {
                                System.out.println("Найдены данные: " + dataFiltr.toString());
                                // Отправляем список заказов клиенту
                                soos.writeObject(dataFiltr);
                                System.out.println("Отправлено " + dataFiltr.size() + " данных на число " + filtr);
                            }
                        } else {
                            System.out.println("Получен неправильный тип данных: " + receivedObject.getClass().getName());
                            // Обработка ошибки: отправляем сообщение о неверном типе
                            soos.writeObject("INVALID_DATA_TYPE");
                        }
                    }
                    case "exportToCSV"-> {
                        System.out.println("Запрос экспорт данных в CSV файл, клиент: " + clientSocket.getInetAddress().toString());
                        StaffTimetable data = (StaffTimetable) sois.readObject();
                        System.out.println(data.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getStaffTimetable().getDataList(data)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Ошибка при экспорте данных");
                        } 
                    }

                   case "GET_ALL_COMMENTS" -> {
                       System.out.println("Запрос к БД на получение отзывов: " + clientSocket.getInetAddress().toString());
                       SQLFactory sqlFactory = new SQLFactory();
                       List<Comment> serviceList = sqlFactory.getBeautyService().getAllComments();

                       System.out.println(serviceList);

                       soos.writeObject(serviceList);
                       System.out.println("Отправлено " + serviceList.size() + " отзывов");
                   }
                    case "UPDATE_COMMENT" -> {
                        System.out.println("Запрос к БД на изменение оценки(таблица comment), клиент: " + clientSocket.getInetAddress().toString());
                        Comment comment = (Comment) sois.readObject();
                        System.out.println(comment.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getBeautyService().updateComment(comment)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Ошибка при обновлении оценки");
                        }
                    }
                    case "addOrder" -> {
                        System.out.println("Выполняется бронирование услуги...");
                        Order order = (Order) sois.readObject();
                        System.out.println(order.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getOrder().addOrder(order)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Ошибка при бронировании услуги");
                        }
                    }
                    case "GET_ALL_ORDER" -> {
                        System.out.println("Запрос к БД на получение заявок: " + clientSocket.getInetAddress().toString());
                        SQLFactory sqlFactory = new SQLFactory();
                        List<Order> orderList = sqlFactory.getOrder().getAllOrders();

                        if (orderList.isEmpty()) {
                            System.out.println("Нет доступных заявок для отправки.");
                            // Можно отправить специальное сообщение клиенту, если нужно
                            soos.writeObject("NO_DATA"); // Или любое другое сообщение о том, что данных нет
                        } else {
                            System.out.println(orderList.toString());
                            soos.writeObject(orderList);
                            System.out.println("Отправлено " + orderList.size() + " заявок");
                        }
                    }
                    case "GET_ORDERS_BY_CLIENT" -> {
                        System.out.println("Запрос к БД на получение заявок для клиента: " + clientSocket.getInetAddress().toString());

                        // Получаем идентификатор клиента
                        int clientId = (Integer) sois.readObject();

                        // Создаем экземпляр SQLFactory для работы с базой данных
                        SQLFactory sqlFactory = new SQLFactory();

                        // Получаем заказы для конкретного клиента
                        List<Order> orders = sqlFactory.getOrder().getOrdersByClientId(clientId);

                        if (orders.isEmpty()) {
                            System.out.println("Нет доступных заявок для клиента с ID: " + clientId);
                            // Отправляем сообщение клиенту, если данных нет
                            soos.writeObject("NO_DATA");
                        } else {
                            System.out.println("Найдены заказы для клиента: " + orders.toString());
                            // Отправляем список заказов клиенту
                            soos.writeObject(orders);
                            System.out.println("Отправлено " + orders.size() + " заявок для клиента с ID: " + clientId);
                        }
                    }

                    case "GET_ORDERS_BY_EMPLOYEE" -> {
                        System.out.println("Запрос к БД на получение заявок для сотрудника: " + clientSocket.getInetAddress().toString());

                        // Получаем идентификатор клиента
                        int employeeId = (Integer) sois.readObject();

                        // Создаем экземпляр SQLFactory для работы с базой данных
                        SQLFactory sqlFactory = new SQLFactory();

                        // Получаем заказы для конкретного клиента
                        List<Order> orders = sqlFactory.getOrder().getOrdersByEmployeeId(employeeId);

                        if (orders.isEmpty()) {
                            System.out.println("Нет доступных заявок для сотрудника с ID: " + employeeId);
                            // Отправляем сообщение клиенту, если данных нет
                            soos.writeObject("NO_DATA");
                        } else {
                            System.out.println("Найдены заказы для сотрудника: " + orders.toString());
                            // Отправляем список заказов клиенту
                            soos.writeObject(orders);
                            System.out.println("Отправлено " + orders.size() + " заявок для сотрудника с ID: " + employeeId);
                        }
                    }

                    case "CHANGE_ORDER_STATUS" -> {
                        System.out.println("Запрос к БД на изменение статуса заявки(таблица order), клиент: " + clientSocket.getInetAddress().toString());
                        Order order = (Order) sois.readObject();
                        System.out.println(order.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getOrder().changeOrderStatus(order)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Ошибка при обновлении услуги");
                        }
                    }

                    case "DELETE_ORDER" ->{
                        System.out.println("Выполняется удаление записи...");
                        Order service = (Order) sois.readObject();
                        System.out.println(service.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getOrder().deleteOrder(service)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Ошибка при удалении записи");
                        }
                    }

                    case "GET_ALL_SERVICE" -> {
                        System.out.println("Запрос к БД на получение доступных услуг: " + clientSocket.getInetAddress().toString());
                        SQLFactory sqlFactory = new SQLFactory();
                        List<BeautyService> serviceList = sqlFactory.getBeautyService().getAllBeautyServices();
                        System.out.println(serviceList.toString());
                        soos.writeObject(serviceList);
                        System.out.println("Отправлено " + serviceList.size() + " услуг");
                    }


                    case "DELETE_SERVICE" -> {
                        System.out.println("Выполняется удаление услуги...");
                        BeautyService service = (BeautyService) sois.readObject();
                        System.out.println(service.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getBeautyService().deleteServiceRecord(service)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Ошибка при удалении услуги");
                        }
                    }
                    case "addService" -> {
                        System.out.println("Запрос к БД на добавление услуги(таблица service), услуга: " + clientSocket.getInetAddress().toString());
                        BeautyService service = (BeautyService) sois.readObject();
                        System.out.println(service.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getBeautyService().insert(service)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Ошибка при добавлении услуги");
                        }
                    }
                    case "updateConsumer" -> {
                        System.out.println("Запрос к БД на изменение клиента(таблица consumer), клиент: " + clientSocket.getInetAddress().toString());
                        Consumer consumer = (Consumer) sois.readObject();
                        System.out.println(consumer.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getConsumer().update(consumer)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Ошибка при обновлении услуги");
                        }
                    }

                    case "registrationConsumer" -> {
                        System.out.println("Запрос к БД на проверку пользователя, клиент: " + clientSocket.getInetAddress().toString());
                        /*Consumer consumer = (Consumer) sois.readObject();*/
                        try {
                            Consumer consumer = (Consumer) sois.readObject();
                            System.out.println("Получен объект: " + consumer);

                            SQLFactory sqlFactory = new SQLFactory();
                            Role r = sqlFactory.getConsumer().registerClient(consumer);
                            System.out.println((r.toString()));

                            // Проверка на успешное добавление
                            if (r.getId() != 0) {
                                soos.writeObject("SUCCESS: Клиент зарегистрирован"); // Успешная регистрация
                                soos.writeObject(r);          // Возврат информации о пользователе
                                System.out.println("Успешная регистрация: " + consumer.getLogin());
                            } else {
                                soos.writeObject("ERROR: Ошибка регистрации");
                                System.out.println("Ошибка регистрации для: " + consumer.getLogin());
                            }
                        } catch (ClassNotFoundException e) {
                            System.out.println("Ошибка: некорректный формат объекта от клиента");
                            soos.writeObject("ERROR: Invalid object format");
                        } catch (IOException e) {
                            System.out.println("Ошибка: проблема при чтении объекта");
                            soos.writeObject("ERROR: Problem reading object");
                        }
                    }
                    case "DELETE_CONSUMER" -> {
                        System.out.println("Выполняется удаление клиента...");
                        Consumer consumer = (Consumer) sois.readObject();
                        System.out.println(consumer.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getConsumer().deleteConsumer(consumer)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Ошибка при удалении услуги");
                        }
                    }

                    case "authorization" -> {
                        try {
                            System.out.println("Выполняется авторизация пользователя....");
                            Authorization auth = (Authorization) sois.readObject(); // Чтение объекта
                            System.out.println(auth.toString());

                            SQLFactory sqlFactory = new SQLFactory();
                            Role r = sqlFactory.getRole().getRole(auth); // Получение роли
                            System.out.println(r.toString());

                            if (r.getId() != 0 && !r.getRole().isEmpty()) { // Проверка на наличие роли
                                soos.writeObject("OK");
                                soos.writeObject(r);
                            } else {
                                soos.writeObject("There is no data!");
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            System.err.println("Ошибка при обработке авторизации: " + e.getMessage());
                            e.printStackTrace();
                            try {
                                soos.writeObject("Error encountered");
                            } catch (IOException ioException) {
                                System.err.println("Ошибка при отправке сообщения об ошибке: " + ioException.getMessage());
                            }
                        }
                    }
                    case "updateService" -> {
                        System.out.println("Запрос к БД на изменение услуги(таблица service), услуга: " + clientSocket.getInetAddress().toString());
                        BeautyService service = (BeautyService) sois.readObject();
                        System.out.println(service.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getBeautyService().update(service)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Ошибка при обновлении услуги");
                        }
                    }
                    case "GET_ALL_EMPLOYEE"-> {
                        System.out.println("Запрос на получение списка сотрудников");
                        SQLFactory sqlFactory = new SQLFactory();
                        List<Employee> employeeList = sqlFactory.getEmployee().getAllEmployee();
                        System.out.println(employeeList.toString());
                        soos.writeObject(employeeList);
                        System.out.println("Отправлено " + employeeList.size() + " сотрудников");

                    }
                    case "GET_ALL_CONSUMER"-> {
                        System.out.println("Запрос на получение списка клиентов");
                        SQLFactory sqlFactory = new SQLFactory();
                        List<Consumer> consumerList = sqlFactory.getConsumer().getAllConsumer();
                        System.out.println(consumerList.toString());
                        soos.writeObject(consumerList);
                        System.out.println("Отправлено " + consumerList.size() + " клиентов");

                    }
                    case "getEmployeeById" -> {
                        System.out.println("Запрос на получение данных сотрудника по ID");
                        Employee employee = (Employee) sois.readObject();
                        System.out.println(employee.toString());
                        SQLFactory sqlFactory = new SQLFactory();
                        Employee new_employee = sqlFactory.getEmployee().findEmployeeByUserId(employee.getId());

                        if (employee != null) {
                            System.out.println("Найден сотрудник: " + new_employee.getSurname() + " " + new_employee.getName());
                            soos.writeObject(new_employee);
                        } else {
                            System.out.println("Сотрудник с ID не найден");
                            soos.writeObject(null);
                        }
                    }

                    case "registrationEmployee" -> {
                        System.out.println("Запрос к БД на проверку пользователя(таблица teachers), клиент: " + clientSocket.getInetAddress().toString());
                        Employee employee = (Employee) sois.readObject();
                        System.out.println(employee.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getEmployee().insert(employee)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Incorrect Data");
                        }
                    }
                    case "updateEmployee" -> {
                        System.out.println("Запрос к БД на изменение сотрудника(таблица employee), сотрудник: " + clientSocket.getInetAddress().toString());
                        Employee employee = (Employee) sois.readObject();
                        System.out.println(employee.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getEmployee().update(employee)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Incorrect Data");
                        }
                    }
                    case "employeeUpdateEmployee" ->{
                        System.out.println("Запрос к БД на изменение сотрудника(таблица employee), сотрудник: " + clientSocket.getInetAddress().toString());
                        Employee employee = (Employee) sois.readObject();
                        System.out.println(employee.toString());
                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getEmployee().employeeUpdateEmployee(employee)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Incorrect Data");
                        }
                    }
                    case "DELETE_EMPLOYEE" -> {
                        System.out.println("Выполняется удаление сотрудника...");
                        Employee employee = (Employee) sois.readObject();
                        System.out.println(employee.toString());

                        SQLFactory sqlFactory = new SQLFactory();

                        if (sqlFactory.getEmployee().deleteEmployee(employee)) {
                            soos.writeObject("OK");
                        } else {
                            soos.writeObject("Ошибка при удалении отрудника");
                        }
                    }


                }
            }
        } catch (IOException e) {
            System.out.println("Клиент отключился или произошла ошибка ввода-вывода: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Класс не найден: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Ошибка SQL: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Ошибка при закрытии сокета: " + e.getMessage());
            }
        }
    }
}
