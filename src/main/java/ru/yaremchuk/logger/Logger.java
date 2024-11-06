package ru.yaremchuk.logger;

import org.telegram.telegrambots.meta.api.objects.Message;

public class Logger {
    public static void printLogg(Message message){
        System.out.println(message.getChatId());
        System.out.println();
        System.out.println();
    }

    public static void main(String[] args) {
        String s = "       },      \"logprobs\": null,      \"finish_reason\": \"stop\"    }  ],  \"usage\": {    \"prompt_tokens\": 32,    \"completion_tokens\": 280,    \"total_tokens\": 312  },  \"system_fingerprint\": null";
        System.out.println(s.length());
    }
}
