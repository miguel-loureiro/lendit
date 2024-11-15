package com.ims.services;

import com.ims.security.CustomUserDetails;
import com.ims.models.User;
import com.ims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Add debug logging
        System.out.println("Attempting to load user with username: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Add debug logging
        System.out.println("Found user: " + user.getUsername() + " with role: " + user.getRole());

        return new CustomUserDetails(user);
    }
}
