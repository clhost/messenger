package messenger.store;


import messenger.store.datasets.User;

public interface UserStore {
    /**
     * Добавить пользователя в хранилище
     * Вернуть его же
     */
    User addUser(String login, String password, String firstname, String lastname);

    /**
     * Обновить информацию о пользователе
     */
    User updateUser(User user);

    /**
     * Получить пользователя по логину/паролю
     * return null if user not found
     */
    User getUser(String login, String pass);

    /**
     * Получить пользователя по id, например запрос информации/профиля
     * return null if user not found
     */
    User getUserById(Long id);
}
