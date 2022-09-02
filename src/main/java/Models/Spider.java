package Models;

import java.sql.Timestamp;

public class Spider {
    public String nickname;
    public int level;
    public int experience;
    public Timestamp registrationTime;

    public int energy;
    public int maxEnergy;
    public int food;
    public int maxFood;
    public int water;
    public int maxWater;
    public int upgradePoints;

    public boolean isDead;

    public int flies;
    public boolean isBuildingWeb;
    public Timestamp endOfBuilding;
    public int webs;


    //TODO удалить записи времени и вместо них сделать максимальные значения для энергии, еды и воды
    public Spider(String nickname, int level, int experience, Timestamp registrationTime, int energy, int maxEnergy, int food, int maxFood, int water, int maxWater,int upgradePoints, boolean isDead, int flies, boolean isBuildingWeb, Timestamp endOfBuilding, int webs) {
        this.nickname = nickname;
        this.level = level;
        this.experience = experience;
        this.registrationTime = registrationTime;

        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.food = food;
        this.maxFood = maxFood;
        this.water = water;
        this.maxWater = maxWater;
        this.upgradePoints = upgradePoints;
        this.isDead = isDead;

        this.flies = flies;
        this.isBuildingWeb = isBuildingWeb;
        this.endOfBuilding = endOfBuilding;
        this.webs = webs;
    }

    public Spider(){

    }

    public String setNickname(String nickname) {
        if (nickname != null) {
            if (nickname.isEmpty())
                return "Вы ничего не написали!";
            if (nickname.length() > 15)
                return "Ваше имя слишком длинное. \nВведите имя длиной меньше 15 символов";
            else {
                this.nickname = nickname;
                return "Ваш ник теперь: " + nickname;
            }
        }
        else return "Вы ничего не написали!";
    }
}