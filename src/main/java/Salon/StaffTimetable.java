package Salon;

import java.io.Serializable;
import java.util.Objects;

public class StaffTimetable implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int idOrder; // recordingservice
    private String nameService;
    private String lastnameEmployee;
    private String nameEmployee;
    private String time;
    private String date;
    private int lengthOfTime;

    // Конструктор по умолчанию
    public StaffTimetable() {
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(int idOrder) {
        this.idOrder = idOrder;
    }

    public String getNameService() {
        return nameService;
    }

    public void setNameService(String nameService) {
        this.nameService = nameService;
    }

    public String getLastnameEmployee() {
        return lastnameEmployee;
    }

    public void setLastnameEmployee(String lastnameEmployee) {
        this.lastnameEmployee = lastnameEmployee;
    }

    public String getNameEmployee() {
        return nameEmployee;
    }

    public void setNameEmployee(String nameEmployee) {
        this.nameEmployee = nameEmployee;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getLengthOfTime() {
        return lengthOfTime;
    }

    public void setLengthOfTime(int lengthOfTime) {
        this.lengthOfTime = lengthOfTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaffTimetable that = (StaffTimetable) o;
        return id == that.id &&
                idOrder == that.idOrder &&
                time == that.time &&
                date == that.date &&
                lengthOfTime == that.lengthOfTime &&
                Objects.equals(nameService, that.nameService) &&
                Objects.equals(lastnameEmployee, that.lastnameEmployee) &&
                Objects.equals(nameEmployee, that.nameEmployee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idOrder, nameService, lastnameEmployee,
                nameEmployee, time, date, lengthOfTime);
    }

    @Override
    public String toString() {
        return "StaffTimetable{" +
                "id=" + id +
                ", idOrder=" + idOrder +
                ", nameService='" + nameService + '\'' +
                ", employee='" + lastnameEmployee + " " + nameEmployee + '\'' +
                ", time=" + time +
                ", date=" + date +
                ", lengthOfTime=" + lengthOfTime +
                '}';
    }
}