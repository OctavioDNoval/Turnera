package com.turnera.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.SecurityContext;
import com.turnera.entity.Teacher;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtTokenServiceTest {

    private static final String SECRET = "test-secret-at-least-32-bytes-long-for-hs256";

    @Test
    void issuedTokenContainsSubjectAndNameAndExpires() {
        SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(key.getEncoded())
                .algorithm(JWSAlgorithm.HS256)
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        JwtEncoder encoder = new NimbusJwtEncoder(jwkSource);
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        JwtTokenService service = new JwtTokenService(encoder, 60);

        Teacher teacher = new Teacher();
        teacher.setUsername("profesor");
        teacher.setName("Pepe");

        String token = service.issueToken(teacher);

        assertThat(token).isNotBlank();

        Jwt jwt = decoder.decode(token);
        assertThat(jwt.getSubject()).isEqualTo("profesor");
        assertThat(jwt.getClaimAsString("name")).isEqualTo("Pepe");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("turnera");
        assertThat(jwt.getExpiresAt()).isAfter(Instant.now());
    }
}