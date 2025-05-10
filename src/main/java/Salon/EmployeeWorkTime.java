package Salon;

public class EmployeeWorkTime implements java.io.Serializable {
    private String fio;
    private double totalMinutes;

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public double getTotalHours() {
        return totalMinutes;
    }

    public void setTotalHours(double totalMinutes) {
        this.totalMinutes = totalMinutes;
    }
    @Override
    public String toString() {
        return "EmployeeWorkTime{" +
                "fio='" + fio + '\'' +
                ", totalMinutes=" + totalMinutes +
                '}';
    }
}
