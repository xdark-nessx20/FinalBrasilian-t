package co.edu.unimagdalena.finalbrasiliant.security.service;

import co.edu.unimagdalena.finalbrasiliant.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = repo.findByEmailIgnoreCase(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        var authority = new SimpleGrantedAuthority(user.getRole().name());
        return User.withUsername(user.getEmail()).password(user.getPasswordHash())
                .authorities(authority).build();
    }
}
