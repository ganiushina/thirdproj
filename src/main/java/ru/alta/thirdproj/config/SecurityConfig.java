package ru.alta.thirdproj.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import ru.alta.thirdproj.services.UserBonusServiceImpl;
import ru.alta.thirdproj.services.UserLoginServiceImpl;
import ru.alta.thirdproj.services.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private UserLoginServiceImpl userLoginService;
    private CustomAccessDeniedHandler customAccessDeniedHandler;


    @Autowired
    public  void setUserLoginService (@Lazy UserLoginServiceImpl userLoginService){this.userLoginService = userLoginService;}

    @Autowired
    public void setCustomAuthenticationSuccessHandler(CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    @Autowired
    public void setCustomAuthenticationFailureHandler(CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    }

    @Autowired
    public void setCustomAccessDeniedHandler(CustomAccessDeniedHandler customAccessDeniedHandler){
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // Отключаем CSRF
                .authorizeRequests()
                .antMatchers("/payment/confirm").hasRole("BUHADMIN")
                .antMatchers("/act/allact").hasRole("BUHADMIN")
                .antMatchers("/margin/detailed").hasAnyRole("BDM", "BUHADMIN", "MANAGER")
                .antMatchers("/margin/charts").hasAnyRole("BDM", "BUHADMIN", "MANAGER")
                .antMatchers("/margin/interpreters").hasAnyRole("BUHADMIN", "MANAGER")
                // только MANAGER может добавлять схемы (POST)
                .antMatchers(HttpMethod.POST, "/bonus-schemes").hasRole("MANAGER")
                // просмотр страницы бонусных схем — любому аутентифицированному
                .antMatchers(HttpMethod.GET, "/bonus-schemes").authenticated()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginProcessingUrl("/index")
                .successHandler(customAuthenticationSuccessHandler)
                .failureHandler(customAuthenticationFailureHandler)
                .permitAll()
                .and()
                .logout()
                .logoutSuccessUrl("/**");

    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userLoginService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

}