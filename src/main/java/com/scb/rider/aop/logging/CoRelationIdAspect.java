package com.scb.rider.aop.logging;

import com.scb.rider.constants.Constants;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.UUID;

//@Aspect
//@Component
@Log4j2
public class CoRelationIdAspect {

    @Before(value = "execution(public * com.scb.rider.controller.*.*(..))")
    public void before(JoinPoint joinPoint) {
        final String correlationId = generateUniqueCorrelationId();
        String[] paramNames = ((MethodSignature)joinPoint.getSignature()).getParameterNames();
        Object[] paramValues = joinPoint.getArgs();
        log.info(Constants.LOG_FORMAT_CONTROLLER_START, joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), correlationId, paramNames, paramValues);
        MDC.put(Constants.CORRELATION_ID_LOG_VAR_NAME, correlationId);
    }

    @After(value = "execution(public * com.scb.rider.controller.*.*(..))")
    public void afterReturning(JoinPoint joinPoint) {
        log.info(Constants.LOG_FORMAT_CONTROLLER_END, joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), MDC.get(Constants.CORRELATION_ID_LOG_VAR_NAME));
        MDC.remove(Constants.CORRELATION_ID_LOG_VAR_NAME);
    }

    @Around("execution(public * com.scb.rider.service.*.*.*(..)) || execution(public * com.scb.rider.service.*.*(..))")
    public Object aroundService(ProceedingJoinPoint  joinPoint) throws Throwable {
        String[] paramNames = ((MethodSignature)joinPoint.getSignature()).getParameterNames();
        Object[] paramValues = joinPoint.getArgs();
        log.info(Constants.LOG_FORMAT_SERVICE_START, joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), MDC.get(Constants.CORRELATION_ID_LOG_VAR_NAME), paramNames, paramValues);
        Object output = joinPoint.proceed();
        log.info(Constants.LOG_FORMAT_SERVICE_END, joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), MDC.get(Constants.CORRELATION_ID_LOG_VAR_NAME));
        return output;
    }

    @Around("execution(public * com.scb.rider.repository.*.*(..))")
    public Object aroundRepository(ProceedingJoinPoint  joinPoint) throws Throwable {
        String[] paramNames = ((MethodSignature)joinPoint.getSignature()).getParameterNames();
        Object[] paramValues = joinPoint.getArgs();
        log.info(Constants.LOG_FORMAT_REPO_START, joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), MDC.get(Constants.CORRELATION_ID_LOG_VAR_NAME), paramNames, paramValues);
        Object output = joinPoint.proceed();
        log.info(Constants.LOG_FORMAT_REPO_END, joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), MDC.get(Constants.CORRELATION_ID_LOG_VAR_NAME));
        return output;
    }

    @Around("execution( * com.scb.rider.client.*.*(..))")
    public Object aroundFeignClient(ProceedingJoinPoint  joinPoint) throws Throwable {
        String[] paramNames = ((MethodSignature)joinPoint.getSignature()).getParameterNames();
        Object[] paramValues = joinPoint.getArgs();
        log.info(Constants.LOG_FORMAT_FEIGN_CLIENT_START, joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), MDC.get(Constants.CORRELATION_ID_LOG_VAR_NAME), paramNames, paramValues);
        Object output = joinPoint.proceed();
        log.info(Constants.LOG_FORMAT_FEIGN_CLIENT_END, joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), MDC.get(Constants.CORRELATION_ID_LOG_VAR_NAME));
        return output;
    }

    @AfterThrowing("execution( * com.scb.rider.client.*.*(..)) || execution(public * com.scb.rider.repository.*.*(..)) || execution(public * com.scb.rider.service.*.*.*(..)) || execution(public * com.scb.rider.service.*.*(..))")
    public void logExceptions(JoinPoint joinPoint){
        log.error(Constants.LOG_FORMAT_EXCEPTION, joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(), MDC.get(Constants.CORRELATION_ID_LOG_VAR_NAME));
    }

    private String generateUniqueCorrelationId() {
        return UUID.randomUUID().toString();
    }
}