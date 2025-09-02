package top.maple_bamboo.api.events.impl;

import top.maple_bamboo.api.events.Event;

public class GameLeftEvent extends Event {
    public GameLeftEvent() {
        super(Stage.Post);
    }
}
