/*
package Salon;

import java.io.Serializable;

public class Order implements Serializable {
    private int id;                  // Уникальный идентификатор заказа
    private int idUser;            // Идентификатор клиента( тк добавлять в заказ услугу может только клиент)
    private int idRecordingService;  // Идентификатор услуги
    private boolean confirmation;    // Подтверждение заказа

    public Order() {
        // Конструктор по умолчанию
    }

    public Order(int id, int idClient, int idRecordingService, boolean confirmation) {
        this.id = id;
        this.idUser = idClient;
        this.idRecordingService = idRecordingService;
        this.confirmation = confirmation;
    }

    // Getters and Setters
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUser() {
        return this.idUser;
    }

    public void setIdUser(int idClient) {
        this.idUser = idClient;
    }

    public int getIdRecordingService() {
        return this.idRecordingService;
    }

    public void setIdRecordingService(int idRecordingService) {
        this.idRecordingService = idRecordingService;
    }

    public boolean isConfirmation() {
        return this.confirmation;
    }

    public void setConfirmation(boolean confirmation) {
        this.confirmation = confirmation;
    }

    // Utility Methods for Confirmation
    public void confirmOrder() {
        this.confirmation = true;
    }

    public void cancelConfirmation() {
        this.confirmation = false;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", idUser=" + idUser +
                ", idRecordingService=" + idRecordingService +
                ", confirmation=" + confirmation +
                '}';
    }
}
*/
package Salon;

import java.io.Serializable;

public class Order implements Serializable {
    private int id;                  // Уникальный идентификатор заказа
    private int idUser;             // Идентификатор клиента
    private int idRecordingService;  // Идентификатор услуги
    private boolean confirmation;     // Подтверждение заказа
    private String name;             // Название услуги
    private String type;             // Тип услуги
    private String employeeName;     // Имя мастера
    private String time;             // Время выполнения услуги


    public Order() {
        // Конструктор по умолчанию
    }

    public Order(int id, int idUser, int idRecordingService, boolean confirmation, String name, String type, String employeeName, String time) {
        this.id = id;
        this.idUser = idUser;
        this.idRecordingService = idRecordingService;
        this.confirmation = confirmation;
        this.name = name;
        this.type = type;
        this.employeeName = employeeName;
        this.time = time;

    }

    // Getters and Setters
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUser() {
        return this.idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdRecordingService() {
        return this.idRecordingService;
    }

    public void setIdRecordingService(int idRecordingService) {
        this.idRecordingService = idRecordingService;
    }

    public boolean isConfirmation() {
        return this.confirmation;
    }

    public void setConfirmation(boolean confirmation) {
        this.confirmation = confirmation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    // Utility Methods for Confirmation
    public void confirmOrder() {
        this.confirmation = true;
    }

    public void cancelConfirmation() {
        this.confirmation = false;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", idUser=" + idUser +
                ", idRecordingService=" + idRecordingService +
                ", confirmation=" + confirmation +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}