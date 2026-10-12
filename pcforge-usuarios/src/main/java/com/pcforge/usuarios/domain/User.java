package com.pcforge.usuarios.domain;

import java.time.Instant;
import java.util.Set;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * Cuenta de usuario de la plataforma. La contrasena nunca se guarda en texto
 * plano: se almacena unicamente su hash BCrypt.
 */
@Document(collection = "users")
public class User {
    @Id
    private String id;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120)
    private String fullName;
    @Indexed(unique = true)
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 160)
    private String email;
    /**
     * Hash BCrypt de la contrasena. Nunca se expone en la API.
     */
    @NotBlank
    private String passwordHash;
    @NotEmpty(message = "El usuario debe tener al menos un rol")
    private Set<Role> roles;
    private boolean active;
    private Instant lastLoginAt;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public boolean canAuthenticate() {
        return active;
    }

    private static boolean $default$active() {
        return true;
    }

    public static class UserBuilder {

        private String id;

        private String fullName;

        private String email;

        private String passwordHash;

        private Set<Role> roles;

        private boolean active$set;

        private boolean active$value;

        private Instant lastLoginAt;

        private Instant createdAt;

        private Instant updatedAt;

        UserBuilder() {
        }

        /**
         * @return {@code this}.
         */

        public User.UserBuilder id(final String id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public User.UserBuilder fullName(final String fullName) {
            this.fullName = fullName;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public User.UserBuilder email(final String email) {
            this.email = email;
            return this;
        }

        /**
         * Hash BCrypt de la contrasena. Nunca se expone en la API.
         * @return {@code this}.
         */

        public User.UserBuilder passwordHash(final String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public User.UserBuilder roles(final Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public User.UserBuilder active(final boolean active) {
            this.active$value = active;
            active$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public User.UserBuilder lastLoginAt(final Instant lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public User.UserBuilder createdAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */

        public User.UserBuilder updatedAt(final Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public User build() {
            boolean active$value = this.active$value;
            if (!this.active$set) active$value = User.$default$active();
            return new User(this.id, this.fullName, this.email, this.passwordHash, this.roles, active$value, this.lastLoginAt, this.createdAt, this.updatedAt);
        }

        @java.lang.Override

        public java.lang.String toString() {
            return "User.UserBuilder(id=" + this.id + ", fullName=" + this.fullName + ", email=" + this.email + ", passwordHash=" + this.passwordHash + ", roles=" + this.roles + ", active$value=" + this.active$value + ", lastLoginAt=" + this.lastLoginAt + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ")";
        }
    }

    public static User.UserBuilder builder() {
        return new User.UserBuilder();
    }

    public String getId() {
        return this.id;
    }

    public String getFullName() {
        return this.fullName;
    }

    public String getEmail() {
        return this.email;
    }

    /**
     * Hash BCrypt de la contrasena. Nunca se expone en la API.
     */

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public Set<Role> getRoles() {
        return this.roles;
    }

    public boolean isActive() {
        return this.active;
    }

    public Instant getLastLoginAt() {
        return this.lastLoginAt;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Hash BCrypt de la contrasena. Nunca se expone en la API.
     */

    public void setPasswordHash(final String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRoles(final Set<Role> roles) {
        this.roles = roles;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setLastLoginAt(final Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @java.lang.Override

    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof User)) return false;
        final User other = (User) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (this.isActive() != other.isActive()) return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final java.lang.Object this$fullName = this.getFullName();
        final java.lang.Object other$fullName = other.getFullName();
        if (this$fullName == null ? other$fullName != null : !this$fullName.equals(other$fullName)) return false;
        final java.lang.Object this$email = this.getEmail();
        final java.lang.Object other$email = other.getEmail();
        if (this$email == null ? other$email != null : !this$email.equals(other$email)) return false;
        final java.lang.Object this$passwordHash = this.getPasswordHash();
        final java.lang.Object other$passwordHash = other.getPasswordHash();
        if (this$passwordHash == null ? other$passwordHash != null : !this$passwordHash.equals(other$passwordHash)) return false;
        final java.lang.Object this$roles = this.getRoles();
        final java.lang.Object other$roles = other.getRoles();
        if (this$roles == null ? other$roles != null : !this$roles.equals(other$roles)) return false;
        final java.lang.Object this$lastLoginAt = this.getLastLoginAt();
        final java.lang.Object other$lastLoginAt = other.getLastLoginAt();
        if (this$lastLoginAt == null ? other$lastLoginAt != null : !this$lastLoginAt.equals(other$lastLoginAt)) return false;
        final java.lang.Object this$createdAt = this.getCreatedAt();
        final java.lang.Object other$createdAt = other.getCreatedAt();
        if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
        final java.lang.Object this$updatedAt = this.getUpdatedAt();
        final java.lang.Object other$updatedAt = other.getUpdatedAt();
        if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof User;
    }

    @java.lang.Override

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isActive() ? 79 : 97);
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final java.lang.Object $fullName = this.getFullName();
        result = result * PRIME + ($fullName == null ? 43 : $fullName.hashCode());
        final java.lang.Object $email = this.getEmail();
        result = result * PRIME + ($email == null ? 43 : $email.hashCode());
        final java.lang.Object $passwordHash = this.getPasswordHash();
        result = result * PRIME + ($passwordHash == null ? 43 : $passwordHash.hashCode());
        final java.lang.Object $roles = this.getRoles();
        result = result * PRIME + ($roles == null ? 43 : $roles.hashCode());
        final java.lang.Object $lastLoginAt = this.getLastLoginAt();
        result = result * PRIME + ($lastLoginAt == null ? 43 : $lastLoginAt.hashCode());
        final java.lang.Object $createdAt = this.getCreatedAt();
        result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
        final java.lang.Object $updatedAt = this.getUpdatedAt();
        result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
        return result;
    }

    @java.lang.Override

    public java.lang.String toString() {
        return "User(id=" + this.getId() + ", fullName=" + this.getFullName() + ", email=" + this.getEmail() + ", passwordHash=" + this.getPasswordHash() + ", roles=" + this.getRoles() + ", active=" + this.isActive() + ", lastLoginAt=" + this.getLastLoginAt() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
    }

    public User() {
        this.active = User.$default$active();
    }

    /**
     * Creates a new {@code User} instance.
     *
     * @param id
     * @param fullName
     * @param email
     * @param passwordHash Hash BCrypt de la contrasena. Nunca se expone en la API.
     * @param roles
     * @param active
     * @param lastLoginAt
     * @param createdAt
     * @param updatedAt
     */

    public User(final String id, final String fullName, final String email, final String passwordHash, final Set<Role> roles, final boolean active, final Instant lastLoginAt, final Instant createdAt, final Instant updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = roles;
        this.active = active;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
