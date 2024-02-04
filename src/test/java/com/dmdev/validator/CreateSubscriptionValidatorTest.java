package com.dmdev.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreateSubscriptionValidatorTest {

  private final CreateSubscriptionValidator validator = CreateSubscriptionValidator.getInstance();

  @Test
  void shouldSubscriptionValid() {

    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .userId(1)
        .name("Subscribe 1")
        .provider(Provider.APPLE.name())
        .expirationDate(Instant.now().plus(1, ChronoUnit.DAYS))
        .build();

    ValidationResult actualResult = validator.validate(dto);

    assertFalse(actualResult.hasErrors());

  }

  @Test
  void invalidSubscriptionExpirationDate() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .userId(1)
        .name("Subscribe 1")
        .provider(Provider.APPLE.name())
        .expirationDate(Instant.now())
        .build();

    ValidationResult actualResult = validator.validate(dto);

    assertTrue(actualResult.hasErrors());

    assertThat(actualResult.getErrors()).hasSize(1);
    assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(103);

  }

  @Test
  void invalidSubscriptionUserId() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .name("Subscribe 1")
        .provider(Provider.APPLE.name())
        .expirationDate(Instant.now().plus(1, ChronoUnit.DAYS))
        .build();

    ValidationResult actualResult = validator.validate(dto);

    assertTrue(actualResult.hasErrors());

    assertThat(actualResult.getErrors()).hasSize(1);
    assertThat(actualResult.getErrors().get(0).getMessage()).isEqualTo("userId is invalid");

  }

  @Test
  void invalidSubscriptionName() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .userId(1)
        .name("")
        .provider(Provider.APPLE.name())
        .expirationDate(Instant.now().plus(1, ChronoUnit.DAYS))
        .build();

    ValidationResult actualResult = validator.validate(dto);

    assertTrue(actualResult.hasErrors());

    assertThat(actualResult.getErrors()).hasSize(1);
    assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(101);

  }

  @Test
  void invalidSubscriptionProvider() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .userId(1)
        .name("Subscribe 1")
        .provider("dummy")
        .expirationDate(Instant.now().plus(1, ChronoUnit.DAYS))
        .build();

    ValidationResult actualResult = validator.validate(dto);

    assertTrue(actualResult.hasErrors());

    assertThat(actualResult.getErrors()).hasSize(1);
    assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(102);

  }

  @Test
  void invalidSubscriptionSomeParams() {
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .name("")
        .provider(null)
        .expirationDate(Instant.now())
        .build();

    ValidationResult actualResult = validator.validate(dto);

    assertTrue(actualResult.hasErrors());

    assertThat(actualResult.getErrors()).hasSize(4);

    List<Integer> errorCodes = actualResult.getErrors().stream().map(Error::getCode).toList();

    assertThat(errorCodes).contains(100, 101, 102, 103);

  }


}