package com.ims.services;

import com.ims.models.CustomUserDetails;
import com.ims.models.Type;
import com.ims.models.User;
import com.ims.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if ("guestuser".equals(username)) {

            User guestUser = new User();
            guestUser.setUsername("guestuser");
            guestUser.setType(Type.GUEST);
            return new CustomUserDetails(guestUser);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username : " + username));
        return new CustomUserDetails(user);
    }
}
