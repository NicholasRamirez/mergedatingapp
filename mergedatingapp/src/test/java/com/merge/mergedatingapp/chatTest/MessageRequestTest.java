package com.merge.mergedatingapp.chatTest;

import com.merge.mergedatingapp.chat.*;
import com.merge.mergedatingapp.chat.dto.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageRequestTest {

    @Test
    void constructorAndGetter_work() {
        MessageRequest req = new MessageRequest("hello world");
        assertEquals("hello world", req.content());
    }

    @Test
    void content_canBeTrimmedExternally() {
        MessageRequest req = new MessageRequest("  hi  ");
        assertEquals("  hi  ", req.content()); // record just stores the value
    }
}