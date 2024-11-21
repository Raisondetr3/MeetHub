package ru.itmo.cs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "participant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Participant {

    @EmbeddedId
    private ParticipantId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "is_creator", nullable = false)
    private Boolean isCreator;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class ParticipantId implements Serializable {
        @Column(name = "user_id")
        private Integer userId;

        @Column(name = "event_id")
        private Integer eventId;
    }
}


