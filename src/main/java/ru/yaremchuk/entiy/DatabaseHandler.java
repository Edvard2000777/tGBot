package ru.yaremchuk.entiy;

import java.sql.*;

public class DatabaseHandler {
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";  // Ваш логин PostgreSQL
    private static final String PASSWORD = "postgres"; // Ваш пароль PostgreSQL


    private Connection connection;

    public DatabaseHandler() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveUser(long telegramId, String username) {
        String insertUserQuery = "INSERT INTO users (telegram_id, username) VALUES (?, ?) "
                + "ON CONFLICT (telegram_id) DO NOTHING";

        try (PreparedStatement stmt = connection.prepareStatement(insertUserQuery)) {
            stmt.setLong(1, telegramId); // Устанавливаем telegram_id
            stmt.setString(2, username); // Устанавливаем username
            stmt.executeUpdate();
            System.out.println("User saved: " + username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveChat(long chatId, String title, String question, String answer) {
        // Проверка на дублирование по chat_id
        String checkDuplicateQuery = "SELECT COUNT(*) FROM chats WHERE chat_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkDuplicateQuery)) {
            checkStmt.setLong(1, chatId);
            ResultSet resultSet = checkStmt.executeQuery();
            resultSet.next();

            // Если чат с таким chat_id уже существует, обновляем его
            if (resultSet.getInt(1) == 0) {
                // Чат не найден, вставляем новый чат
                String sql = "INSERT INTO chats (chat_id, title, question, answer) VALUES (?, ?, ?, ?) "
                        + "ON CONFLICT (chat_id) DO NOTHING";

                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setLong(1, chatId);
                    stmt.setString(2, title);
                    stmt.setString(3, question); // Вставляем вопрос
                    stmt.setString(4, answer);   // Вставляем ответ
                    stmt.executeUpdate();
                    System.out.println("Chat saved: " + title);
                }
            } else {
                System.out.println("Chat with chat_id " + chatId + " already exists, updating question and answer...");
                // Обновляем существующий чат вопросом и ответом
                String updateQuery = "UPDATE chats SET question = ?, answer = ? WHERE chat_id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, question);
                    updateStmt.setString(2, answer);
                    updateStmt.setLong(3, chatId);
                    updateStmt.executeUpdate();
                    System.out.println("Chat updated with question and answer.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void saveMessage(long telegramId, long chatId, long updateId, String messageText) {
        // Проверка на дублирование по update_id
        String checkDuplicateQuery = "SELECT COUNT(*) FROM messages WHERE update_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkDuplicateQuery)) {
            checkStmt.setLong(1, updateId);
            ResultSet resultSet = checkStmt.executeQuery();
            resultSet.next();

            // Если сообщение с таким update_id уже существует, пропускаем вставку
            if (resultSet.getInt(1) == 0) {
                // Сообщение не найдено, вставляем новое сообщение
                String sql = "INSERT INTO messages (user_id, chat_id, update_id, message_text) \n" +
                        "VALUES ((SELECT id FROM users WHERE telegram_id = ?), \n" +
                        "(SELECT id FROM chats WHERE chat_id = ?), ?, ?)";

                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setLong(1, telegramId); // telegramId вместо userId
                    stmt.setLong(2, chatId);
                    stmt.setLong(3, updateId);
                    stmt.setString(4, messageText);
                    stmt.executeUpdate();
                    System.out.println("Message saved: " + messageText);
                }
            } else {
                System.out.println("Message with update_id " + updateId + " already exists.");
            }
        } catch (SQLException e) {
            logError("Failed to save message", e);
        }
    }


    public void saveDialog(long userId, long chatId, boolean isUserMessage, boolean isBotAI, String username, long updateId, String sender, String messageText) {
        String sql = "INSERT INTO dialog_messages (user_id, chat_id, sender, message_text, user_message, botai, username, update_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (update_id) DO NOTHING";

        System.out.println("Saving dialog: " + messageText + " | Sender: " + sender);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            // Проверяем, существует ли пользователь в таблице users перед вставкой в dialog_messages
            String checkUserQuery = "SELECT COUNT(*) FROM users WHERE id = ?";
            try (PreparedStatement checkUserStmt = connection.prepareStatement(checkUserQuery)) {
                checkUserStmt.setLong(1, userId);
                ResultSet resultSet = checkUserStmt.executeQuery();
                resultSet.next();

                if (resultSet.getInt(1) == 0) {
                    System.out.println("User not found, inserting...");
                    // Если пользователя нет, вставляем его в таблицу users с ON CONFLICT по username
                    String insertUserQuery = "INSERT INTO users (id, username) VALUES (?, ?) "
                            + "ON CONFLICT (username) DO NOTHING";  // Обрабатываем конфликт по username
                    try (PreparedStatement insertUserStmt = connection.prepareStatement(insertUserQuery)) {
                        insertUserStmt.setLong(1, userId);
                        insertUserStmt.setString(2, username); // Записываем также username, если необходимо
                        insertUserStmt.executeUpdate();
                    }
                } else {
                    System.out.println("User found, skipping insert.");
                }
            }

            // Проверка наличия чата
            String checkChatQuery = "SELECT COUNT(*) FROM chats WHERE chat_id = ?";
            try (PreparedStatement checkChatStmt = connection.prepareStatement(checkChatQuery)) {
                checkChatStmt.setLong(1, chatId);
                ResultSet chatResultSet = checkChatStmt.executeQuery();
                chatResultSet.next();

                if (chatResultSet.getInt(1) == 0) {
                    System.out.println("Chat not found, inserting...");
                    // Если чата нет, вставляем его в таблицу chats с ON CONFLICT
                    String insertChatQuery = "INSERT INTO chats (chat_id, title) VALUES (?, ?) ON CONFLICT (chat_id) DO NOTHING";
                    try (PreparedStatement insertChatStmt = connection.prepareStatement(insertChatQuery)) {
                        insertChatStmt.setLong(1, chatId);
                        insertChatStmt.setString(2, "Default Chat Title");  // Или можно передавать название чата, если оно доступно
                        insertChatStmt.executeUpdate();
                    }
                } else {
                    System.out.println("Chat found, skipping insert.");
                }
            }

            // Вставляем сообщение в dialog_messages
            statement.setLong(1, userId);         // ID пользователя
            statement.setLong(2, chatId);         // ID чата
            statement.setString(3, sender);       // Отправитель ('user' или 'bot')
            statement.setString(4, messageText);  // Текст сообщения
            statement.setBoolean(5, isUserMessage); // true, если сообщение от пользователя
            statement.setBoolean(6, isBotAI);     // true, если сообщение связано с ботом
            statement.setString(7, username);     // Имя пользователя
            statement.setLong(8, updateId);       // Уникальный ID обновления

            int rowsAffected = statement.executeUpdate();  // Вставляем запись в таблицу dialog_messages
            if (rowsAffected > 0) {
                System.out.println("Dialog saved successfully.");
            } else {
                System.out.println("No rows affected, possible conflict or no data inserted.");
            }
        } catch (SQLException e) {
            System.out.println("Error saving dialog: " + e.getMessage());
            e.printStackTrace();  // Логирование ошибок
        }
    }
    private void logError(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace();
    }

}
