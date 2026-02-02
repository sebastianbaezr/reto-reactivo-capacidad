package co.com.bancolombia.usecase.validator;

import co.com.bancolombia.model.enums.DomainErrorCode;
import co.com.bancolombia.model.exception.BusinessException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CapacityValidator {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 90;
    private static final int MIN_TECHNOLOGIES = 3;
    private static final int MAX_TECHNOLOGIES = 20;

    private CapacityValidator() {
        // Utility class
    }

    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(DomainErrorCode.CAPACITY_NAME_REQUIRED);
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BusinessException(DomainErrorCode.INVALID_CAPACITY_NAME_LENGTH);
        }
    }

    public static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BusinessException(DomainErrorCode.CAPACITY_DESCRIPTION_REQUIRED);
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException(DomainErrorCode.INVALID_CAPACITY_DESCRIPTION_LENGTH);
        }
    }

    public static void validateTechnologyIds(List<Long> technologyIds) {
        if (technologyIds == null || technologyIds.isEmpty()) {
            throw new BusinessException(DomainErrorCode.TECHNOLOGY_IDS_REQUIRED);
        }

        if (technologyIds.size() < MIN_TECHNOLOGIES) {
            throw new BusinessException(DomainErrorCode.MIN_TECHNOLOGIES_REQUIRED);
        }

        if (technologyIds.size() > MAX_TECHNOLOGIES) {
            throw new BusinessException(DomainErrorCode.MAX_TECHNOLOGIES_EXCEEDED);
        }

        Set<Long> uniqueIds = new HashSet<>(technologyIds);
        if (uniqueIds.size() != technologyIds.size()) {
            throw new BusinessException(DomainErrorCode.DUPLICATE_TECHNOLOGIES);
        }
    }
}
