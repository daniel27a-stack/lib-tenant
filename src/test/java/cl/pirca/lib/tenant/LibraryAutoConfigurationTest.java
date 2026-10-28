package cl.pirca.lib.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LibraryAutoConfigurationTest {

    @Test
    void libraryInfoCarriesTheLibraryName() {
        LibraryInfo info = new LibraryAutoConfiguration().libraryInfo();

        assertEquals("lib-tenant", info.name());
    }
}
