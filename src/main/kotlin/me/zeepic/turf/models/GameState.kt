package me.zeepic.turf.models

enum class GameState(val running: Boolean) {

    IDLE(false), STARTING(false), BUILDING(true), ATTACKING(true), ENDING(false)

}
