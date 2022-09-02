package Views;

public enum Stickers {
    FOOD("CAACAgIAAxkBAAIGoGMGAAEutuZAapwRXY2amBMxtKB8zQACMgADbFk0HLsQg6paNrqrKQQ"),
    HELLO("CAACAgIAAxkBAAIGoWMGAU-9cL6ZRI8iGa2rGeGXAAE6lgACJwADbFk0HBvXIAoQus4VKQQ"),
    HAPPY("CAACAgIAAxkBAAIGomMGAVmM246psjvV0h7UWcHbtTDKAAItAANsWTQcArVfneGa-38pBA"),
    SLEEP("CAACAgIAAxkBAAIGv2MGeB_wNC9_VA8k8XQ8F1UecQZxAAIwAANsWTQcR0MIUOCJq0spBA"),
    SPORT("CAACAgIAAxkBAAIGxmMGeaBb1ud8EZiZxNzot8sCEIF6AAIaAANsWTQcXGikR85Wel0pBA"),
    ENGINEER("CAACAgIAAxkBAAIGyWMGehc1xaVE-jpcz70MQWlwzRqUAAIuAANsWTQciR4yD0hGNLspBA"),
    SHOWER("CAACAgIAAxkBAAIGx2MGecwkqXc_UtMUgXStSbKQyUNWAAIrAANsWTQcadQYLI3MgM0pBA"),
    LAUGHTER("CAACAgIAAxkBAAIGwWMGeG51hLNO07GAaCTE_vW4odxUAAIRAANsWTQcdeVNewk3mXcpBA"),
    FIRE("CAACAgIAAxkBAAIGymMGeka26FbM7vu_uwhe3ow7Z8MkAAIPAANsWTQcYkj5TalW0aUpBA"),
    MONDAY("CAACAgIAAxkBAAIGt2MGdn3xkUglC3fArWljJRJ7gnYpAAISAANsWTQcWGAXg8XKjqspBA"),
    WTF("CAACAgIAAxkBAAIGuGMGdoCd5PmPiSc8SeeFadAqYLuaAAIYAANsWTQcYssap7ZY7eIpBA"),
    WHAT("CAACAgIAAxkBAAIGw2MGeOC9w2PDeezleW_FHZmfkixtAAIkAANsWTQcN888JAssZ-wpBA"),
    BOOK("CAACAgIAAxkBAAIHAAFjBo9ZxK_k94TGCR6StONq2zFOygACIAADbFk0HEjaujkuvzpRKQQ"),
    IS_IT_FOR_ME("CAACAgIAAxkBAAIGumMGdonqJWKJDXuuUOGvdyp7wlcvAAIUAANsWTQcX5-bBxbyEBMpBA"),
    CRY("CAACAgIAAxkBAAIGu2MGdovV1D9zYXviYyUYhuCHKO1uAAIVAANsWTQcmGFpFpZOpRMpBA"),
    ANGRY("CAACAgIAAxkBAAIGvGMGdo7R4Dg9dFFKSRsQPPM_e69iAAIWAANsWTQcsgwos4W8uygpBA"),
    WARRIOR("CAACAgIAAxkBAAIGvWMGd774EVYTS2oxJYnghT_U6urHAAIhAANsWTQc5TdCb-0G9OspBA"),
    JAPANESE_WARRIOR("CAACAgIAAxkBAAIGvmMGeAd9luwTGMa8EbA2fMJY-CkwAAI2AANsWTQcNPoMgKaxSHwpBA"),
    BAN("CAACAgIAAxkBAAIGxWMGeWQe1RwbdV0-i_ZpldI9JqGWAAIQAANsWTQciqCh1aaQRwYpBA"),
    SORRY("CAACAgIAAxkBAAIGyGMGegOAG5oQfRdJdPByiOosBD0IAAIxAANsWTQcsl5TSuwFl_kpBA"),
    DOCTOR_DEATH("CAACAgIAAxkBAAIGzmMGewpqhaNjPlDX5UbZEkZYrHreAAIfAANsWTQcWBdHDpq3ancpBA");

    private String sticker;

    Stickers(String sticker) {
        this.sticker = sticker;
    }

    @Override
    public String toString() {
        return sticker;
    }
}
