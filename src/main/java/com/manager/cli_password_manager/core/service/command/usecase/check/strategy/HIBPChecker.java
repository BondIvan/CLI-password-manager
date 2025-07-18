package com.manager.cli_password_manager.core.service.command.usecase.check.strategy;

import com.manager.cli_password_manager.core.entity.enums.CheckingApi;
import com.manager.cli_password_manager.core.exception.checker.CheckerException;
import com.manager.cli_password_manager.core.exception.checker.HIBPCheckerException;
import com.manager.cli_password_manager.core.service.command.usecase.check.ApiLimiter;
import com.manager.cli_password_manager.core.service.command.usecase.check.Checker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HIBPChecker implements Checker {
    private static final String ALGORITHM = "SHA-1";
    private static final int CONNECTION_TIMEOUT = 10;

    @Value("${api.hibp.url}")
    private String url;

    private static final HttpClient HTTP_CLIENT;

    static {
        HTTP_CLIENT = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT))
                .build();
    }

    private final ApiLimiter limiter;
    private HIBPChecker self; //TODO Проверить работу с self

    public HIBPChecker(@Qualifier("HIBPChecker") ApiLimiter apiLimiter) {
        this.limiter = apiLimiter;
    }

    @Override
    public boolean isPwned(String password) throws CheckerException {
        try {
            String hash = makeSHA1Hash(password);
            return self.checkHashViaHIBP(hash);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HIBPCheckerException("Checking was interrupted", e);
        } catch (Exception e) {
            throw new HIBPCheckerException(e);
        }
    }

    @Override
    public CheckingApi getType() {
        return CheckingApi.HIBP;
    }

    @Cacheable(
         cacheNames = "persist-file-ehcache",
            key = "#root.target.getShortHashForCache(fullHash)"
    )
    public boolean checkHashViaHIBP(String fullHash) throws InterruptedException {
        log.info("Значение [{}] не найдено в кэше, проверяем вручную", getShortHashForCache(fullHash));
        limiter.doDelay();
        return viaHIBP(fullHash);
    }

    private boolean viaHIBP(String hash) {
        String firstFive = hash.substring(0, 5);
        String checkURL = url + firstFive;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(checkURL))
                    .timeout(Duration.ofSeconds(CONNECTION_TIMEOUT))
                    .header("User-Agent", "PassManager")
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200) {
                log.error("HTTP response code for request is not 200");
                throw new HIBPCheckerException("HTTP response code for request is not 200");
            }

            String[] hashesSplitByLineBreak = response.body().split("\n");
            Set<String> hashes = Arrays.stream(hashesSplitByLineBreak)
                    .map(line -> line.split(":")[0])
                    .collect(Collectors.toSet());

            return hashes.contains(hash.substring(5));
        } catch (Exception e) {
            log.error("Send HTTP request error: {}", e.getMessage());
            throw new HIBPCheckerException("Send HTTP request error: " + e.getMessage(), e);
        }
    }

    public String makeSHA1Hash(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);
            byte[] MDBytes = messageDigest.digest(password.getBytes());
            BigInteger number = new BigInteger(1, MDBytes);
            StringBuilder hashNumber = new StringBuilder(number.toString(16));

            return processingHashNumberLengthCorrect(hashNumber).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            log.error("Cannot make a hash from the password: {}", e.getMessage());
            throw new HIBPCheckerException("Cannot make a hash from the password: " + e.getMessage(), e);
        }
    }

    // Add preceding 0s to make it 40 bit
    private String processingHashNumberLengthCorrect(StringBuilder hashNumber) {
        while (hashNumber.length() < 40)
            hashNumber.insert(0, "0");

        return hashNumber.toString();
    }

    public String getShortHashForCache(String fullHash) {
        return fullHash.substring(0, 5) + fullHash.substring(fullHash.length() - 5);
    }
}
