package com.rj.ecommerce_backend.securityconfig.services;

import com.rj.ecommerce.api.shared.core.Email;
import com.rj.ecommerce_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String emailString) throws UsernameNotFoundException {
        Email email = Email.Companion.of(emailString);
        return userRepository.findUserByEmail(email)
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + email.value()));
    }
}
