package ca.yorku.eecs2311.schedulelynx.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  @Column(nullable = false, unique = true) private String username;

  @Column(nullable = false, unique = true) private String email;

  @Column(nullable = false) private String fullName;

  @Column(nullable = false) private String passwordHash;

  @Column(nullable = false) private LocalDateTime createdAt;

  protected User() {
    // Required by JPA
  }

  public User(String username, String email, String fullName,
              String passwordHash) {
    this.username = username;
    this.email = email;
    this.fullName = fullName;
    this.passwordHash = passwordHash;
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() { return id; }

  public String getUsername() { return username; }

  public String getEmail() { return email; }

  public String getFullName() { return fullName; }

  public String getPasswordHash() { return passwordHash; }

  public LocalDateTime getCreatedAt() { return createdAt; }
}
