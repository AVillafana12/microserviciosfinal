package com.clinic.appointment.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;
import java.util.stream.Collectors;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("preferred_username"));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // Extraer roles de realm_access
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> realmAccessMap = (Map<String, Object>) realmAccess;
            Object roles = realmAccessMap.get("roles");
            if (roles instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<String> rolesList = (Collection<String>) roles;
                authorities.addAll(
                        rolesList.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                .collect(Collectors.toList())
                );
            }
        }

        // Extraer roles de client_access (para clientes específicos si existen)
        Object clientAccess = jwt.getClaim("client_access");
        if (clientAccess instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientAccessMap = (Map<String, Object>) clientAccess;
            Object roles = clientAccessMap.get("roles");
            if (roles instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<String> rolesList = (Collection<String>) roles;
                authorities.addAll(
                        rolesList.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                .collect(Collectors.toList())
                );
            }
        }

        // Si no hay roles, agregar rol anonymous
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
        }

        return authorities;
    }
}
