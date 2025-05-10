package Salon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;  // Версия сериализации
    private int id;

    private int idService;
    private String name;
    private String type;
    private Double result_mark = 0.0;

    private String markHistory; // История оценок


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdService() {
        return idService;
    }

    public void setIdService(int idService) {
        this.idService = idService;
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

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", idService=" + idService +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", mark=" + result_mark +
                ", all marks=" + markHistory +
                '}';
    }



    public Double getResult_mark() {
        return result_mark;
    }

    public void setResult_mark(Double result_mark) {
        this.result_mark = result_mark;
    }

    public String getMarkHistory() {
        return markHistory;
    }

    public void setMarkHistory(String markHistory) {
        this.markHistory = markHistory;
    }


   /* public void addMark(int newMark, String current_markHistory) {
        System.out.println("Все оценки до добавления " + current_markHistory);
        if (markHistory == null || markHistory.isEmpty()) {
            markHistory = String.valueOf(newMark);
        } else {
            markHistory += " " + newMark; // Добавляем новую оценку

        }

        calculateAverageMark(); // Пересчитываем среднее после добавления
    }*/



    /*public void calculateAverageMark() {
        if (markHistory == null || markHistory.trim().isEmpty()) {
            result_mark = 0.0;
            return;
        }

        String[] marks = markHistory.split(" ");
        double sum = 0;
        int count = 0;//мб -1 тк по умолчанию 0

        for (String markStr : marks) {
            try {
                sum += Integer.parseInt(markStr.trim());
                count++;
            } catch (NumberFormatException e) {
                // Пропускаем некорректные значения
                System.err.println("Некорректная оценка в истории: " + markStr);
            }
        }

        result_mark = count > 0 ? sum / count : 0.0;
    }*/
}
