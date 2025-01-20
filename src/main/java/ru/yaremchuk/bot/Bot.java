package ru.yaremchuk.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.yaremchuk.entiy.DatabaseHandler;
import ru.yaremchuk.gtp.Gpt;



public class Bot extends TelegramLongPollingBot {

    public Bot(String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return "AiBot_java_bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        User user = update.getMessage().getFrom();
        String userName = user.getUserName();  // Получаем никнейм пользователя
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.getText();
        System.out.println(text);

        Long userId = message.getFrom().getId();

        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatTitle = message.getChat().getTitle(); // Может быть null для приватных чатов
            Integer updateId = update.getUpdateId();

            // Инициализация обработчика базы данных
            DatabaseHandler dbHandler = new DatabaseHandler();
            // Сохранение пользователя
            dbHandler.saveUser(userId, userName);
            // Сохранение сообщения
            dbHandler.saveMessage(userId, chatId, updateId, text);


            System.out.println("Chat Title: " + chatTitle);
            System.out.println("Chat ID: " + chatId);
            System.out.println("Update ID: " + updateId);
            System.out.println("Username: " + userName);
            System.out.println("--------------------------");
            //dbHandler.saveDialog(chatId, true, false, userName, updateId, "user", text);
            String response = Gpt.chatGPT("Respond in Russian as a dermatologist with 20 years of experience:" + text);
            dbHandler.saveChat(chatId, chatTitle, text, response);
            // Сохраняем ответ бота
            dbHandler.saveDialog(userId, chatId, false, true, userName, updateId , "bot", response);

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(response);

            send(sendMessage);
        }
    }

    private void send(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
