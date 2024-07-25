package xd.arkosammy.publicenderchest.logging

enum class TimeQueryType(val commandNodeName: String) {
    BEFORE("before"),
    AFTER("after");

    companion object {

        fun getFromCommandNodeName(name: String) : TimeQueryType? {
            for (timeQueryType: TimeQueryType in TimeQueryType.entries) {
                if (name == timeQueryType.commandNodeName) {
                    return timeQueryType
                }
            }
            return null
        }

    }

}