package com.digitalsignage.admin.security;

import com.digitalsignage.admin.common.enums.UserRole;
import com.digitalsignage.admin.common.exception.BusinessException;
import com.digitalsignage.admin.entity.SysUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    static final String CLAIM_ORG_ID = "orgId";
    static final String CLAIM_ROLE = "role";
    static final String CLAIM_TYP = "typ";
    static final String TYP_ACCESS = "access";
    static final String TYP_REFRESH = "refresh";

    private final JwtProperties jwtProperties;

    public String createAccessToken(SysUser user) {
        return buildToken(user, TYP_ACCESS, minutesToMillis(jwtProperties.getAccessTokenMinutes()));
    }

    public String createRefreshToken(SysUser user) {
        return buildToken(user, TYP_REFRESH, daysToMillis(jwtProperties.getRefreshTokenDays()));
    }

    public AdminPrincipal parseAccessToken(String token) {
        Claims claims = parseAndValidate(token, TYP_ACCESS);
        return toPrincipal(claims);
    }

    public Long parseRefreshTokenUserId(String token) {
        Claims claims = parseAndValidate(token, TYP_REFRESH);
        return Long.parseLong(claims.getSubject());
    }

    private Claims parseAndValidate(String token, String expectedTyp) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String typ = claims.get(CLAIM_TYP, String.class);
            if (!expectedTyp.equals(typ)) {
                throw new BusinessException(401, "invalid token");
            }
            return claims;
        } catch (ExpiredJwtException e) {
            throw new BusinessException(401, "token expired");
        } catch (MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new BusinessException(401, "invalid token");
        }
    }

    private AdminPrincipal toPrincipal(Claims claims) {
        Long userId = Long.parseLong(claims.getSubject());
        Long orgId = claims.get(CLAIM_ORG_ID, Long.class);
        String username = claims.get("username", String.class);
        UserRole role = UserRole.valueOf(claims.get(CLAIM_ROLE, String.class));
        return AdminPrincipal.builder()
                .userId(userId)
                .organizationId(orgId)
                .username(username)
                .role(role)
                .build();
    }

    private String buildToken(SysUser user, String typ, long ttlMillis) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMillis);
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_ORG_ID, user.getOrganization().getId())
                .claim("username", user.getUsername())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TYP, typ)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey())
                .compact();
    }

    private SecretKey signingKey() {
        byte[] bytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes for HS256");
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    private static long minutesToMillis(int minutes) {
        return minutes * 60L * 1000L;
    }

    private static long daysToMillis(int days) {
        return days * 24L * 60L * 60L * 1000L;
    }
}
