package com.dmdev.integration.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubscriptionDaoIT extends IntegrationTestBase {

  private SubscriptionDao subscriptionDao;

  @BeforeEach
  void init() {
    subscriptionDao = SubscriptionDao.getInstance();
  }

  @Test
  void findAll() {
    var subscription1 = subscriptionDao.insert(getSubscription(10, "Sub 1", Provider.APPLE));
    var subscription2 = subscriptionDao.insert(getSubscription(200, "sub 2", Provider.GOOGLE));
    var subscription3 = subscriptionDao.insert(getSubscription(999, "sub 3", Provider.GOOGLE));
    var subscription4 = subscriptionDao.insert(getSubscription(2, "sub 4", Provider.APPLE));

    List<Subscription> actualResult = subscriptionDao.findAll();

    assertThat(actualResult).hasSize(4);

    var subscriptionIds = actualResult.stream()
        .map(Subscription::getId)
        .toList();

    assertThat(subscriptionIds).contains(
        subscription1.getId(),
        subscription2.getId(),
        subscription3.getId(),
        subscription4.getId()
    );
  }

  @Test
  void findById() {
    var subscription = subscriptionDao.insert(getSubscription(10, "sub 1", Provider.GOOGLE));

    var actualResult = subscriptionDao.findById(subscription.getId());

    assertThat(actualResult).isPresent();
    assertThat(actualResult.get()).isEqualTo(subscription);
  }

  @Test
  void shouldNotFindByIdIfSubscriptionDoesNotExist() {
    subscriptionDao.insert(getSubscription(100, "Sub 1", Provider.APPLE));

    Optional<Subscription> actualResult = subscriptionDao.findById(999);

    assertThat(actualResult).isEmpty();
  }

  @Test
  void deleteExistingEntity() {
    Subscription subscription = subscriptionDao.insert(
        getSubscription(100, "sub 1", Provider.GOOGLE));

    boolean actualResult = subscriptionDao.delete(subscription.getId());

    assertThat(actualResult).isTrue();
  }

  @Test
  void deleteNotExistingEntity() {
    subscriptionDao.insert(
        getSubscription(100, "sub 1", Provider.GOOGLE));

    boolean actualResult = subscriptionDao.delete(999);

    assertThat(actualResult).isFalse();
  }

  @Test
  void update() {
    Subscription subscription = subscriptionDao.insert(
        getSubscription(999, "sub 3", Provider.GOOGLE));
    subscription.setStatus(Status.EXPIRED);

    subscriptionDao.update(subscription);

    Optional<Subscription> actualResult = subscriptionDao.findById(subscription.getId());
    assertThat(actualResult.get()).isEqualTo(subscription);
  }

  @Test
  void insert() {
    Subscription subscription = getSubscription(999, "sub 3", Provider.GOOGLE);

    Subscription actualResult = subscriptionDao.insert(subscription);

    assertThat(actualResult.getId()).isNotNull();
  }

  @Test
  void findByUserId() {
    subscriptionDao.insert(getSubscription(999, "Sub 1", Provider.APPLE));
    subscriptionDao.insert(getSubscription(999, "sub 2", Provider.GOOGLE));

    List<Subscription> actualResult = subscriptionDao.findByUserId(999);

    assertThat(actualResult).hasSize(2);
  }


  @Test
  void emptyListIfUserDoNotHaveSubscription() {
    subscriptionDao.insert(getSubscription(999, "Sub 1", Provider.APPLE));
    subscriptionDao.insert(getSubscription(999, "sub 2", Provider.GOOGLE));

    var actualResult = subscriptionDao.findByUserId(100);

    assertThat(actualResult).isEmpty();
  }


  private Subscription getSubscription(Integer userId, String name, Provider provider) {
    return Subscription.builder()
        .userId(userId)
        .name(name)
        .provider(provider)
        .status(Status.ACTIVE)
        .expirationDate(Instant.now().plus(1, ChronoUnit.DAYS))
        .build();
  }
}