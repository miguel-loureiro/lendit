package com.ims.models.dtos;

import com.ims.models.Item;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@NoArgsConstructor
public class ItemDesignationAndCategoryDto {

    @Getter
    @Setter
    private String designation;
    @Getter
    @Setter
    private Item.Category category;

    public ItemDesignationAndCategoryDto(String designation, Item.Category category) {
        this.designation = designation;
        this.category = category;
    }

    public ItemDesignationAndCategoryDto(Item item) {
        this.designation = item.getDesignation();
        this.category = item.getCategory();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemDesignationAndCategoryDto that = (ItemDesignationAndCategoryDto) o;
        return Objects.equals(designation, that.designation) && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(designation, category);
    }
}

