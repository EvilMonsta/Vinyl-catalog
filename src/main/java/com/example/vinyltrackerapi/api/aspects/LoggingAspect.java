package com.example.vinyltrackerapi.api.aspects;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    @Pointcut("within(com.example.vinyltrackerapi.api.controllers..*)")
    public void controllerMethods() {
    }

    @Before("controllerMethods()")
    public void logRequest(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes)
                Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        log.info("[{}] {} - {}", request.getMethod(),
                request.getRequestURI(), joinPoint.getSignature());
    }

    @AfterThrowing(pointcut = "controllerMethods()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        HttpServletRequest request = ((ServletRequestAttributes)
                Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        log.error("[ERROR] [{}] {} - {}: {}", request.getMethod(),
                request.getRequestURI(), joinPoint.getSignature(), ex.getMessage());
    }
}
