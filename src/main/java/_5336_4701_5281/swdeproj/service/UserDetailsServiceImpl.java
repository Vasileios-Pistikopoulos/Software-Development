package _5336_4701_5281.swdeproj.service;

import _5336_4701_5281.swdeproj.model.User;
import _5336_4701_5281.swdeproj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🚀 Trying to authenticate user: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("❌ User not found: " + username);
                    return new UsernameNotFoundException("User not found");
                });

        System.out.println("✅ Found user: " + user.getUsername());
        System.out.println("🧂 Encrypted password: " + user.getPassword());
        System.out.println("🔐 Roles: " + user.getRoles());

        // Ensure both role and roles set are considered
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
        }
        user.getRoles().forEach(role -> 
            authorities.add(new SimpleGrantedAuthority(role.name()))
        );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

}