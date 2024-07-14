package com.info7255.medicalplanapi.service;

import com.google.api.client.json.webtoken.JsonWebSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import com.google.auth.oauth2.TokenVerifier;

@Service
public class AuthService {

    private String errorMessage;
    @Value("${secret.CLIENT_ID}")
    private String clientID;


    public Boolean authorizeToken(@RequestHeader HttpHeaders headers) {
        String authorizationHeader = headers.getFirst("Authorization");
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            errorMessage = "Token missing";
            return false;
        }

        try {
            String token = authorizationHeader.substring(7);
            System.out.println("token:"+ token);
            TokenVerifier tokenVerifier = TokenVerifier.newBuilder()
                    .setAudience(clientID)
                    .build();

            JsonWebSignature verifiedIdToken = tokenVerifier.verify(token);
            System.out.println("Token verified successfully: " + verifiedIdToken.getPayload());
            return true;
        } catch (Exception e) {
            System.out.println("Token verification failed: " + e.getMessage());
            errorMessage = "Token invalid!!  "+e.getMessage();
            return false;
        }
    }

    public String getErrorMessage(){
        return errorMessage;
    }


}
