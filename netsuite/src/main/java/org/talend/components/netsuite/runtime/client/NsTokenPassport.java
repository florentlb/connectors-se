package org.talend.components.netsuite.runtime.client;

import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.RandomStringUtils;

import lombok.Data;

@Data
public class NsTokenPassport {

    protected String account;

    protected String consumerKey;

    protected String token;

    protected String nonce;

    protected long timestamp;

    protected NsTokenPassportSignature signature;

    private String secret;

    public NsTokenPassport(String account, String consumerKey, String consumerSecret, String token,
            String tokenSecret) {
        this.account = account;
        this.consumerKey = consumerKey;
        this.token = token;
        this.secret = String.join("&", consumerSecret, tokenSecret);
        this.signature = new NsTokenPassportSignature();
        signature.setAlgorithm("Hmac_SHA256");
    }

    public String refresh() {
        try {
            this.nonce = generateNonce();
            this.timestamp = Instant.now().getEpochSecond();
            String baseString = String.join("&", account, consumerKey, token, nonce, String.valueOf(timestamp));
            return computeShaHash(baseString, secret, signature.getAlgorithm());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generateNonce() {
        return RandomStringUtils.randomAlphanumeric(40);
    }

    private String computeShaHash(String baseString, String key, String algorithm) throws Exception {
        byte[] bytes = key.getBytes();
        SecretKeySpec mySigningKey = new SecretKeySpec(bytes, algorithm);
        Mac messageAuthenticationCode = Mac.getInstance(algorithm);
        messageAuthenticationCode.init(mySigningKey);
        byte[] hash = messageAuthenticationCode.doFinal(baseString.getBytes());
        String result = Base64.getEncoder().encodeToString(hash);
        return result;
    }
}
