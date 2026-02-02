package co.com.bancolombia.usecase.validator;

import co.com.bancolombia.model.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CapacityValidator Tests")
class CapacityValidatorTest {

    @Test
    @DisplayName("Should validate name successfully")
    void testValidateName_Success() {
        assertDoesNotThrow(() -> CapacityValidator.validateName("Valid Name"));
    }

    @Test
    @DisplayName("Should throw exception when name is null")
    void testValidateName_Null() {
        assertThrows(BusinessException.class, () -> CapacityValidator.validateName(null));
    }

    @Test
    @DisplayName("Should throw exception when name is blank")
    void testValidateName_Blank() {
        assertThrows(BusinessException.class, () -> CapacityValidator.validateName("   "));
    }

    @Test
    @DisplayName("Should throw exception when name exceeds max length")
    void testValidateName_TooLong() {
        String longName = "a".repeat(51);
        assertThrows(BusinessException.class, () -> CapacityValidator.validateName(longName));
    }

    @Test
    @DisplayName("Should validate description successfully")
    void testValidateDescription_Success() {
        assertDoesNotThrow(() -> CapacityValidator.validateDescription("Valid Description"));
    }

    @Test
    @DisplayName("Should throw exception when description is null")
    void testValidateDescription_Null() {
        assertThrows(BusinessException.class, () -> CapacityValidator.validateDescription(null));
    }

    @Test
    @DisplayName("Should throw exception when description exceeds max length")
    void testValidateDescription_TooLong() {
        String longDescription = "a".repeat(91);
        assertThrows(BusinessException.class, () -> CapacityValidator.validateDescription(longDescription));
    }

    @Test
    @DisplayName("Should validate technology IDs successfully with 3 technologies")
    void testValidateTechnologyIds_MinimumValid() {
        List<Long> techIds = Arrays.asList(1L, 2L, 3L);
        assertDoesNotThrow(() -> CapacityValidator.validateTechnologyIds(techIds));
    }

    @Test
    @DisplayName("Should validate technology IDs successfully with 20 technologies")
    void testValidateTechnologyIds_MaximumValid() {
        List<Long> techIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
                                           11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L);
        assertDoesNotThrow(() -> CapacityValidator.validateTechnologyIds(techIds));
    }

    @Test
    @DisplayName("Should throw exception when less than 3 technologies")
    void testValidateTechnologyIds_LessThanMinimum() {
        List<Long> techIds = Arrays.asList(1L, 2L);
        assertThrows(BusinessException.class, () -> CapacityValidator.validateTechnologyIds(techIds));
    }

    @Test
    @DisplayName("Should throw exception when more than 20 technologies")
    void testValidateTechnologyIds_MoreThanMaximum() {
        List<Long> techIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
                                           11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L);
        assertThrows(BusinessException.class, () -> CapacityValidator.validateTechnologyIds(techIds));
    }

    @Test
    @DisplayName("Should throw exception when duplicate technologies exist")
    void testValidateTechnologyIds_Duplicates() {
        List<Long> techIds = Arrays.asList(1L, 2L, 3L, 1L);
        assertThrows(BusinessException.class, () -> CapacityValidator.validateTechnologyIds(techIds));
    }

    @Test
    @DisplayName("Should throw exception when technology IDs is null")
    void testValidateTechnologyIds_Null() {
        assertThrows(BusinessException.class, () -> CapacityValidator.validateTechnologyIds(null));
    }

    @Test
    @DisplayName("Should throw exception when technology IDs is empty")
    void testValidateTechnologyIds_Empty() {
        assertThrows(BusinessException.class, () -> CapacityValidator.validateTechnologyIds(Collections.emptyList()));
    }
}
