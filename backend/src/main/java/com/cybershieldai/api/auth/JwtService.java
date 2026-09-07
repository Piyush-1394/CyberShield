package com.cybershieldai.api.auth;

import com.cybershieldai.api.common.AuthUser;
import com.cybershieldai.api.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtService {
    private final AppProperties props;

    public JwtService(AppProperties props) {
        this.props = props;
    }

    public String createAccessToken(AuthUser user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.jwt().accessTokenMinutes() * 60);
        return Jwts.builder()
                .subject(String.valueOf(user.userId()))
                .claim("orgId", user.organizationId())
                .claim("role", user.role().name())
                .claim("email", user.email())
                .claim("name", user.fullName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key())
                .compact();
    }

    public AuthUser parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        return new AuthUser(
                Long.parseLong(claims.getSubject()),
                claims.get("orgId", Number.class).longValue(),
                com.cybershieldai.api.common.Role.valueOf(claims.get("role", String.class)),
                claims.get("email", String.class),
                claims.get("name", String.class)
        );
    }

    private SecretKey key() {
        byte[] bytes = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            bytes = java.util.Arrays.copyOf(bytes, 32);
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
