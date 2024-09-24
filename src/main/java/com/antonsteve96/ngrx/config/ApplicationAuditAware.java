package com.antonsteve96.ngrx.config;

import com.antonsteve96.ngrx.user.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;

/**
 * This class is used to get the current user who is performing the action.
 */
@Configuration
public class ApplicationAuditAware implements AuditorAware<Long> {

    @Override
    @NonNull
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return Optional.of(0L);
        }
        if (authentication.getPrincipal() instanceof User user) {
            return Optional.ofNullable(user.getId());
        } else {
            User user = (User) authentication.getPrincipal();
            return Optional.of(user.getId());
        }
    }

}