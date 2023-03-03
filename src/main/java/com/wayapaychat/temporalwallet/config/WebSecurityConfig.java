package com.wayapaychat.temporalwallet.config;

import java.util.Arrays;

import com.wayapaychat.temporalwallet.security.AuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.
                cors().and().csrf().disable()
                // dont authenticate this particular request
                .authorizeRequests()
                //.antMatchers("/wallet/create/account","/api/v1/wallet/account/count/**", "/api/userjdbc/register/jdbc/add", "/api/v1/wallet/notify/**",
                //"/wallet/get/default/wallet/open/**","/wallet/create/cooperate/user","/api/v1/wallet/account/lookup/**","/temporal-service/api/v1/wallet/account/count/**", 
                //"/api/v1/wallet/user/account","/api/v1/wallet/transaction-count/{account}", "/api/v1/wallet/transaction/get-user-transaction-count/{userId}", "/api/v1/wallet/transaction/get-user-transaction-count", "/api/v1/wallet/create/cooperate/user","/api/users/register/admin", "/api/users/welcome").permitAll()
                //For Local Test Purpose
                .antMatchers("/v2/api-docs", "/configuration/**", "/swagger*/**","/actuator/**", "/webjars/**", "/api/v1/wallet/transaction/email").permitAll()
                // all other requests need to be authenticated
                .anyRequest().authenticated().and()
                .addFilter(new AuthorizationFilter(authenticationManager())).sessionManagement()

                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("*"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    
    @Bean
    public HttpFirewall defaultHttpFirewall() {
        return new DefaultHttpFirewall();
    }

}
