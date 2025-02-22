package com.paymybuddy.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;

    @ManyToMany
    @JoinTable(name = "user_connections", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "connection_id"))
    private List<User> connections;
}
