package com.example.aspects;

import com.example.annotations.Timed;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@Aspect
public class TimingAspect {
    private static final Logger logger = LoggerFactory.getLogger(TimingAspect.class);

    @Around("@annotation(com.example.annotations.Timed)")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Timed timed = method.getAnnotation(Timed.class);

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable t) {
            logger.error("Error executing method {}.{}: {}", className, methodName, t.getMessage());
            throw t;
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.info("[TIMING] {}.{} executed in {} {}", className, methodName, duration, timed.unit());

        return result;
    }
}