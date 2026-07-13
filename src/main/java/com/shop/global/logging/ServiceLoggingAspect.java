package com.shop.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Service 메서드 호출 시 메서드명 + 소요 시간 자동 로깅.
 *
 * <p>대상: com.team23..service.. 패키지의 모든 메서드.
 * <p>인자 / 반환값은 로깅하지 않음 (민감 정보 보호).
 */
@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {
    @Around("execution(* com.shop..service..*(..))") // com.team23..service.. — com.team23 하위 어디든 service 패키지 안
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed(); // 원본 메서드 실행 - 호출 전후로 로깅 인터셉트
            long duration = System.currentTimeMillis() - startTime;
            log.info("[AOP][Service] {}.{}() 완료 - {}ms", className, methodName, duration);
            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[AOP][Service] {}.{}() 예외 - {}ms - {}: {}", className, methodName, duration, e.getClass().getSimpleName(), e.getMessage());
            throw e;   // 예외는 그대로 던짐 (동작 변경 X)
        }
    }
}
