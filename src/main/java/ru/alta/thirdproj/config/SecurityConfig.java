package ru.alta.thirdproj.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import ru.alta.thirdproj.services.UserBonusServiceImpl;
import ru.alta.thirdproj.services.UserLoginServiceImpl;
import ru.alta.thirdproj.services.UserServiceImpl;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

   private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
  //  private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private UserLoginServiceImpl userLoginService;

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

    @Autowired
    public  void setUserLoginService (@Lazy UserLoginServiceImpl userLoginService){this.userLoginService = userLoginService;}

    @Autowired
    public void setCustomAuthenticationSuccessHandler(CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests()
//
//                .and()
//                .formLogin()
//
//                .loginProcessingUrl("/user")
//                .successHandler(customAuthenticationSuccessHandler)
//                .failureHandler(authenticationFailureHandler())
//                .permitAll()
//                .and()
//                .logout()
//                .logoutSuccessUrl("/user")
//                .permitAll();
        http
                //.csrf()
               // .disable()
                .authorizeRequests()
              //  .antMatchers("/registration").not().fullyAuthenticated()
                //Доступ только для пользователей с ролью Администратор
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest()
                .authenticated()
                .and()
                //Настройка для входа в систему
                .formLogin()
                .failureHandler(authenticationFailureHandler())
 //               .loginPage("/login")
                //Перенарпавление на главную страницу после успешного входа
                .successHandler(customAuthenticationSuccessHandler)
                .defaultSuccessUrl("/user")

                .permitAll()
                .and()
                .logout()
                .permitAll()
                .logoutSuccessUrl("/bonus");

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