package com.dmdev.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;
import com.dmdev.validator.ValidationResult;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @Mock
  private SubscriptionDao subscriptionDao;
  @Mock
  private CreateSubscriptionMapper createSubscriptionMapper;
  @Mock
  private CreateSubscriptionValidator createSubscriptionValidator;

  private Clock fixedClock;
  private SubscriptionService subscriptionService;

  @BeforeEach
  void setUp() {
    fixedClock = Clock.fixed(Instant.now().plus(1, ChronoUnit.DAYS), ZoneId.systemDefault());
    subscriptionService = new SubscriptionService(
        subscriptionDao,
        createSubscriptionMapper,
        createSubscriptionValidator,
        fixedClock
    );
  }

  @Test
  void upsertSuccess() {

    CreateSubscriptionDto dto = getSubscriptionDto();
    Subscription subscription = getSubscription();

    doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(dto);
    doReturn(List.of(subscription)).when(subscriptionDao).findByUserId(dto.getUserId());
    doReturn(subscription).when(subscriptionDao).upsert(subscription);

    Subscription actualResult = subscriptionService.upsert(dto);

    assertThat(actualResult).isEqualTo(subscription);
    assertThat(actualResult.getExpirationDate()).isEqualTo(fixedClock.instant());
    verify(subscriptionDao).upsert(subscription);

  }

  @Test
  void upsertFailedValidation() {

    CreateSubscriptionDto dto = getSubscriptionDto();
    ValidationResult validationResult = new ValidationResult();
    validationResult.add(Error.of(100, "error"));

    doReturn(validationResult).when(createSubscriptionValidator).validate(dto);

    assertThrows(ValidationException.class,
        () -> subscriptionService.upsert(dto));
    verifyNoInteractions(subscriptionDao, createSubscriptionMapper);

  }

  @Test
  void cancelSuccess() {
    Subscription subscription = getSubscription();

    doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());

    subscriptionService.cancel(subscription.getId());

    assertThat(subscription.getStatus()).isEqualTo(Status.CANCELED);
    verify(subscriptionDao).update(subscription);

  }

  @Test
  void cancelFindFail() {
    Subscription subscription = getSubscription();

    doReturn(Optional.empty()).when(subscriptionDao).findById(subscription.getId());

    assertThrows(IllegalArgumentException.class,
        () -> subscriptionService.cancel(subscription.getId()));

    verify(subscriptionDao, times(0)).update(subscription);

  }

  @Test
  void cancelFailedStatus() {
    Subscription subscription = getSubscription();
    subscription.setStatus(Status.CANCELED);

    doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());

    assertThrows(SubscriptionException.class,
        () -> subscriptionService.cancel(subscription.getId()));

    verify(subscriptionDao, times(0)).update(subscription);

  }

  @Test
  void expireSuccess() {
    Subscription subscription = getSubscription();

    doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());

    subscriptionService.expire(subscription.getId());

    assertThat(subscription.getStatus()).isEqualTo(Status.EXPIRED);

    verify(subscriptionDao).update(subscription);
  }

  @Test
  void expireFindFail() {
    Subscription subscription = getSubscription();

    doReturn(Optional.empty()).when(subscriptionDao).findById(subscription.getId());

    assertThrows(IllegalArgumentException.class,
        () -> subscriptionService.expire(subscription.getId()));

    verify(subscriptionDao, times(0)).update(subscription);

  }

  @Test
  void expireFailedStatus() {
    Subscription subscription = getSubscription();
    subscription.setStatus(Status.EXPIRED);

    doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());

    assertThrows(SubscriptionException.class,
        () -> subscriptionService.expire(subscription.getId()));

    verify(subscriptionDao, times(0)).update(subscription);

  }

  private Subscription getSubscription() {
    return Subscription.builder()
        .id(999)
        .userId(1)
        .name("Sub1")
        .provider(Provider.APPLE)
        .status(Status.ACTIVE)
        .expirationDate(Instant.now(fixedClock))
        .build();
  }

  private CreateSubscriptionDto getSubscriptionDto() {
    return CreateSubscriptionDto.builder()
        .userId(1)
        .name("Sub1")
        .provider(Provider.APPLE.name())
        .expirationDate(Instant.now(fixedClock))
        .build();
  }
}