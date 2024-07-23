package xd.arkosammy.publicenderchest.config

import xd.arkosammy.monkeyconfig.settings.BooleanSetting
import xd.arkosammy.monkeyconfig.settings.ConfigSetting
import xd.arkosammy.monkeyconfig.settings.list.StringListSetting
import xd.arkosammy.monkeyconfig.util.SettingLocation

enum class ConfigSettings(private val builder: ConfigSetting.Builder<*, *, *>) {
    ENABLE_PLAYER_BLACKLIST(BooleanSetting.Builder(SettingLocation(SettingGroups.PLAYER_BLACKLIST.groupName, "enable_player_blacklist"), """
        Toggle the use of the player blacklist feature.
    """.trimIndent(), false)),
    PLAYER_BLACKLIST(StringListSetting.Builder(SettingLocation(SettingGroups.PLAYER_BLACKLIST.groupName, "player_blacklist"), """
        Add players to this blacklist by specifying their username.
    """.trimIndent(), mutableListOf())),

    ENABLE_DIMENSION_BLACKLIST(BooleanSetting.Builder(SettingLocation(SettingGroups.DIMENSION_BLACKLIST.groupName, "enable_dimension_blacklist"), """
        Toggle the use of the dimension blacklist feature.
    """.trimIndent(), false)),
    DIMENSION_BLACKLIST(StringListSetting.Builder(SettingLocation(SettingGroups.DIMENSION_BLACKLIST.groupName, "dimension_blacklist"), """
        Add dimensions to this blacklist by specifying their full identifier. For example, to add the Overworld dimension, you can add the identifier "minecraft:overworld" to this list.
    """.trimIndent(), mutableListOf()));

    val settingLocation: SettingLocation = builder.settingLocation

    companion object {

        val settingBuilders: List<ConfigSetting.Builder<*, *, *>>
            get() = entries.map { e -> e.builder }.toList()

    }

}