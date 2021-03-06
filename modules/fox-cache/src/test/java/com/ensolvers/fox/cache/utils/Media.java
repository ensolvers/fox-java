package com.ensolvers.fox.cache.utils;

import java.util.Objects;

public class Media {
    private Long id;
    private String title;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Media media = (Media) o;
        return id.equals(media.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
