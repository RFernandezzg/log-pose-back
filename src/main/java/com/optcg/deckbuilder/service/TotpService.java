package com.optcg.deckbuilder.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

@Service
public class TotpService {

    private final GoogleAuthenticator gAuth;

    public TotpService() {
        this.gAuth = new GoogleAuthenticator();
    }

    public GoogleAuthenticatorKey generateSecret() {
        return gAuth.createCredentials();
    }

    public String getQrCodeUrl(GoogleAuthenticatorKey key, String username) {
        return String.format("otpauth://totp/OPTCG_Deck_Builder:%s?secret=%s&issuer=OPTCG_Deck_Builder", username, key.getKey());
    }

    public boolean validateCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}
