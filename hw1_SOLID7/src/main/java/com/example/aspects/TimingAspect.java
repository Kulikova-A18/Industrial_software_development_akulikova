package com.example.aspects;

import com.example.annotations.Timed;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class TimingAspect {

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
            System.err.println("Ошибка при выполнении метода " + className + "." + methodName + ": " + t.getMessage());
            throw t;
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.printf("[TIMING] %s.%s выполнен за %d %s%n",
                className, methodName, duration, timed.unit());

        return result;
    }
}