package ru.yaremchuk.app;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.yaremchuk.bot.Bot;


public class  App
{
    public static void main( String[] args ) {
        String token = "";

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new Bot(token));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
