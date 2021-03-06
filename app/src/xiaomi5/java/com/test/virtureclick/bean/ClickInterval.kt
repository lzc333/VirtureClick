package com.test.virtureclick.bean

enum class ClickInterval(val timeMillis: Long) {
    heartstone_next_battle(420),
    heartstone_surrender_setting(300),
    heartstone_surrender_animation(6000),
    heartstone_surrender_battle(450),
    heartstone_hangup(300),
    heartstone_hangup_battle(3500),
    heartstone_hangup_end_round(7330),
    //小米5搜索对手的返回和对战开始的确认坐标重叠
    heartstone_hangup_sure_round(300000),
    heartstone_skill(7330)
}