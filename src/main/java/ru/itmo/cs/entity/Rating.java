package ru.itmo.cs.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Рейтинг.
 */
@Getter
@RequiredArgsConstructor
public enum Rating {
  ONE_STAR(1),
  TWO_STARS(2),
  THREE_STARS(3),
  FOUR_STARS(4),
  FIVE_STARS(5);

  private final int stars;
}
