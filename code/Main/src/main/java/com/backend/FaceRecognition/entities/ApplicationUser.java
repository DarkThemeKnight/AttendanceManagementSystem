package com.backend.FaceRecognition.entities;

import com.backend.FaceRecognition.constants.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationUser implements UserDetails {
    @Id
    @JsonIgnore
    private String id;
    private String firstname;
    private String lastname;
    private String middleName;
    @Column(unique = true)
    private String schoolEmail;
    @JsonIgnore
    private String password;
    private String address;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles",joinColumns = @JoinColumn(name = "id"))
    private Set<Role> userRole;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private boolean isEnabled;
    private String profilePictureId;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRole.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }
    @Override
    public String getUsername() {
        return id;
    }
    public boolean addUserRole(Role role){
        if (userRole == null){
            userRole = new HashSet<>();
        }
        return userRole.add(role);
    }
    public boolean hasRole(Role role){
        return userRole.contains(role);
    }
}