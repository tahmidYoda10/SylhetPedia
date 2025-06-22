package com.sylhetpedia.backend.service;

import com.sylhetpedia.backend.model.Otp;
import com.sylhetpedia.backend.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final JavaMailSender javaMailSender;
    private final OtpRepository otpRepository;

    public void generateOtp(String email) {
        try {
            String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

            Otp otpEntity = otpRepository.findByEmail(email).orElseGet(Otp::new);
            otpEntity.setEmail(email);
            otpEntity.setOtp(otp);
            otpEntity.setExpiryTime(expiry);

            otpRepository.save(otpEntity);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP code is: " + otp + " (valid for 5 minutes)");
            javaMailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Email send failed: " + e.getMessage());
        }
    }

    public boolean verifyOtp(String email, String inputOtp) {
        Optional<Otp> optionalOtp = otpRepository.findByEmail(email);
        if (optionalOtp.isPresent()) {
            Otp otpEntity = optionalOtp.get();
            if (otpEntity.getExpiryTime().isAfter(LocalDateTime.now())
                    && otpEntity.getOtp().equals(inputOtp)) {
                otpRepository.delete(otpEntity);
                return true;
            } else {
                otpRepository.delete(otpEntity);  // cleanup
            }
        }
        return false;
    }
}
