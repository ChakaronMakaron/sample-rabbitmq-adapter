package com.lemakhno.exceptions;

import com.lemakhno.enums.MessageStatus;

public class StopMessageProcessingException extends RuntimeException {

    private final MessageStatus messageStatus;

    public StopMessageProcessingException(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }
}
