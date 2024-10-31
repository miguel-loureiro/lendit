package com.ims.models.dtos;

import com.ims.models.Type;
import com.ims.models.User;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
public class UserDto {

    private String username;
    private String email;
    private Type type;
    private String profileImage;
    private Set<ItemDesignationAndCategoryDto> items;

    public UserDto() {}

    public UserDto(User user) {

        this.username = user.getUsername();
        this.email = user.getEmail();
        this.type = user.getType();
        this.profileImage = user.getProfileImage();
        this.items = user.getItems() != null
                ? user.getItems().stream()
                .map(item -> new ItemDesignationAndCategoryDto(item.getDesignation(), item.getCategory()))
                .collect(Collectors.toSet())
                : new HashSet<>();
    }

    public UserDto(String username, String email, Type type) {

        this.username = username;
        this.email = email;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return Objects.equals(username, userDto.username) &&
                Objects.equals(email, userDto.email) &&
                type == userDto.type &&
                Objects.equals(profileImage, userDto.profileImage) &&
                Objects.equals(items, userDto.items);
    }

    @Override
    public int hashCode() {

        return Objects.hash(username, email, type, profileImage, items);
    }
}
