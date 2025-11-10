package com.manager.cli_password_manager.core.entity;

import com.manager.cli_password_manager.core.entity.enums.Category;
import de.huxhorn.sulky.ulid.ULID;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Note {
    private static final ULID ULID = new ULID();

    private final String id;
    private String name;
    private String login;
    private String password; // only encrypted password
    private Category category;

    public Note() {
        this.id = ULID.nextULID();
        this.category = Category.NO_CATEGORY;
    }

    // Full copy constructor
    public Note(Note other) {
        this.id = other.getId();;
        this.name = other.getName();
        this.login = other.getLogin();
        this.password = other.getPassword();
        this.category = other.getCategory();
    }

    // Wither methods
    public Note withName(String newName) {
        Note newNote = new Note(this);
        newNote.setName(newName);
        return newNote;
    }

    public Note withLogin(String newLogin) {
        Note newNote = new Note(this);
        newNote.setLogin(newLogin);
        return newNote;
    }

    public Note withPassword(String newPassword) {
        Note newNote = new Note(this);
        newNote.setPassword(newPassword);
        return newNote;
    }

    public Note withCategory(Category category) {
        Note newNote = new Note(this);
        newNote.setCategory(category);
        return newNote;
    }

    @Override
    public String toString() {
        return String.format("[Id: %s| Name: %s| Category: %s| Login: %s| Password: %s]",
                id, name, category, login, "*****");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note note)) return false;
        return Objects.equals(name, note.name) && Objects.equals(login, note.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, login);
    }
}
