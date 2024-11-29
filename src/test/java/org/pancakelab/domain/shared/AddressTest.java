package org.pancakelab.domain.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author Shantanu Singh.
 */
class AddressTest {

    @Test
    void givenValidBuildingAndRoom_whenCreatingAddress_thenAddressIsCreated() {
        Address address = new Address(1, 101);
        assertEquals(1, address.building());
        assertEquals(101, address.room());
    }

    @Test
    void givenInvalidBuildingNumber_whenCreatingAddress_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Address(0, 101));
        assertEquals("Building number must be positive.", exception.getMessage());
    }

    @Test
    void givenInvalidRoomNumber_whenCreatingAddress_thenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Address(1, 0));
        assertEquals("Room number must be positive.", exception.getMessage());
    }
}