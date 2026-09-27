package com.xtremealex.aeroport.web.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Internazionalizzazione IT/EN.
 *
 * La lingua si sceglie con il parametro ?lang=it|en (link a bandiera nel footer/nav)
 * e viene ricordata in un cookie: nessuna sessione server, nessuna profilazione.
 * Default italiano; se il cookie non c'è si prova la lingua del browser tra quelle
 * supportate, altrimenti IT.
 */
@Configuration
@EnableConfigurationProperties(WebProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private static final Locale IT = Locale.of("it");
    private static final Locale EN = Locale.of("en");
    private static final List<Locale> SUPPORTED = List.of(IT, EN);

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("XTR_LANG");
        resolver.setDefaultLocale(IT);
        resolver.setCookieMaxAge(Duration.ofDays(365));
        resolver.setCookiePath("/");
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /** Locale supportati, utili anche alle view per costruire lo switch lingua. */
    public static List<Locale> supported() {
        return SUPPORTED;
    }
}
