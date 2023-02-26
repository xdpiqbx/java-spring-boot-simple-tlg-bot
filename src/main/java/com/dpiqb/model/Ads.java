package com.dpiqb.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name = "ads")
@Data
public class Ads {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;
  private String ad;
}
