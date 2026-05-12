package org.agora.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.agora.security.JwtService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Cek apakah ada header Authorization yang berawalan "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Ekstrak token (buang tulisan "Bearer " di depannya)
        jwt = authHeader.substring(7);

        // 3. Ekstrak email dari token
        userEmail = jwtService.extractUsername(jwt);

        // 4. Jika token ada email-nya, dan user belum terautentikasi di Context
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Ambil data user dari database (via UserDetailsService)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Validasi tokennya
            if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {

                // 6. Buat objek otentikasi
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 7. Simpan otentikasi tersebut ke dalam Security Context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Lanjutkan request ke tahap berikutnya
        filterChain.doFilter(request, response);
    }
}