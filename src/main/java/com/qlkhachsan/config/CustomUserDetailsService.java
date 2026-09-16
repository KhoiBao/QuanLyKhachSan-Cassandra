package com.qlkhachsan.config;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.qlkhachsan.model.AppUser;
import com.qlkhachsan.repository.UserStore;

/**
 * Doc tai khoan tu kho du lieu (Store) de Spring Security xac thuc.
 * Mat khau da bam BCrypt nen Security tu so sanh voi mat khau nhap vao.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserStore userStore;

    public CustomUserDetailsService(UserStore userStore) {
        this.userStore = userStore;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userStore.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Khong ton tai tai khoan: " + username));
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash() == null ? "" : user.getPasswordHash())
                .disabled(!user.isActive())
                .authorities(new SimpleGrantedAuthority(user.getRole().getAuthority()))
                .build();
    }
}
