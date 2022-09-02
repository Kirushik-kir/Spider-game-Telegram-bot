package Views;

import Models.Spider;
import Models.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

public class Buttons {

    public static InlineKeyboardMarkup questionAboutNicknameButtons(){
        return new InlineKeyboardMarkup(new InlineKeyboardButton(("Отлично")).callbackData("/main_menu"),
                new InlineKeyboardButton(("Изменить")).callbackData("/change_nickname")
        );
    }

    public static InlineKeyboardMarkup mainMenuButtons(Spider spider){
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.addRow(new InlineKeyboardButton((spider.nickname + " " + Emojies.SPIDER)).callbackData("/info"),
                new InlineKeyboardButton("Как играть " + Emojies.BOOK).callbackData("/help")
        );
        markupInline.addRow( new InlineKeyboardButton("Настройки " + Emojies.SETTINGS).callbackData("/settings"),
                new InlineKeyboardButton("Команды " + Emojies.SCROLL).callbackData("/commands")

        );
        markupInline.addRow( new InlineKeyboardButton("Поддержка " + Emojies.HELP).callbackData("/admin"),
                new InlineKeyboardButton("Помочь проекту " + Emojies.BANK_CARD).callbackData("/help_project")

        );
        return markupInline;
    }

    public static InlineKeyboardMarkup backToMainMenuButton(){
        return new InlineKeyboardMarkup(new InlineKeyboardButton("Назад").callbackData("/main_menu"));
    }

    public static InlineKeyboardMarkup backToMainMenuWithGratefulButton(){
        return new InlineKeyboardMarkup(new InlineKeyboardButton("Отлично").callbackData("/main_menu"));
    }

    public static InlineKeyboardMarkup backToStartButton(){
        return new InlineKeyboardMarkup(new InlineKeyboardButton("Назад").callbackData("/start"));
    }

    public static InlineKeyboardMarkup deathButton(){
        return new InlineKeyboardMarkup(new InlineKeyboardButton("Завести нового паучка").callbackData("/start"));
    }

    public static InlineKeyboardMarkup actionsWithSpiderButtons(User user, Statement statement) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.addRow(new InlineKeyboardButton("Покормить " + Emojies.FLY).callbackData("/feed"),
                new InlineKeyboardButton("Дать воды " + Emojies.WATER).callbackData("/water")
                );
        if (user.spider.isBuildingWeb){
            try {
                ResultSet result = statement.executeQuery("select * from users where id = " + user.id);
                result.next();
                long millis = result.getTimestamp("end_of_building").getTime() - System.currentTimeMillis();
                long hours = TimeUnit.MILLISECONDS.toHours(millis);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millis - (hours * 3600_000));
                String mins = "минут";
                String hrs = "часов";
                switch ((int) minutes % 10) {
                    case 1: {
                        mins = "минута";
                        break;
                    }
                    case 2, 3, 4: {
                        mins = "минуты";
                        break;
                    }
                }
                switch ((int) hours) {
                    case 4, 3, 2: {
                        hrs = "часа";
                        break;
                    }
                    case 1: {
                        hrs = "час";
                        break;
                    }

                }
                markupInline.addRow(new InlineKeyboardButton("Паутина строится: еще " + hours + " " + hrs + " и " + minutes + " " + mins).callbackData("/build_web"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else{
            markupInline.addRow(new InlineKeyboardButton("Отправить строить паутину " + Emojies.WEB).callbackData("/build_web"));
        }
        markupInline.addRow(new InlineKeyboardButton("Использовать очки улучшения " + Emojies.UP_ROW).callbackData("/upgrade"));
        markupInline.addRow(new InlineKeyboardButton("Назад").callbackData("/main_menu"));
        return markupInline;
    }

    public static InlineKeyboardMarkup settingsButtons(){
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.addRow(new InlineKeyboardButton(("Изменить ник")).callbackData("/change_nickname"));
        markupInline.addRow(new InlineKeyboardButton(("Назад")).callbackData("/main_menu"));
        return markupInline;
    }

    public static InlineKeyboardMarkup upgradePointsUsagesButtons(){
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.addRow(new InlineKeyboardButton("запас еды + 1 к запасу").callbackData("/upgrade_food"));
        markupInline.addRow(new InlineKeyboardButton("запас воды + 1 к запасу").callbackData("/upgrade_water"));
        markupInline.addRow(new InlineKeyboardButton("запас энергии + 1 к запасу").callbackData("/upgrade_energy"));
        markupInline.addRow(new InlineKeyboardButton("Отмена").callbackData("/main_menu"));
        return markupInline;
    }

    // на стадии разработки
    public static InlineKeyboardMarkup helpProjectButton(){
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        //markupInline.addRow(new InlineKeyboardButton("пожертвовать").pay());
        markupInline.addRow(new InlineKeyboardButton("Назад").callbackData("/main_menu"));
        return markupInline;
    }

    public static InlineKeyboardMarkup applyToUser(){
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.addRow(new InlineKeyboardButton("Ответить").callbackData("/answer"));
        return markupInline;
    }
}