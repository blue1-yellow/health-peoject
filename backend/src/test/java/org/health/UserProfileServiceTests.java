package org.health;

import org.health.mapper.UserProfileMapper;
import org.health.pojo.UserProfile;
import org.health.service.UserProfileService;
import org.health.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserProfileServiceTests {
    @AfterEach void clear() { UserContext.remove(); }
    private UserProfile sample() {
        UserProfile p = new UserProfile(); p.setName("测试"); p.setHeight(170.0); p.setWeight(65.0); p.setAge(30); p.setGender("male"); return p;
    }
    @Test void calculatesAdultBmiAndFat() {
        UserProfile p = UserProfileService.calculate(sample());
        assertEquals(22.5, p.getBmi()); assertEquals(17.7, p.getBodyFat());
        p.setGender("female"); assertEquals(28.5, UserProfileService.calculate(p).getBodyFat());
    }
    @Test void doesNotEstimateForChildrenOrMissingInputs() {
        UserProfile p = sample(); p.setAge(17);
        assertNull(UserProfileService.calculate(p).getBodyFat());
        p.setAge(null); assertNull(UserProfileService.calculate(p).getBodyFat());
        p.setWeight(null); assertNull(UserProfileService.calculate(p).getBmi());
    }
    @Test void neverAcceptsClientUserId() {
        UserProfileMapper mapper = mock(UserProfileMapper.class); UserContext.setUserId(7);
        UserProfile p = sample(); p.setUserId(99);
        new UserProfileService(mapper).save(p);
        assertEquals(7, p.getUserId()); verify(mapper).save(p);
    }
    @Test void readsOnlyCurrentAccount() {
        UserProfileMapper mapper = mock(UserProfileMapper.class); UserContext.setUserId(7);
        when(mapper.find(7)).thenReturn(sample());
        assertEquals("测试", new UserProfileService(mapper).get().getName()); verify(mapper).find(7);
    }
    @Test void rejectsInvalidValuesAndAvatar() {
        UserProfileMapper mapper = mock(UserProfileMapper.class); UserProfileService service = new UserProfileService(mapper);
        UserProfile p = sample(); p.setHeight(Double.NaN); assertThrows(IllegalArgumentException.class, () -> service.save(p));
        p.setHeight(170.0); p.setWeight(-1.0); assertThrows(IllegalArgumentException.class, () -> service.save(p));
        p.setWeight(65.0); p.setAvatar("data:image/svg+xml;base64,abc"); assertThrows(IllegalArgumentException.class, () -> service.save(p));
        p.setAvatar("data:image/jpeg;base64,YWJj"); assertThrows(IllegalArgumentException.class, () -> service.save(p));
        verify(mapper, never()).save(any());
    }
}
