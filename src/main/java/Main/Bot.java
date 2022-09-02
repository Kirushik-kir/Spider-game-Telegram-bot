package Main;

import Models.Message;
import Models.Spider;
import Views.Stickers;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import Models.User;
import com.pengrad.telegrambot.request.SendSticker;

import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static Views.Buttons.*;
import static Views.Texts.*;

//TODO сделать топ игроков посередине. Чтобы можно было увидеть себя и своих соседей
//TODO сделать функцию пожертвования
public class Bot {

    private static final String TOKEN = "";
    private static final String URL = "jdbc:mysql://localhost/telegram_users?serverTimezone=Europe/Moscow&useSSL=false&useUnicode=true&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final long ADMIN = 0;
    private static User user;
    private static Message message;

    private static final TelegramBot bot = new TelegramBot(TOKEN); //регаем бота

    private static Statement statement;
    private static Statement resourcesStatement;
    private static Statement webStatement;
    private static Statement flyStatement;
    private static Statement experienceStatement;

    //открываем бд
    static {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            Connection resourcesConnection = DriverManager.getConnection(URL, USER, PASSWORD);
            Connection webConnection = DriverManager.getConnection(URL, USER, PASSWORD);
            Connection flyConnection = DriverManager.getConnection(URL, USER, PASSWORD);
            Connection experienceConnection = DriverManager.getConnection(URL, USER, PASSWORD);

            statement = connection.createStatement();
            resourcesStatement = resourcesConnection.createStatement();
            webStatement = webConnection.createStatement();
            flyStatement = flyConnection.createStatement();
            experienceStatement = experienceConnection.createStatement();

            System.out.println("Подключение прошло успешно");
        } catch (SQLException e) {
            System.out.println("Подключение не удалось");
            bot.execute(new SendMessage(ADMIN,"Ошибка подключения к базе данных"));
            e.printStackTrace();
        }
    }

    // Класс для постоянного лишения ресурсов пользователей
    private static class ResourcesCounter extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    reduceFood(resourcesStatement);
                    reduceWater(resourcesStatement);
                    addEnergy(resourcesStatement);
                    try {
                        Thread.sleep(900_000); // 15 минут
                    } catch (InterruptedException e) {
                        bot.execute(new SendMessage(ADMIN, "Ошибка остановки таймера после убавления воды - InterruptedException"));
                        e.printStackTrace();
                    }
                    reduceWater(resourcesStatement);
                    addEnergy(resourcesStatement);
                    try {
                        Thread.sleep(900_000); // 15 минут
                    } catch (InterruptedException e) {
                        bot.execute(new SendMessage(ADMIN, "Ошибка остановки таймера для класса с убавлением еды - InterruptedException"));
                        e.printStackTrace();
                    }
                    reduceFood(resourcesStatement);
                    reduceWater(resourcesStatement);
                    addEnergy(resourcesStatement);
                }
            } catch(SQLException e){
                    bot.execute(new SendMessage(ADMIN, "Ошибка с потоком ресурсов - SQLException"));
                    e.printStackTrace();
            }
        }
    }

    // Класс для постоянной проверки паука на постройку паутины
    // За одну сделанную паутину дается 15 опыта
    private static class WebBuilding extends Thread{
        @Override
        public void run() {
            try {
                while (true) {
                    Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                    ResultSet result = webStatement.executeQuery("select * from users where is_dead = 0 and is_building_web = 1 and end_of_building <= '" + currentTime + "'");
                    while (result.next()){
                        long id = result.getLong("id");
                        int level = result.getInt("level");
                        int experience = result.getInt("experience");
                        String nickname = result.getString("nickname");
                        experienceStatement.executeUpdate("update users set end_of_building = null, is_building_web = 0, webs = webs + 1, energy = energy - 40, experience = experience + 10 where id = " + id + " and is_dead = 0");
                        experience += 10;
                        if(level * 100 <= experience){
                            experienceStatement.executeUpdate("update users set level = level + 1, experience = " + (experience - (level * 100)) + ", upgrade_points = upgrade_points + level where id = " + id + " and is_dead = 0");
                            bot.execute(new SendMessage(id, nickname + " достроил паутину! \nУровень был повышен!").replyMarkup(backToMainMenuWithGratefulButton()));
                        }
                        else {
                            bot.execute(new SendMessage(id, nickname + " достроил паутину!").replyMarkup(backToMainMenuWithGratefulButton()));
                        }
                    }
                    try {
                        Thread.sleep(60_000);
                    } catch (InterruptedException e) {
                        bot.execute(new SendMessage(ADMIN, "Ошибка остановки таймера для класса с проверкой готовности паутины - InterruptedException"));
                        e.printStackTrace();
                    }

                }

            } catch(SQLException e){
                bot.execute(new SendMessage(ADMIN, "Ошибка с потоком для проверки паутин на готовность - SQLException"));
                e.printStackTrace();
            }
        }
    }

    // Класс для поимки мух и ломания паутин
    // За одну пойманную муху дается 5 опыта
    private static class WebHandler extends Thread{
        @Override
        public void run() {
            try {
                while (true) {
                    ResultSet result = flyStatement.executeQuery("select * from users where webs > 0 && is_dead = 0");
                    while (result.next()){

                        long id = result.getLong("id");
                        int level = result.getInt("level");
                        int experience = result.getInt("experience");
                        String nickname = result.getString("nickname");

                        for (int i = 0; i < result.getInt("webs"); i++) {

                            if (((int) (Math.random() * 200) + 1) > 199) { //0.5% на 5 минут, что паук поймает муху
                                experienceStatement.executeUpdate("update users set flies = flies + 1, experience = experience + 5 where id = " + id + " and is_dead = 0");
                                experience += 5;
                                if (level * 100 <= experience){
                                    experienceStatement.executeUpdate("update users set level = level + 1, experience = " + (experience - (level * 100)) + ", upgrade_points = upgrade_points + level where id = " + id + " and is_dead = 0");
                                    bot.execute(new SendMessage(id, nickname + " поймал муху! \nУровень был повышен!").replyMarkup(backToMainMenuWithGratefulButton()));
                                }
                                else {
                                    bot.execute(new SendMessage(id, nickname + " поймал муху!").replyMarkup(backToMainMenuWithGratefulButton()));
                                    System.out.println("Муха была поймана пользователем " + id);
                                }
                            }

                            if (((int) (Math.random() * 400) + 1) > 399) { //0.25% на 5 минут, что паутина сломается у паука
                                experienceStatement.executeUpdate("update users set webs = webs - 1 where id = " + id + " and is_dead = 0");
                                bot.execute(new SendMessage(id, "У вас порвалась паутина").replyMarkup(backToMainMenuButton()));
                                System.out.println("Паутина была сломана у пользователя " + id);
                            }
                        }
                    }
                    try {
                        Thread.sleep(300_000);
                    } catch (InterruptedException e) {
                        bot.execute(new SendMessage(ADMIN, "Ошибка остановки таймера для класса с поимкой мух и ломанием паутин - InterruptedException"));
                        e.printStackTrace();
                    }

                }

            } catch(SQLException e){
                bot.execute(new SendMessage(ADMIN, "Ошибка с потоком для ловли мух и ломания паутин - SQLException"));
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        new ResourcesCounter().start();
        new WebBuilding().start();
        new WebHandler().start();

        Statement finalStatement = statement;

        boolean[] isCommand = new boolean[1]; // Нужно, чтобы узнавать, является ли сообщение базовой командой
        //                                    - или же это неизвестный текст. Используется для установки ника
        //                                    - чтобы избежать базовых команд в нике.
        isCommand[0] = true;

        ArrayList<Long> isWaitingForName = new ArrayList<>(); // Нужно, чтобы узнавать, нужен ли ник пользователю -
        //                                                     - значение меняется только вовремя присвоения и изменения ника.

        ArrayList<Long> isWaitingForHelp = new ArrayList<>(); // Нужно, чтобы узнавать нужна ли пользователю помощь.

        //for admins:
        boolean[] isGoingToApply = new boolean[1];
        isGoingToApply[0] = false;

        long[] waiterForAnswer = new long[1];

        bot.setUpdatesListener(updates -> {
            updates.forEach(System.out::println);
            updates.forEach(update -> {

                isCommand[0] = true;

                if (update.message() == null) {
                    if (update.callbackQuery() != null) {
                        message = new Message(update.callbackQuery().message().messageId(),
                                update.callbackQuery().message().chat().id(),
                                update.callbackQuery().from().id(),
                                update.callbackQuery().data(),
                                update.callbackQuery().message().text(),
                                new Timestamp((long) update.callbackQuery().message().date() * 1000));
                    }
                    System.out.println(update.callbackQuery().data());
                    System.out.println(update.callbackQuery().message().text());
                } else {
                    message = new Message(update.message().messageId(),
                            update.message().chat().id(),
                            update.message().from().id(),
                            update.message().text(),
                            new Timestamp((long) update.message().date() * 1000));
                }

                if (message != null){

                    if (!isInDataBase(message.senderId, finalStatement)) {
                        System.out.println(message.senderId);
                        if (update.message() != null) {
                            user = new User(message.senderId,
                                    update.message().chat().firstName(),
                                    update.message().chat().lastName(),
                                    update.message().chat().username(),
                                    new Spider()
                            );
                        }
                        else {
                            user = new User(message.senderId,
                                    update.callbackQuery().from().firstName(),
                                    update.callbackQuery().from().lastName(),
                                    update.callbackQuery().from().username(),
                                    new Spider()
                                    );
                        }
                        addPersonToDataBase(user, finalStatement);
                        System.out.println("пользователь был добавлен " + user.id);
                    } else {
                        user = findPerson(message.senderId, finalStatement);
                        System.out.println("он уже в базе");
                    }

                //здесь будем обрабатывать базовые команды
                if (message.text != null) {
                    switch (message.text.toLowerCase(Locale.ROOT)) {
                        case ("/start"): {
                            bot.execute(new SendSticker(message.chatId, Stickers.HELLO.toString()));

                            if (user.spider.registrationTime == null) {
                                isWaitingForName.add(user.id);
                                bot.execute(new SendMessage(message.chatId, notAuthorizedStartText()));
                            } else
                                bot.execute(new SendMessage(message.chatId, authorizedStartText(user.spider)).replyMarkup(mainMenuButtons(user.spider)));
                            break;
                        }
                        case ("/settings"), ("настройки"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            if (user.spider.registrationTime != null) {
                                bot.execute(new SendSticker(message.chatId, Stickers.ENGINEER.toString()));
                                bot.execute(new SendMessage(message.chatId, settingsText()).replyMarkup(settingsButtons()));
                            }
                            else {
                                bot.execute(new SendSticker(message.chatId, Stickers.ANGRY.toString()));
                                bot.execute(new SendMessage(message.chatId, withoutNicknameText()).replyMarkup(backToStartButton()));
                            }
                            break;
                        }
                        case ("/help"), ("как играть"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            bot.execute(new SendSticker(message.chatId, Stickers.BOOK.toString()));
                            bot.execute(new SendMessage(message.chatId, helpText()).replyMarkup(backToMainMenuButton()));
                            break;
                        }
                        case ("/main_menu"), ("главное меню"), ("назад"), ("отлично"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            if (user.spider.registrationTime != null) {
                                try {
                                    bot.execute(new SendSticker(message.chatId, Stickers.SHOWER.toString()));
                                    bot.execute(new SendMessage(message.chatId, mainMenuText(getTopUsers(finalStatement))).replyMarkup(mainMenuButtons(user.spider)));
                                }
                                catch (SQLException e){
                                    bot.execute(new SendMessage(message.chatId, "Не получилось отобразить топ игроков.\nОбратитесь в поддержку."));
                                    bot.execute(new SendMessage(ADMIN, "Не получилось отобразить топ игроков"));
                                }
                        }
                            else {
                                bot.execute(new SendSticker(message.chatId, Stickers.ANGRY.toString()));
                                bot.execute(new SendMessage(message.chatId, withoutNicknameText()).replyMarkup(backToStartButton()));
                        }
                            break;
                        }
                        case ("/change_nickname"), ("изменить ник"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            if (user.spider.registrationTime != null) {
                            isWaitingForName.add(user.id);
                                bot.execute(new SendSticker(message.chatId, Stickers.WTF.toString()));
                                bot.execute(new SendMessage(message.chatId, changeNicknameText(user.spider)).replyMarkup(backToMainMenuButton()));
                        }
                            else {
                                bot.execute(new SendSticker(message.chatId, Stickers.ANGRY.toString()));
                                bot.execute(new SendMessage(message.chatId, withoutNicknameText()).replyMarkup(backToStartButton()));
                        }
                            break;
                        }
                        case ("/upgrade"), ("использовать очки улучшения"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            if (user.spider.registrationTime != null) {

                                if (user.spider.upgradePoints == 0){
                                    bot.execute(new SendSticker(message.chatId, Stickers.CRY.toString()));
                                    bot.execute(new SendMessage(message.chatId, upgradePointsUsagesText(user.spider)).replyMarkup(backToMainMenuButton()));
                                }
                                else {
                                    bot.execute(new SendSticker(message.chatId, Stickers.HAPPY.toString()));
                                    bot.execute(new SendMessage(message.chatId, upgradePointsUsagesText(user.spider)).replyMarkup(upgradePointsUsagesButtons()));
                                }
                        }
                            else {
                                bot.execute(new SendSticker(message.chatId, Stickers.ANGRY.toString()));
                                bot.execute(new SendMessage(message.chatId, withoutNicknameText()).replyMarkup(backToStartButton()));
                            }
                            break;
                        }
                        case ("/upgrade_food"), ("запас еды + 1"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            if (user.spider.registrationTime != null) {
                                bot.execute(new SendSticker(message.chatId, Stickers.HAPPY.toString()));
                                bot.execute(new SendMessage(message.chatId, giveSpiderBetterFood(user, finalStatement)).replyMarkup(upgradePointsUsagesButtons()));
                        }
                            else {
                                bot.execute(new SendSticker(message.chatId, Stickers.ANGRY.toString()));
                                bot.execute(new SendMessage(message.chatId, withoutNicknameText()).replyMarkup(backToStartButton()));
                            }
                            break;
                        }
                        case ("/upgrade_water"), ("запас воды + 1"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            if (user.spider.registrationTime != null) {
                                bot.execute(new SendSticker(message.chatId, Stickers.HAPPY.toString()));
                                bot.execute(new SendMessage(message.chatId, giveSpiderBetterWater(user, finalStatement)).replyMarkup(upgradePointsUsagesButtons()));
                        }
                            else {
                                bot.execute(new SendSticker(message.chatId, Stickers.ANGRY.toString()));
                                bot.execute(new SendMessage(message.chatId, withoutNicknameText()).replyMarkup(backToStartButton()));
                            }
                            break;
                        }
                        case ("/upgrade_energy"), ("запас энергии + 1"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            if (user.spider.registrationTime != null) {
                                bot.execute(new SendSticker(message.chatId, Stickers.HAPPY.toString()));
                                bot.execute(new SendMessage(message.chatId, giveSpiderBetterEnergy(user, finalStatement)).replyMarkup(upgradePointsUsagesButtons()));
                        }
                            else {
                                bot.execute(new SendSticker(message.chatId, Stickers.ANGRY.toString()));
                                bot.execute(new SendMessage(message.chatId, withoutNicknameText()).replyMarkup(backToStartButton()));
                            }
                            break;
                        }
                        case ("/info"), ("паучок"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            if (user.spider.registrationTime != null) {
                                bot.execute(new SendSticker(message.chatId, Stickers.MONDAY.toString()));
                                bot.execute(new SendMessage(message.chatId, infoText(user.spider)).replyMarkup(actionsWithSpiderButtons(user, finalStatement)));
                        }
                            else {
                                bot.execute(new SendSticker(message.chatId, Stickers.ANGRY.toString()));
                                bot.execute(new SendMessage(message.chatId, withoutNicknameText()).replyMarkup(backToStartButton()));
                        }
                            break;
                        }
                        case ("/commands"), ("команды"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            bot.execute(new SendSticker(message.chatId, Stickers.FIRE.toString()));
                            bot.execute(new SendMessage(message.chatId, commandsText()).replyMarkup(backToMainMenuButton()));
                            break;
                        }
                        case ("/feed"), ("покормить"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            if (user.spider.registrationTime != null) {
                                bot.execute(new SendSticker(message.chatId, Stickers.IS_IT_FOR_ME.toString()));
                                bot.execute(new SendMessage(message.chatId, feedSpider(user, finalStatement)).replyMarkup(actionsWithSpiderButtons(user, finalStatement)));
                        }
                            else {
                                bot.execute(new SendSticker(message.chatId, Stickers.ANGRY.toString()));
                                bot.execute(new SendMessage(message.chatId, withoutNicknameText()).replyMarkup(backToStartButton()));
                        }
                            break;
                        }
                        case ("/water"), ("дать воды"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            if (user.spider.registrationTime != null) {
                                bot.execute(new SendSticker(message.chatId, Stickers.IS_IT_FOR_ME.toString()));
                                bot.execute(new SendMessage(message.chatId, giveSpiderDrink(user, statement)).replyMarkup(actionsWithSpiderButtons(user, finalStatement)));
                        }
                            else {
                                bot.execute(new SendSticker(message.chatId, Stickers.ANGRY.toString()));
                                bot.execute(new SendMessage(message.chatId, withoutNicknameText()).replyMarkup(backToStartButton()));
                        }
                            break;
                        }
                        case ("/build_web"), ("отправить строить паутину"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            if (user.spider.registrationTime != null) {
                                bot.execute(new SendSticker(message.chatId, Stickers.MONDAY.toString()));
                                bot.execute(new SendMessage(message.chatId, buildWeb(user, message, finalStatement)).replyMarkup(actionsWithSpiderButtons(user, finalStatement)));
                        }
                            else {
                                bot.execute(new SendSticker(message.chatId, Stickers.ANGRY.toString()));
                                bot.execute(new SendMessage(message.chatId, withoutNicknameText()).replyMarkup(backToStartButton()));
                        }
                            break;
                        }

                        case ("/admin"), ("поддержка"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            isWaitingForHelp.add(user.id);
                            bot.execute(new SendSticker(message.chatId, Stickers.DOCTOR_DEATH.toString()));
                            bot.execute(new SendMessage(message.chatId, askAdminHelpText()).replyMarkup(backToMainMenuButton()));
                            break;
                        }
                        case ("/help_project"), ("помочь проекту"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }
                            bot.execute(new SendSticker(message.chatId, Stickers.HAPPY.toString()));
                            bot.execute(new SendMessage(message.chatId, helpProjectText()).replyMarkup(helpProjectButton()));
                            break;
                        }
                        case ("/answer"): {
                            if (message.buttonText != null) {
                                deleteCurrentMessageWithSticker();
                            }

                            if (user.id == ADMIN){
                                isGoingToApply[0] = true;
                                System.out.println(message.buttonText);
                                //waiterForAnswer[0] = Long.parseLong(message.buttonText.replace("Новое сообщение от пользователя ", "").substring(message.buttonText.indexOf(" ")));
                                String[] buffer = message.buttonText.split(" ");
                                waiterForAnswer[0] = Long.parseLong(buffer[4]);
                                bot.execute(new SendSticker(message.chatId, Stickers.MONDAY.toString()));
                                bot.execute(new SendMessage(message.chatId, "Ответ пользователю " + waiterForAnswer[0]));
                            }
                            else {
                                bot.execute(new SendSticker(message.chatId, Stickers.BAN.toString()));
                                bot.execute(new SendMessage(message.chatId, "Эта команда только для админа").replyMarkup(backToMainMenuButton()));
                            }
                            break;
                        }
                        default: {
                            isCommand[0] = false;
                        }
                    }
                }


                    if (!isCommand[0]) {

                        //добавляем ник пользователя и время его первой регистрации в бд или изменяем этот ник на новый
                        if (isWaitingForName.contains(user.id)) {
                            try {
                                if (user.spider.nickname == null) {
                                    String s = user.spider.setNickname(message.text);
                                    bot.execute(new SendMessage(message.chatId, s).replyMarkup(questionAboutNicknameButtons()));
                                    if (s.equals("Ваш ник теперь: " + user.spider.nickname)) {
                                        // Дефолтные значения бд (чтобы не путаться)
                                        user.spider.level = 1;
                                        user.spider.experience = 0;
                                        user.spider.registrationTime = message.time;

                                        user.spider.energy = 100;
                                        user.spider.maxEnergy = 100;
                                        user.spider.food = 100;
                                        user.spider.maxFood = 100;
                                        user.spider.water = 100;
                                        user.spider.maxWater = 100;

                                        user.spider.flies = 3;
                                        user.spider.isBuildingWeb = false;
                                        user.spider.endOfBuilding = null;
                                        user.spider.webs = 1;

                                        finalStatement.executeUpdate("update users set nickname = '" + user.spider.nickname + "', registration_time = '" + message.time + "' where id = " + user.id + " and is_dead = 0");
                                        isWaitingForName.remove(user.id);
                                        System.out.println("Ник был добавлен " + user.spider.nickname);
                                    }
                                } else {
                                    String s = user.spider.setNickname(message.text);
                                    bot.execute(new SendMessage(message.chatId, s).replyMarkup(questionAboutNicknameButtons()));
                                    if (s.equals("Ваш ник теперь: " + user.spider.nickname)) {
                                        finalStatement.executeUpdate("update users set nickname = '" + user.spider.nickname + "' where id = " + user.id + " and is_dead = 0");
                                        isWaitingForName.remove(user.id);
                                        System.out.println("Ник был изменен " + user.spider.nickname);
                                    }
                                }
                            } catch (SQLException e) {
                                bot.execute(new SendMessage(ADMIN, "Ошибка с изменением ника - SQLException"));
                                e.printStackTrace();
                            }
                        }

                        // проверяет, нужна ли помощь пользователю
                        if (isWaitingForHelp.contains(user.id)) {
                            bot.execute(new SendSticker(ADMIN, Stickers.DOCTOR_DEATH.toString()));
                            bot.execute(new SendMessage(ADMIN, toAdminText(user, message.text)).replyMarkup(applyToUser()));
                            isWaitingForHelp.remove(user.id);
                            bot.execute(new SendSticker(message.chatId, Stickers.DOCTOR_DEATH.toString()));
                            bot.execute(new SendMessage(message.chatId, adminResponseAboutHelpText()).replyMarkup(backToMainMenuWithGratefulButton()));
                        }

                        // проверяет админа
                        if (user.id == ADMIN){

                            if (message.text.startsWith("Джарвис")) {
                                bot.execute(new SendMessage(message.chatId, "Чем могу помочь, сэр?"));
                            }

                            if (isGoingToApply[0]){
                                isGoingToApply[0] = false;
                                bot.execute(new SendSticker(waiterForAnswer[0], Stickers.DOCTOR_DEATH.toString()));
                                bot.execute(new SendMessage(waiterForAnswer[0], message.text).replyMarkup(backToMainMenuWithGratefulButton()));
                            }
                        }
                    }

                }

            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

    }

    //удаляет сообщение если на него пришли по кнопкам
    private static void deleteCurrentMessageWithSticker(){
        bot.execute(new DeleteMessage(message.chatId, message.messageId - 1));
        bot.execute(new DeleteMessage(message.chatId, message.messageId));
    }

    //выдает топ-5 игроков
    private static ResultSet getTopUsers(Statement statement) throws SQLException{
        return statement.executeQuery("select * from users where is_dead = 0 order by level desc, experience desc limit 5");
    }

    //проверяет наличие пользователя по айдишнику
    private static boolean isInDataBase(long id, Statement statement){
        try {
            return statement.executeQuery("select * from users where id = " + id + " and is_dead = 0").next();
        } catch (SQLException e) {
            bot.execute(new SendMessage(ADMIN, "Ошибка в методе проверки наличия пользователя в базе - SQLException"));
            e.printStackTrace();
            return false;
        }
    }

    //добавляет пользователя в бд
    private static void addPersonToDataBase(User user, Statement statement){
        try {
            int row = statement.executeUpdate("INSERT users(id, first_name, last_name, username, is_premium) " +
                    "VALUES (" + user.id + ", '" + user.first_name + "', '" + user.last_name + "', '" + user.username + "', 0)");
            System.out.println("мы добавили " + row + " пользователей");
        } catch (SQLException e) {
            bot.execute(new SendMessage(ADMIN, "Ошибка в методе добавления пользователя в базу - SQLException"));
            e.printStackTrace();
        }
    }

    //ищет пользователя в бд и возвращает его
    public static User findPerson(long id, Statement statement) {
        try {
            ResultSet result = statement.executeQuery("select * from users where id = " + id + " and is_dead = 0");
            result.next();
            return new User(id, result.getString("first_name"), result.getString("last_name"), result.getString("username"),
                    new Spider(result.getString("nickname"), result.getInt("level"),
                            result.getInt("experience"), result.getTimestamp("registration_time"),
                            result.getInt("energy"), result.getInt("max_energy"),
                            result.getInt("food"), result.getInt("max_food"),
                            result.getInt("water"), result.getInt("max_water"),
                            result.getInt("upgrade_points"), result.getBoolean("is_dead"),
                            result.getInt("flies"), result.getBoolean("is_building_web"),
                            result.getTimestamp("end_of_building"), result.getInt("webs")
                            )
            );
        } catch (SQLException e) {
            bot.execute(new SendMessage(ADMIN, "Ошибка в методе с поиском человека в базе данных - SQLException"));
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    //метод кормления паука
    public static String feedSpider(User user, Statement statement){
        try {
            if (user.spider.food != 100) {
                if (user.spider.flies > 0) {
                    int food = user.spider.food;
                    int energy = user.spider.energy;
                    user.spider.flies--;
                    food = food < 80 ? food + 20 : user.spider.maxFood;
                    energy = energy < 80 ? energy + 20 : user.spider.maxEnergy;
                    statement.executeUpdate("update users set flies = flies - 1 where id = " + user.id  + " and is_dead = 0");
                    statement.executeUpdate("update users set food = " + food + ", energy = " + energy + " where id = " + user.id  + " and is_dead = 0");
                    return user.spider.nickname + " покормлен.\n" +
                            "Уровень его сытости: " + food;
                } else return "Извините, у вас недостаточно мух, чтобы покормить вашего паучка. Дождитесь, пока муха попадет в паутину";
            }
            else return user.spider.nickname + " полностью сыт.\n" +
                    "Нет смысла кормить его сейчас";

        } catch (SQLException e) {
            bot.execute(new SendMessage(ADMIN, "Ошибка в методе с кормлением паука - SQLException"));
            e.printStackTrace();
        }
        return "Если вы увидели это сообщение, пожалуйста, сообщите в поддержку";
    }

    //метод поения паука
    public static String giveSpiderDrink(User user, Statement statement){
        try {
            if (user.spider.water != user.spider.maxWater) {
                    statement.executeUpdate("update users set water = " + user.spider.maxWater + " where id = " + user.id  + " and is_dead = 0");
                    return user.spider.nickname + " напоен.\n" +
                            "Уровень его воды: " + user.spider.maxWater;
            }
            else return user.spider.nickname + " не хочет сейчас пить.\n";

        } catch (SQLException e) {
            bot.execute(new SendMessage(ADMIN, "Ошибка в методе с поением паука - SQLException"));
            e.printStackTrace();
        }
        return "Если вы увидели это сообщение, пожалуйста, сообщите в поддержку";
    }

    //метод для улучшения максимального запаса еды
    public static String giveSpiderBetterFood(User user, Statement statement){
        try {
            if (user.spider.upgradePoints != 0) {
                    statement.executeUpdate("update users set max_food = max_food + 1, upgrade_points = upgrade_points - 1 where id = " + user.id + " and is_dead = 0");
                    user.spider.maxFood += 1;
                    return "Теперь максимальный запас еды = " + (user.spider.maxFood);
            }
            else return "У вас не хватает очков улучшения";

        } catch (SQLException e) {
            bot.execute(new SendMessage(ADMIN, "Ошибка в методе с повышением максимального запаса еды паука - SQLException"));
            e.printStackTrace();
        }
        return "Если вы увидели это сообщение, пожалуйста, сообщите в поддержку";
    }

    //метод для улучшения максимального запаса воды
    public static String giveSpiderBetterWater(User user, Statement statement){
        try {
            if (user.spider.upgradePoints != 0) {
                    statement.executeUpdate("update users set max_water = max_water + 1, upgrade_points = upgrade_points - 1 where id = " + user.id  + " and is_dead = 0");
                    user.spider.maxWater += 1;
                    return "Теперь максимальный запас воды = " + (user.spider.maxWater);
            }
            else return "У вас не хватает очков улучшения";

        } catch (SQLException e) {
            bot.execute(new SendMessage(ADMIN, "Ошибка в методе с повышением максимального запаса воды паука - SQLException"));
            e.printStackTrace();
        }
        return "Если вы увидели это сообщение, пожалуйста, сообщите в поддержку";
    }

    //метод для улучшения максимального запаса энергии
    public static String giveSpiderBetterEnergy(User user, Statement statement){
        try {
            if (user.spider.upgradePoints != 0) {
                    statement.executeUpdate("update users set max_energy = max_energy + 1, upgrade_points = upgrade_points - 1 where id = " + user.id + " and is_dead = 0");
                    user.spider.maxEnergy += 1;
                    return "Теперь максимальный запас энергии = " + (user.spider.maxEnergy);
            }
            else return "У вас не хватает очков улучшения";

        } catch (SQLException e) {
            bot.execute(new SendMessage(ADMIN, "Ошибка в методе с повышением максимального запаса энергии паука - SQLException"));
            e.printStackTrace();
        }
        return "Если вы увидели это сообщение, пожалуйста, сообщите в поддержку";
    }

    //метод отправления паука строить новую паутину
    public static String buildWeb(User user, Message message, Statement statement){
        try {
            if (!user.spider.isBuildingWeb) {
                if (user.spider.energy >= 40) {
                    statement.executeUpdate("update users set is_building_web = 1 where id = " + user.id + " and is_dead = 0");
                    System.out.println(new Timestamp(message.time.getTime() + 14400_000));
                    statement.executeUpdate("update users set end_of_building = '" + new Timestamp(message.time.getTime() + 14400_000) + "' where id = " + user.id + " and is_dead = 0");
                    System.out.println("+ 1 паук строит паутину");
                    return user.spider.nickname + " уполз плести паутину";
                }
                else return "У вас недостаточно энергии, чтобы построить новую паутину.\n" +
                        "Требуется 40 энергии для этого";
            }
            else {
                ResultSet result = statement.executeQuery("select * from users where id = " + user.id + " and is_dead = 0");
                result.next();
                long millis = result.getTimestamp("end_of_building").getTime() - System.currentTimeMillis();
                long hours = TimeUnit.MILLISECONDS.toHours(millis);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millis - (hours * 3600_000));
                String mins = "минут";
                switch ((int) minutes % 10){
                    case 1 :{
                        mins = "минута";
                        break;
                    }
                    case 2, 3, 4 : {
                        mins = "минуты";
                        break;
                    }
                }
                switch ( (int) hours){
                    case 4, 3, 2 : return user.spider.nickname + " и так сейчас плетет паутину." + "\n" +
                                "Вы не можете управлять им, пока он не закончит" + "\n" +
                                "Осталось " + hours + " часа и " + minutes + " " + mins;

                    case 1 : return user.spider.nickname + " и так сейчас плетет паутину." + "\n" +
                            "Вы не можете управлять им, пока он не закончит" + "\n" +
                            "Осталось " + hours + " час и " + minutes + " " + mins;

                    case 0 : return user.spider.nickname + " и так сейчас плетет паутину." + "\n" +
                            "Вы не можете управлять им, пока он не закончит" + "\n" +
                            "Осталось " + minutes + " " + mins;
                }

            }
        } catch (SQLException e) {
            bot.execute(new SendMessage(ADMIN, "Ошибка в методе с отправкой паука строить паутину - SQLException"));
            e.printStackTrace();
        }
        return "Если вы увидели это сообщение, пожалуйста, сообщите в поддержку";
    }

    // метод для постоянного уменьшения воды у пользователей
    private static void reduceWater(Statement statement) throws SQLException{
        ResultSet result = statement.executeQuery("select * from users where water = 1");
        while (result.next()){
            long id = result.getLong("id");
            String nickname = result.getString("nickname");
            bot.execute(new SendMessage(id, thirstyDeathText(nickname)).replyMarkup(deathButton()));
        }

        statement.executeUpdate("update users set is_dead = 1, water = 0, food = 0, energy = 0 where water = 1");
        statement.executeUpdate("update users set water = water - 1 where registration_time is not null && is_dead = 0");
        result = statement.executeQuery("select * from users where water = 10 or water = 30 or water = 50 or water = 70");
            while (result.next()){
                long id = result.getLong("id");
                String nickname = result.getString("nickname");
                bot.execute(new SendSticker(id, Stickers.FOOD.toString()));
                bot.execute(new SendMessage(id, nickname + " хочет пить"));
            }
        System.out.println("-вода");
    }


    // метод для постоянного уменьшения еды у пользователей
    private static void reduceFood(Statement statement) throws SQLException{
        ResultSet result = statement.executeQuery("select * from users where food = 1");
        while (result.next()){
            long id = result.getLong("id");
            String nickname = result.getString("nickname");
            bot.execute(new SendMessage(id, hungryDeathText(nickname)).replyMarkup(deathButton()));
        }

            statement.executeUpdate("update users set is_dead = 1, water = 0, food = 0, energy = 0 where food = 1");
            statement.executeUpdate("update users set food = food - 1 where registration_time is not null && is_dead = 0");
            result = statement.executeQuery("select * from users where food = 10 or food = 30 or food = 50 or food = 70");
            while (result.next()){
                long id = result.getLong("id");
                String nickname = result.getString("nickname");
                bot.execute(new SendSticker(id, Stickers.FOOD.toString()));
                bot.execute(new SendMessage(id, nickname + " хочет кушать, не забывайте о нем"));
            }
            System.out.println("-еда");
    }

    // метод для постоянного прибавления энергии у пользователей
    private static void addEnergy(Statement statement) throws SQLException{
        statement.executeUpdate("update users set energy = energy + 1 where registration_time is not null && energy < max_energy && is_building_web = 0 && is_dead = 0");
        System.out.println("+энергия");
    }
}