package com.gkash.gkashsoftpossdk.model;

import com.gkash.gkashsoftpossdk.GkashSoftPOSSDK;

public class SocketEventCallback {
    public GkashSoftPOSSDK.TransactionCallbackType type;
    public GkashSoftPOSSDK.TransactionEventCallback eventIndex;

    public GkashSoftPOSSDK.TransactionCallbackType getType() {
        return type;
    }

    public void setType(GkashSoftPOSSDK.TransactionCallbackType type) {
        this.type = type;
    }

    public GkashSoftPOSSDK.TransactionEventCallback getEventIndex() {
        return eventIndex;
    }

    public void setEventIndex(GkashSoftPOSSDK.TransactionEventCallback eventIndex) {
        this.eventIndex = eventIndex;
    }
}
