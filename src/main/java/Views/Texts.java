package Views;

import Models.Spider;
import Models.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Texts {

    public static String notAuthorizedStartText() {
        return "Здравствуйте, как хотите назвать своего паучка?";
    }

    public static String authorizedStartText(Spider spider) {
        return "Рады видеть Вас снова, Ваш " + spider.nickname + " скучал";
    }

    public static String settingsText() {
        return "Это раздел настроек, здесь вы можете изменить ник и распределить свои очки улучшений.";
    }

    public static String helpText() {
        return """
                Заботьтесь о своем паучке и повышайте его уровень.

                Чтобы покормить своего паука выйдите в главное меню -> "паучок" -> "покормить". Одна муха восстанавливает 20 единиц еды.

                Чтобы напоить паука выйдите в главное меню -> "паучок" -> "попоить".
                                
                Энергия восстанавливается сама со временем, но она перестает восстанавливаться, когда паук чем-то занят.

                Стройте паутины и ловите мух, чтобы всегда было чем накормить пушистика.
                Учтите, что паутины могут рваться и это никак не зависит от того, когда они были построены.""";
    }

    public static String mainMenuText(ResultSet result) throws SQLException {
        StringBuilder top = new StringBuilder("Лидеры:" + "\n\n\n");
        while (result.next()){
            int experience = result.getInt("experience");
            int level = result.getInt("level");
            for (; level > 1; level--){
                experience += (level - 1) * 100;
            }
            top.append(result.getString("nickname")).append(" ").append(result.getInt("level")).append(" lvl. - ").append(experience).append(" xp.").append("\n\n");
        }
        return top.toString();
    }

    public static String changeNicknameText(Spider spider) {
        return "Ваш ник: " + spider.nickname + "\n\n" +
                "Введите новый ник";
    }

    public static String infoText(Spider spider) {
        return "Информация о " + spider.nickname + " " + spider.level + " lvl. " + "\n\n" +
                "Опыт : " + spider.experience + "\n" +
                "Мух: " + spider.flies + "\n" +
                "Паутин: " + spider.webs + "\n" +
                "Энергия: " + spider.energy + "/" + spider.maxEnergy + "\n" +
                "Еда: " + spider.food + "/" + spider.maxFood + "\n" +
                "Вода: " + spider.water + "/" + spider.maxWater;
    }

    public static String commandsText() {
        return """
                Здесь будет список всех доступных команд

                Базовые менюшки:
                /settings - настройки
                /help - как играть
                /main_menu - главное меню
                /change_nickname - изменение ника
                /info - информация о пауке
                /commands - получение списка команд

                Действия с пауком:
                /feed - покормить
                /water - попоить
                /build_web - строить паутину
                                
                Использование очков улучшения:
                /upgrade - использовать очки улучшений
                /upgrade_food - увеличить запас еды
                /upgrade_water - увеличить запас воды
                /upgrade_energy - увеличить запас энергии
                                
                Помочь проекту:
                /admin - связь с админом (просьба не беспокоить без повода)
                /help_project - материальная помощь проекту""";
    }

    public static String withoutNicknameText() {
        return "Вы еще не назвали своего паука. Дайте ему имя и возвращайтесь.";
    }

    public static String thirstyDeathText(String nickname) {
        return nickname + " умер от жажды.(" + "\n" +
                "Нам очень жаль." + "\n" +
                "К сожалению, его никак не вернуть." + "\n\n" +
                "Заведите нового паучка и заботьтесь о нем на сей раз лучше.";
    }

    public static String hungryDeathText(String nickname) {
        return nickname + " умер от голода.(" + "\n" +
                "Нам очень жаль." + "\n" +
                "К сожалению, его никак не вернуть." + "\n\n" +
                "Заведите нового паучка и заботьтесь о нем на сей раз лучше.";
    }

    public static String upgradePointsUsagesText(Spider spider) {
        if (spider.upgradePoints == 0) {
            return "У вас " + spider.upgradePoints + " очков улучшения" + "\n" +
                    "Вы можете получить их с каждым повышением уровня паука.";
        } else {
            return "У вас " + spider.upgradePoints + " очков улучшения" + "\n" +
                    "Выберите, что улучшить. Учтите, что сейчас вы не сможете отменить свой выбор. Такая функция планируется в будущих обновлениях.";
        }
    }

    public static String askAdminHelpText() {
        return """
                Пожалуйста, воздержитесь от спама.
                Здравствуйте, подробно опишите свою проблему или предложение.""";
    }

    public static String toAdminText(User user, String message) {
        return "Новое сообщение от пользователя " + user.id + " (" + user.username + ") " + "\n\n" +
                message;
    }

    public static String adminResponseAboutHelpText() {
        return "Ваше сообщение было отправлено. Если ваше сообщение не является спамом с вами скоро свяжутся в личном чате.";
    }

    public static String helpProjectText() {
        return "Спасибо, что решили помочь нам, но пока проект находится на стадии теста, этот пункт меню отключен";
    }

}