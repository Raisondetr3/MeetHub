package ru.itmo.cs.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.itmo.cs.service.JwtService;
import ru.itmo.cs.service.UserService;

import java.io.IOException;

/**
 * Фильтр для аутентификации с использованием JWT.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        jwtToken = authHeader.substring(7);
        username = jwtService.extractUsername(jwtToken);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Аутентифицируем пользователя через AuthenticationManager
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, null);
            authToken.setDetails(jwtService.isTokenValid(jwtToken, username));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        chain.doFilter(request, response);
    }
}
