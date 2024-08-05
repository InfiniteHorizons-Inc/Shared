import com.infinitehorizons.models.Version;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    @Test
    void toStringTest() {
        assertEquals(Version.of(1, 0, 0).toString(), "1.0.0");
        assertEquals(Version.of(-1, -10, -20).toString(), "1.10.20");
    }

    @Test
    void parsePositiveTest() {
        assertEquals(Version.parseVersion("1.2.3"), Version.of(1, 2, 3));
        assertEquals(Version.parseVersion("22"), Version.of(22));
    }

    @Test
    void parseNegativeTest() {
        assertThrows(IllegalArgumentException.class, () -> Version.parseVersion("Hi there!"));
        assertThrows(IllegalArgumentException.class, () -> Version.parseVersion("th15.c0ul4.b3.4.v3r5io2"));
    }

    @Test
    void equalsTest() {
        assertEquals(Version.of(1, 0, 0), Version.of(1, 0, 0));
        assertNotEquals(Version.of(1, 0, 0), Version.of(1, 0, 1));
    }

    @Test
    void hashCodeTest() {
        assertEquals(Version.of(1, 0, 0).hashCode(), Version.of(1, 0, 0).hashCode());
        assertNotEquals(Version.of(1, 0, 0).hashCode(), Version.of(1, 0, 1).hashCode());
    }

    @Test
    void compareToTest() {
        assertEquals(-1, Version.of(1, 0, 0).compareTo(Version.of(1, 0, 1)));
        assertEquals(0, Version.of(1, 0, 0).compareTo(Version.of(1, 0, 0)));
    }

}
