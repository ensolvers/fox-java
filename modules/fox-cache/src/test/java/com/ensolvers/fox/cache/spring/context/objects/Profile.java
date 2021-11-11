package com.ensolvers.fox.cache.spring;

import java.util.Objects;

public class Profile {
  private Long id;
  private String name;
  private Media media;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Media getMedia() {
    return media;
  }

  public void setMedia(Media media) {
    this.media = media;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Profile profile = (Profile) o;
    return id.equals(profile.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
