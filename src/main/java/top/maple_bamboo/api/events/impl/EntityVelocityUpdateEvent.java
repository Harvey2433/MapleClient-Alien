package top.maple_bamboo.api.events.impl;

import top.maple_bamboo.api.events.Event;

public class EntityVelocityUpdateEvent extends Event {
    public EntityVelocityUpdateEvent() {
        super(Stage.Pre);
    }
}
