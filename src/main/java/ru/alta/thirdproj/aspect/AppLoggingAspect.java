package ru.alta.thirdproj.aspect;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class AppLoggingAspect {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Pointcut("execution(public * ru.alta.thirdproj.services.UserLoginServiceImpl.*(..))")
    public void callAtMyServicePublic() { }


    @Pointcut("execution(public * ru.alta.thirdproj.config.CustomAuthenticationFailureHandler.*(..)))")
    public void callAuthenticationException() {
    }

    @After("callAuthenticationException()")
    public void afterCallMethod(JoinPoint jp) {
        String args = Arrays.stream(jp.getArgs())
                .map(a -> a.toString())
                .collect(Collectors.joining(","));
        System.out.println("AuthenticationException "  + ", args=[" + args + "]");
    }

    @Pointcut("execution(public * ru.alta.thirdproj.config.CustomAuthenticationSuccessHandler.*(..)))")
    public void callAuthenticationSuccess() {
    }

    @After("callAuthenticationSuccess()")
    public void afterAuthenticationSuccess(JoinPoint jp) {
        String args = Arrays.stream(jp.getArgs())
                .map(a -> a.toString())
                .collect(Collectors.joining(","));
        logger.info("AuthenticationSuccess, args=[{}]", args);
    }



    @Pointcut("execution(* ru.alta.thirdproj.services.UserLoginServiceImpl.loadUserByUsername(..)) && args(userName))")
    public void callAtMyServiceMethod1(String userName) {
    }

    @After("callAtMyServiceMethod1(userName)")
    public void beforeCallAtMethod1(String userName) {
        logger.info("попытка залогиниться: {}", userName);
    }

    @AfterReturning(
            pointcut = "execution(* ru.alta.thirdproj.services.UserLoginServiceImpl.loadUserByUsername(..))",
            returning = "result")
    public void afterLoginSucceeded(Object result) {
        if (result instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) result;
            logger.info("Залогинился user: {}", userDetails.getUsername());
            return;
        }
        logger.info("Залогинился user (неопределенный тип результата): {}", result);
    }


    @Before("execution(public void ru.alta.thirdproj.services.UserLoginServiceImpl.*(..))") // pointcut expression
    public void beforeAnyMethodInUserDAOClassWithDetails(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        System.out.println("В UserLoginServiceImpl был вызван метод: " + methodSignature);
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            System.out.println("Аргументы:");
            for (Object o : args) {
                System.out.println(o);
            }
        }
    }
}
