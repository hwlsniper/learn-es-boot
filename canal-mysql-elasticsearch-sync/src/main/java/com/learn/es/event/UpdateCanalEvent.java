package com.learn.es.event;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;

public class UpdateCanalEvent extends AbstractCanalEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public UpdateCanalEvent(Entry source) {
        super(source);
    }
}
