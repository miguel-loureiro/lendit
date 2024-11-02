package com.ims.services;

import com.ims.models.CustomUserDetails;
import com.ims.models.Role;
import com.ims.models.User;
import com.ims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Add debug logging
        System.out.println("Attempting to load user with email: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Add debug logging
        System.out.println("Found user: " + user.getUsername() + " with role: " + user.getRole());

        return new CustomUserDetails(user);
    }
}
