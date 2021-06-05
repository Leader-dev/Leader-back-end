package com.leader.api.service.util;

import com.leader.api.util.component.ClientDataUtil;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserIdServiceTests {

    private static final ObjectId TEST_USER_ID = new ObjectId();

    @Autowired
    private UserIdService userIdService;

    @MockBean
    private ClientDataUtil clientDataUtil;

    @Test
    public void saveUserIdTest() {
        // no pre-actions

        userIdService.setCurrentUserId(TEST_USER_ID);

        verify(clientDataUtil, Mockito.times(1)).set(UserIdService.USER_ID, TEST_USER_ID);
    }

    @Test
    public void getUserIdTest() {
        when(clientDataUtil.get(UserIdService.USER_ID, ObjectId.class)).thenReturn(TEST_USER_ID);

        ObjectId userId = userIdService.getCurrentUserId();

        assertEquals(TEST_USER_ID, userId);
    }

    @Test
    public void removeUserIdTest() {
        // no pre-actions

        userIdService.clearCurrentUserId();

        verify(clientDataUtil, Mockito.times(1)).remove(UserIdService.USER_ID);
    }

    @Test
    public void userIdExistsTrueTest() {
        when(clientDataUtil.get(UserIdService.USER_ID, ObjectId.class)).thenReturn(TEST_USER_ID);

        boolean exists = userIdService.currentUserExists();

        assertTrue(exists);
    }

    @Test
    public void userIdExistsFalseTest() {
        when(clientDataUtil.get(UserIdService.USER_ID, ObjectId.class)).thenReturn(null);

        boolean exists = userIdService.currentUserExists();

        assertFalse(exists);
    }
}
