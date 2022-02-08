package ru.alta.thirdproj.aspect;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import ru.alta.thirdproj.entites.UserLogin;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class AppLoggingAspect {
    // "execution(modifier-pattern? return-type-pattern declaring-type-pattern? method-name-pattern(param-pattern)
    // throws-pattern?)"
    // execution([модификатор_метода(public, *)?] [тип_возврата] [класс?] [имя_метода]([аргументы]) [исключения?]

    private Logger logger = LoggerFactory.getLogger(this.getClass());
//
//    @AfterReturning(
//            pointcut = "execution(public * ru.alta.thirdproj.services.UserLoginServiceImpl.loadUserByUsername(..))",
//            returning = "result")
//    public void afterGetUser(UserLogin result) {
//        System.out.println("Залогинился user : "  + result.getUserName());
//    }
//
//    @Before("execution(public void ru.alta.thirdproj.services.UserLoginServiceImpl.loadUserByUsername(..))") // pointcut expression
//    public void beforeAddUserInUserDAOClass() {
//        System.out.println("AOP: Поймали добавление пользователя");
//    }
//
//    @Pointcut("execution(public * ru.alta.thirdproj.entites.UserLogin.get*(..))") // pointcut expression
//    public void userDAOGetTrackerPointcut() {
//    }

    @Before("execution(public * ru.alta.thirdproj.services.UserLoginServiceImpl.*(..))")
    public void allMethodsCallsAnalytics() {
     //   System.out.println("В классе UserDAO вызывают метод (Аналитика)");
    }
//
    @After("execution(public * ru.alta.thirdproj.services.UserLoginServiceImpl.loadUserByUsername(String))")
    public void allMethodsCalls() {
      //  System.out.println("В классе UserDAO вызывают метод (kghkhkh)" );
    }


    @Pointcut("execution(public * ru.alta.thirdproj.services.UserLoginServiceImpl.*(..))")
    public void callAtMyServicePublic() { }

//    @After("callAtMyServicePublic()")
//    public void afterCallAt(JoinPoint jp) {
//        String args = Arrays.stream(jp.getArgs())
//                .map(a -> a.toString())
//                .collect(Collectors.joining(","));
//        System.out.println("after " + jp.toString() + ", args=[" + args + "]");
//      //  System.out.println("after " + jp.toString());
//        logger.info("after " + jp.toString());
//    }

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
        System.out.println("AuthenticationSuccess "  + ", args=[" + args + "]");
    }



    @Pointcut("execution(* ru.alta.thirdproj.services.UserLoginServiceImpl.loadUserByUsername(..)) && args(userName))")
    public void callAtMyServiceMethod1(String userName) {
    }
//    @AfterThrowing("callAtMyServiceMethod1(userName)")
//    public void throwCallAtMethod1(String userName) {
//        System.out.println("Не залогинился:  " + userName );
//    }

    @After("callAtMyServiceMethod1(userName)")
    public void beforeCallAtMethod1(String userName) {
        System.out.println("попытка залогиниться:  " + userName );
    }









//
//    @Before("execution(public void com.geekbrains.aop.UserDAO.*())") // pointcut expression
//    public void beforeAnyMethodWithoutArgsInUserDAOClass() {
//        System.out.println("AOP: любой метод без аргументов из UserDAO");
//    }

//    @Before("execution(public void com.geekbrains.aop.UserDAO.*(..))") // pointcut expression
//    public void beforeAnyMethodInUserDAOClass() {
//        System.out.println("AOP: любой метод c аргументами из UserDAO");
//    }

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
//
//    @AfterReturning(
//            pointcut = "execution(public * com.geekbrains.aop.UserDAO.getAllUsers(..))",
//            returning = "result")
//    public void afterGetBobInfo(JoinPoint joinPoint, List<String> result) {
//        result.set(0, "Donald Duck");
//    }
//
//    @AfterThrowing(
//            pointcut = "execution(public * com.geekbrains.aop.UserDAO.*)",
//            throwing = "exc")
//    public void afterThrowing(JoinPoint joinPoint, Throwable exc) {
//        System.out.println(exc); // logging
//    }

//    @After("execution(public * com.geekbrains.aop.UserDAO.*)")
//    public void afterMethod() {
//        System.out.println("After");
//    }

    // todo куда делся list?
//    @Around("execution(public * com.geekbrains.aop.UserDAO.*(..))")
//    public void methodProfiling(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
//        System.out.println("start profiling");
//        long begin = System.currentTimeMillis();
//        proceedingJoinPoint.proceed();
//        long end = System.currentTimeMillis();
//        long duration = end - begin;
//        System.out.println((MethodSignature) proceedingJoinPoint.getSignature() + " duration: " + duration);
//        System.out.println("end profiling");
//    }
}
