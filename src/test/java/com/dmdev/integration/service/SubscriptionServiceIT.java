package com.dmdev.integration.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.service.SubscriptionService;
import com.dmdev.validator.CreateSubscriptionValidator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceIT extends IntegrationTestBase {

  private SubscriptionService subscriptionService;
  private SubscriptionDao subscriptionDao;
  private Clock fixedClock;

  @BeforeEach
  void init() {
    fixedClock = Clock.fixed(Instant.now().plus(1, ChronoUnit.DAYS), ZoneId.systemDefault());

    subscriptionDao = SubscriptionDao.getInstance();
    subscriptionService = new SubscriptionService(
        subscriptionDao,
        CreateSubscriptionMapper.getInstance(),
        CreateSubscriptionValidator.getInstance(),
        fixedClock
    );
  }

  @Test
  void upsert() {
    CreateSubscriptionDto subscriptionDto = getSubscriptionDto();

    Subscription actualResult = subscriptionService.upsert(subscriptionDto);

    assertThat(actualResult.getId()).isNotIn();

  }

  @Test
  void cancel() {
    Subscription subscription = subscriptionDao.insert(getSubscription());

    subscriptionService.cancel(subscription.getId());

    Subscription actualResult = subscriptionDao.findById(subscription.getId()).get();
    assertThat(actualResult.getStatus()).isEqualTo(Status.CANCELED);
  }


  @Test
  void expire() {
    Subscription subscription = subscriptionDao.insert(getSubscription());

    subscriptionService.expire(subscription.getId());

    Subscription actualResult = subscriptionDao.findById(subscription.getId()).get();
    assertThat(actualResult.getStatus()).isEqualTo(Status.EXPIRED);
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