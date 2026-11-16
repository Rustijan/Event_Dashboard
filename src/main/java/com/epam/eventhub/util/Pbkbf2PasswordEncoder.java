package com.epam.eventhub.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public class Pbkbf2PasswordEncoder implements PasswordEncoder{
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 600_000;
    private static final int KEY_LENGTH = 256;

    private final SecureRandom secureRandom = new SecureRandom();


    @Override
    public String encode(String rawPassword){
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        byte[] hash = hashPassword(rawPassword, salt);
        return ITERATIONS + ":" +
                Base64.getEncoder().encodeToString(salt)+":" +
                Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword){
        if(rawPassword == null || encodedPassword == null){
            return false;
        }
        String[] parts = encodedPassword.split(":");
        if(parts.length != 3){
            return false;
        }
        try{
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt;
            byte[] expectedHash;

            if(parts[1].matches("[0-9a-fA-F]{32}")
                && parts[2].matches("[0-9a-fA-F]{64}")){
                salt  = HexFormat.of().parseHex(parts[1]);
                expectedHash = HexFormat.of().parseHex(parts[2]);
            }else{
                salt = Base64.getDecoder().decode(parts[1]);
                expectedHash = Base64.getDecoder().decode(parts[2]);
            }
            byte[] actualHash= hashPassword(rawPassword, salt, iterations);
            if(actualHash.length != expectedHash.length){
                return false;
            }
            int difference = 0;
            for(int i = 0; i < actualHash.length; i++){
                difference |= actualHash[i] ^ expectedHash[i];
            }
            return difference ==0;

        } catch(IllegalArgumentException e){
            return false;
        }
    }



    private byte[] hashPassword(String password, byte[] salt){
        return hashPassword(password, salt, ITERATIONS);
    }
    private byte[] hashPassword(String password, byte[] salt, int iterations){

        if(password==null){
            throw new IllegalArgumentException("Password cannot be null");
        }
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                KEY_LENGTH
                );

        try{
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();

        }catch(GeneralSecurityException e){
            throw new IllegalStateException("Could not hash password", e);
        }finally{
            spec.clearPassword();
        }
    }
}
