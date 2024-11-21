package ru.itmo.cs.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryEnum {
    CONFERENCE("Conference"),
    SEMINAR("Seminar"),
    WORKSHOP("Workshop"),
    MEETUP("Meetup"),
    CORPORATE("Corporate"),
    PARTY("Party"),
    CONCERT("Concert"),
    SHOW("Show"),
    OPENING("Opening"),
    OTHER("Other");

    private final String displayName;
}

