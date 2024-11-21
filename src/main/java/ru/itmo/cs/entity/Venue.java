package ru.itmo.cs.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "venue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @OneToOne
    @JoinColumn(name = "location_id", nullable = false, unique = true) // Внешний ключ теперь в "venue"
    private Location location;

    @Column(nullable = false)
    private Integer capacity;
}


