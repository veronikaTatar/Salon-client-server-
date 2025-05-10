package test;

import DB.SQLFactory;
import Salon.Authorization;
import Salon.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationTest {

    @Test
    void TestGetRole() throws SQLException, ClassNotFoundException {
        SQLFactory sqlFactory = new SQLFactory();
        Authorization objAuthorization = new Authorization();

        objAuthorization.setLogin("a");
        objAuthorization.setPassword("1");

        Role role = sqlFactory.getRole().getRole(objAuthorization);
        Assertions.assertTrue(role.getRole().equals("student"));
    }
}
