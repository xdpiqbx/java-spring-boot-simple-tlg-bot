package com.dpiqb.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.sql.Timestamp;

@Entity(name="userData")
@Data
public class User {
  @Id
  private long chatId;
  private String firstName;
  private String lastName;
  private String userName;
  private Timestamp registerAt;
}
