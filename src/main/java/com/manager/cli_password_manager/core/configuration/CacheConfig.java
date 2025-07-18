package com.manager.cli_password_manager.core.configuration;

import com.manager.cli_password_manager.core.exception.file.loader.FileCreatorException;
import com.manager.cli_password_manager.core.exception.file.loader.FileLoaderException;
import com.manager.cli_password_manager.core.service.file.creator.SecureDirectoryCreator;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.impl.config.persistence.DefaultPersistenceConfiguration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@Slf4j
@EnableCaching
@Configuration
public class CacheConfig {
    private static final String CACHE_NAME = "persist-file-ehcache";

    @Value("${shell.file.userHome}")
    private String userHome;
    @Value("${shell.file.rootDirectory}")
    private String directoryName;
    @Value("${shell.cache.cache-directory}")
    private String cacheNameDirectory;
    @Value("${shell.cache.size}")
    private int cacheSize;
    @Value("${shell.cache.expiration-time}")
    private int expirationTime;

    private File cacheDirectory;

    private final SecureDirectoryCreator directoryCreator;

    public CacheConfig(SecureDirectoryCreator directoryCreator) {
        this.directoryCreator = directoryCreator;
    }

    @PostConstruct
    public void init() {
        String homePath = System.getProperty(userHome);
        Path appDataDir = Paths.get(homePath, directoryName);

        if (!Files.exists(appDataDir))
            throw new FileLoaderException("Application directory not found");

        Path cacheDir = appDataDir.resolve(cacheNameDirectory);

        try {
            directoryCreator.createAndSecure(cacheDir);
            this.cacheDirectory = cacheDir.toFile();
        } catch (FileCreatorException e) {
            log.error("Cannot create cache directory: {}", e.getMessage());
            throw new FileLoaderException("Cannot create cache directory: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Load access file error: {}", e.getMessage());
            throw new FileLoaderException("Load access file error: " + e.getMessage(), e);
        }
    }

    @Bean(destroyMethod = "close")
    public javax.cache.CacheManager ehcacheManager() {
        CacheConfiguration<String, Boolean> ehCacheConfiguration = createEhcacheConfiguration();
        org.ehcache.config.Configuration ehcacheConfig = createEhConfiguration(ehCacheConfiguration);

        CachingProvider cachingProvider = Caching.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");
        EhcacheCachingProvider ehcacheCachingProvider = (EhcacheCachingProvider) cachingProvider;

        CacheManager cacheManager = ehcacheCachingProvider.getCacheManager(
                ehcacheCachingProvider.getDefaultURI(),
                ehcacheConfig
        );

        log.info("Кэш [{}] успешно сконфигурирован", CACHE_NAME);

        return cacheManager;
    }

    private CacheConfiguration<String, Boolean> createEhcacheConfiguration() {
        return CacheConfigurationBuilder
                .newCacheConfigurationBuilder(
                        String.class,
                        Boolean.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder().disk(cacheSize, MemoryUnit.MB, true))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(expirationTime)))
                .withDiskStoreThreadPool("check-cache-th", 2)
                .build();
    }

    private org.ehcache.config.Configuration createEhConfiguration(CacheConfiguration<String, Boolean> ehCacheConfiguration) {
        return ConfigurationBuilder.newConfigurationBuilder()
                .withCache(CACHE_NAME, ehCacheConfiguration)
                .withService(new DefaultPersistenceConfiguration(cacheDirectory))
                .build();
    }
}