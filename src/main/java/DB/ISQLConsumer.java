package DB;

import Salon.Consumer;
import Salon.Role;

import java.sql.SQLException;
import java.util.List;

public interface ISQLConsumer {
    // Получение списка всех потребителей
    List<Consumer> getAllConsumer() throws SQLException;

    // Проверка существования логина
    boolean isLoginExists(String login) throws SQLException;

    // Регистрация нового потребителя
    Role registerClient(Consumer client) throws SQLException;

    // Поиск потребителя по ID пользователя
    Consumer findConsumerByUserId(int idUser) throws SQLException;

    // Обновление данных потребителя
    boolean update(Consumer obj) throws SQLException;

    // Удаление потребителя
    boolean deleteConsumer(Consumer obj);

    // Получение экземпляра сервиса (для совместимости с текущей реализацией)
    SQLConsumer getConsumer() throws SQLException, ClassNotFoundException;
}