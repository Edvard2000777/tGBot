package ru.yaremchuk.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.yaremchuk.gtp.Gpt;

public class Bot extends TelegramLongPollingBot {
    private boolean gpt  = false;
    public Bot(String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return "AiBot_java_bot";
    }
    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.getText();
        System.out.println(text);

        if (message.hasText()) {

            System.out.println(message.getChat().getTitle());
            System.out.println(message.getChatId());
            System.out.println(update.getUpdateId());
            System.out.println("--------------------------");

//                if (text.equals("gpt off")) {
//                    gpt = false;
//
//                } else if (text.equals("gpt on")) {
//                    gpt = true;
//                    String response = Gpt.chatGPT("ты тут?");
//
//                    SendMessage sendMessage = new SendMessage();
//                    sendMessage.setChatId(chatId);
//                    sendMessage.setText(response);
//                    send(sendMessage);
//
//                }else if (gpt) {
              //      String response = Gpt.chatGPT("представим что ты мудрый старец под именем Леонардо и ответь на этот сообщение. " + text);
               //     String response = Gpt.chatGPT("представим что ты игривая девица под именем Катарина и ответь на этот сообщение. " + text);
                    String response = Gpt.chatGPT("Отвечай как врач дерматолог с 20 летним стажем: " + text);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText(response);
                    send(sendMessage);
              //  }
            }





      //  }


    }

    private void send(SendMessage sendMessage){
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

}
