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
import ru.alta.thirdproj.services.UserBonusServiceImpl;
import ru.alta.thirdproj.services.UserLoginServiceImpl;
import ru.alta.thirdproj.services.UserServiceImpl;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private DataSource dataSource;
    private UserBonusServiceImpl userBonusService;
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private UserServiceImpl userService;

    private UserLoginServiceImpl userLoginService;

//    private JwtAuthFilter jwtAuthFilter;
//
//    @Autowired
//    private setJwtAuthFilter jwtAuthFilter;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Autowired
    public void setUseBonusService(UserBonusServiceImpl userService) {
        this.userBonusService = userService;
    }

    @Autowired
    public void setUserService(@Lazy UserServiceImpl userService){ this.userService = userService;}

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
        http
                //.csrf()
               // .disable()
                .authorizeRequests()
              //  .antMatchers("/registration").not().fullyAuthenticated()
                //Доступ только для пользователей с ролью Администратор
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                //Настройка для входа в систему
                .formLogin()
 //               .loginPage("/login")
                //Перенарпавление на главную страницу после успешного входа
                .defaultSuccessUrl("/user")
                .permitAll()
                .and()
                .logout()
                .permitAll()
                .logoutSuccessUrl("/");
//                .anyRequest().permitAll();
//                .antMatchers("/register/**").permitAll()
//                .antMatchers("/admin/**").hasRole("ADMIN")
//                .antMatchers("/products/**").hasRole("ADMIN")
//                .antMatchers("/shop/order/**").authenticated()
//                .antMatchers("/all/**").authenticated()
//                .and()
//                .formLogin()
//             //   .loginPage("/login")
//                .loginProcessingUrl("/authenticateTheUser")
//                .successHandler(customAuthenticationSuccessHandler)
//                .permitAll();
//                .and()
//                .logout()
//                .logoutSuccessUrl("/")
//                .permitAll();
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