package com.info7255.medicalplanapi.service;

import com.info7255.medicalplanapi.utils.GooglePublicKeyProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.io.IOException;

@Service
public class AuthService {

    private final GooglePublicKeyProvider keyProvider;
    private String errorMessage;

    public AuthService() throws IOException {
        keyProvider = new GooglePublicKeyProvider();
    }

    public Boolean authorizeToken(@RequestHeader HttpHeaders headers) {
        String authorizationHeader = headers.getFirst("Authorization");
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            errorMessage = "Token missing";
            return false;
        }

        try {
            Algorithm algorithm = Algorithm.RSA256(keyProvider);
            JWTVerifier verifier = JWT.require(algorithm).build();
            String token = authorizationHeader.substring(7);
            DecodedJWT jwt = verifier.verify(token);
            return true;
        } catch (JWTVerificationException exception) {
            errorMessage = "Token invalid!!";
            return false;
        }
    }

    public String getErrorMessage(){
        return errorMessage;
    }
}
