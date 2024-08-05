import com.infinitehorizons.models.SecurityLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityLevelTest {

    @Test
    void isAuthorisedTest() {
        assertTrue(SecurityLevel.OWNER.isAuthorised(SecurityLevel.CONTRIBUTOR));
        assertTrue(SecurityLevel.OWNER.isAuthorised(SecurityLevel.STAFF));
        assertTrue(SecurityLevel.OWNER.isAuthorised(SecurityLevel.DEVELOPER));
        assertTrue(SecurityLevel.DEVELOPER.isAuthorised(SecurityLevel.STAFF));
        assertTrue(SecurityLevel.STAFF.isAuthorised(SecurityLevel.STAFF));

        assertFalse(SecurityLevel.STAFF.isAuthorised(SecurityLevel.OWNER));
        assertFalse(SecurityLevel.STAFF.isAuthorised(SecurityLevel.DEVELOPER));
        assertFalse(SecurityLevel.DEVELOPER.isAuthorised(SecurityLevel.OWNER));
    }

}
