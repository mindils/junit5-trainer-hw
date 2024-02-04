package com.dmdev.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class CreateSubscriptionMapperTest {

  private final CreateSubscriptionMapper mapper = CreateSubscriptionMapper.getInstance();

  @Test
  void map() {
    Instant nowPlusDay = Instant.now().plus(1, ChronoUnit.DAYS);
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .userId(1)
        .name("Sub1")
        .provider(Provider.APPLE.name())
        .expirationDate(nowPlusDay)
        .build();

    Subscription actualResult = mapper.map(dto);

    Subscription expectedResult = Subscription.builder()
        .userId(1)
        .name("Sub1")
        .provider(Provider.APPLE)
        .expirationDate(nowPlusDay)
        .status(Status.ACTIVE)
        .build();

    assertThat(actualResult).isEqualTo(expectedResult);
  }

  @Test
  void mapEmptyProvider() {
    Instant nowPlusDay = Instant.now().plus(1, ChronoUnit.DAYS);
    CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
        .userId(1)
        .name("Sub1")
        .expirationDate(nowPlusDay)
        .build();

    Subscription actualResult = mapper.map(dto);

    Subscription expectedResult = Subscription.builder()
        .userId(1)
        .name("Sub1")
        .provider(null)
        .expirationDate(nowPlusDay)
        .status(Status.ACTIVE)
        .build();

    assertThat(actualResult).isEqualTo(expectedResult);

  }
}