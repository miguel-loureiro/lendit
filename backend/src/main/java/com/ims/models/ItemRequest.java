package com.ims.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_requests")
@Getter
@Setter
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Column(nullable = false)
    private Integer queuePosition;

    @Enumerated(EnumType.STRING)
    private ItemRequestStatus status;

    @Column
    private LocalDate returnDate;

    @Version
    private Long version;

    public ItemRequest() {
        this.requestDate = LocalDate.from(LocalDateTime.now());
        this.status = ItemRequestStatus.PENDING;
    }
}