package com.yzh.campushub.event;

public record PostSearchSyncEvent(Long postId, Action action) {
    public enum Action {
        UPSERT,
        DELETE
    }
}
