package Models;

import java.sql.Timestamp;

public class Message {
    public int messageId; //ид сообщения
    public long chatId; //ид чата
    public long senderId; //ид отправителя
    public String text; //текст, отправленный пользователем ИЛИ ДАТА CallBackQuery
    public String buttonText; //необязательное поле - текст над кнопкой
    public Timestamp time; //время, когда написали это сообщение

    public Message(int messageId, long chatId, long senderId, String text, String buttonText, Timestamp time) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.text = text;
        this.buttonText = buttonText;
        this.time = time;
    }

    public Message(int messageId, long chatId, long senderId, String text, Timestamp time) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.text = text;
        this.time = time;
    }
}