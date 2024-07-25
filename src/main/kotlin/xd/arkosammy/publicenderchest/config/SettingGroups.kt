package xd.arkosammy.publicenderchest.config

import xd.arkosammy.monkeyconfig.groups.DefaultMutableSettingGroup
import xd.arkosammy.monkeyconfig.groups.MutableSettingGroup

enum class SettingGroups(private val settingGroup: MutableSettingGroup) {
    PLAYER_BLACKLIST(DefaultMutableSettingGroup("player_blacklist", """
        Add players to a blacklist to prevent them from being able to use the public ender chest.
    """.trimIndent())),
    DIMENSION_BLACKLIST(DefaultMutableSettingGroup("dimension_blacklist", """
        Add dimension identifiers to this blacklist to prevent players from being able to use the public ender chest in those dimensions.
    """.trimIndent())),
    DATABASE(DefaultMutableSettingGroup("database", """
        Configure settings related to the mod's database.
    """.trimIndent()));

    val groupName: String
        get() = this.settingGroup.name

    companion object {

        val settingGroups: List<MutableSettingGroup>
            get() = entries.map { e -> e.settingGroup }.toList()

    }

}