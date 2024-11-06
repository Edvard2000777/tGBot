package ru.yaremchuk.app;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.yaremchuk.bot.Bot;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        String token = "8099031407:AAG2rqPqRzRrq0BUVO5kGEg4bHvgnF3ZTBY";

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new Bot(token));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
