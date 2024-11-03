package com.ims.models.dtos;

import com.ims.models.Role;
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
    private Role role;
    private String profileImage;
    private Set<ItemDesignationAndCategoryDto> items;

    public UserDto() {}

    public UserDto(User user) {

        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.profileImage = user.getProfileImage();

    }

    public UserDto(String username, String email, Role role) {

        this.username = username;
        this.email = email;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return Objects.equals(username, userDto.username) &&
                Objects.equals(email, userDto.email) &&
                role == userDto.role &&
                Objects.equals(profileImage, userDto.profileImage) &&
                Objects.equals(items, userDto.items);
    }

    @Override
    public int hashCode() {

        return Objects.hash(username, email, role, profileImage, items);
    }
}
