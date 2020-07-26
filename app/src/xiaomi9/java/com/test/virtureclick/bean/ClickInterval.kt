package com.test.virtureclick.bean

enum class ClickInterval(val timeMillis: Long) {
    heartstone_next_battle(350),
    heartstone_surrender_setting(200),
    heartstone_surrender_animation(5000),
    heartstone_surrender_battle(350),
    heartstone_hangup(200),
    heartstone_hangup_battle(3500),
    heartstone_hangup_end_round(7330),
    heartstone_hangup_sure_round(11760)
}