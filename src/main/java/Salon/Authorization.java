package Salon;


import java.io.Serializable;

public class Authorization implements Serializable {
    private String login;
    private String password;

    public Authorization() {
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toString() {
        return "Authorization{login='" + this.login + "', password='" + this.password + "'}";
    }
}


