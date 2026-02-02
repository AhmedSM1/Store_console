package sa.com.store.authorization.aspect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LogManager.getLogger(LoggingAspect.class);

    @Pointcut("execution(public * sa.com.store.authorization.service.*.*(..))")
    private void serviceLogging() {
    }
    @Pointcut("execution(public * sa.com.store.authorization.controller.*.*(..))")
    private void controllerLogging() {
    }


    @Around(value = "serviceLogging()")
    public Object logAroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        return getObject(joinPoint);
    }

    @Around(value = "controllerLogging()")
    public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        return getObject(joinPoint);
    }

    private Object getObject(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        log.debug(">> {}() - {}", methodName, Arrays.toString(args));
        Object result = joinPoint.proceed();
        log.debug("<< {}() - {}", methodName, result);
        return result;
    }
}
