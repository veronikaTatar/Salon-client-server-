package Salon;
import java.io.Serializable;

public class Consumer implements Serializable {
    private int id;           // Уникальный идентификатор посетителя
    private String name; // Имя посетителя
    private String surname;  // Фамилия посетителя
    private String email;     // Электронная почта посетителя
    private String login;
    private String password;

    public Consumer() {
        // Конструктор по умолчанию
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Consumer)) return false;
        Consumer caller = (Consumer) o;
        return id == caller.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Consumer{" +
                "surname='" + this.surname + '\'' +
                ", name='" + this.name + '\'' +
                ", email='" + this.email + '\'' +
                ", login='" + this.login + '\'' +
                ", password='" + this.password + '\'' + // Пароль теперь перед id
                ", id=" + this.id +
                '}';
    }



    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}