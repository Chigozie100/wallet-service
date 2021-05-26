package com.wayapaychat.temporalwallet.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
//    }

//    @Bean
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.
                cors().and().csrf().disable()
                // dont authenticate this particular request
                .authorizeRequests().antMatchers(
                "/wallet/create/account", "/api/userjdbc/register/jdbc/add","/wallet/get/default/wallet/open/**", "/api/users/register/admin", "/api/users/welcome").permitAll()
                .antMatchers("/v2/api-docs", "/configuration/**", "/swagger*/**","/actuator/**", "/webjars/**").permitAll()
                // all other requests need to be authenticated
                .anyRequest().authenticated().and()

//                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
//                .and()
                // make sure we use stateless session; session won't be used to
                // store user's state.
//                .addFilter(getAuthenticationFilter())
                .addFilter(new AuthorizationFilter(authenticationManager())).sessionManagement()

                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    }

//    private AuthenticationFilter getAuthenticationFilter() throws Exception {
//        //JwtRequestFilter filter = new JwtRequestFilter(authenticationManager());
//        final AuthenticationFilter filter = new AuthenticationFilter(authenticationManager());
//        filter.setFilterProcessesUrl("/api/users/login");
//        return filter;
//    }

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