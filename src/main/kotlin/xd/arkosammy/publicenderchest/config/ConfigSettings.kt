package xd.arkosammy.publicenderchest.config

import xd.arkosammy.monkeyconfig.settings.BooleanSetting
import xd.arkosammy.monkeyconfig.settings.ConfigSetting
import xd.arkosammy.monkeyconfig.settings.NumberSetting
import xd.arkosammy.monkeyconfig.settings.list.StringListSetting
import xd.arkosammy.monkeyconfig.util.SettingLocation

enum class ConfigSettings(private val builder: ConfigSetting.Builder<*, *, *>) {
    ENABLE_PLAYER_BLACKLIST(BooleanSetting.Builder(SettingLocation(SettingGroups.PLAYER_BLACKLIST.groupName, "enable_player_blacklist"), """
        (Default = false) Toggle the use of the player blacklist feature.
    """.trimIndent(), false)),
    PLAYER_BLACKLIST(StringListSetting.Builder(SettingLocation(SettingGroups.PLAYER_BLACKLIST.groupName, "player_blacklist"), """
        Add players to this blacklist by specifying their username.
    """.trimIndent(), mutableListOf())),

    ENABLE_DIMENSION_BLACKLIST(BooleanSetting.Builder(SettingLocation(SettingGroups.DIMENSION_BLACKLIST.groupName, "enable_dimension_blacklist"), """
        (Default = false) Toggle the use of the dimension blacklist feature.
    """.trimIndent(), false)),
    DIMENSION_BLACKLIST(StringListSetting.Builder(SettingLocation(SettingGroups.DIMENSION_BLACKLIST.groupName, "dimension_blacklist"), """
        Add dimensions to this blacklist by specifying their full identifier. For example, to add the Overworld dimension, you can add the identifier "minecraft:overworld" to this list.
    """.trimIndent(), mutableListOf())),

    PURGE_OLDER_THAN_X_DAYS(NumberSetting.Builder(SettingLocation(SettingGroups.DATABASE.groupName, "purge_older_than_x_days"), """
        (Default = 30) Purge database entries older than the amount of days specified. The database is purged on every server shutdown and using the `/publicenderchest database purge` command.
    """.trimIndent(), 30).withLowerBound(0));

    val settingLocation: SettingLocation = builder.settingLocation

    companion object {

        val settingBuilders: List<ConfigSetting.Builder<*, *, *>>
            get() = entries.map { e -> e.builder }.toList()

    }

}