package wisekb.agent.rm;

import org.apache.hadoop.conf.Configuration;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * aspectj를 사용하여 ResourceManager class 의 serviceInit method를 handling 한다.
 *
 * @see org.apache.hadoop.yarn.server.resourcemanager.ResourceManager#serviceInit(Configuration)
 */

@Aspect
public class ResourceManagerAspectJ {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceManagerAspectJ.class);

    /**
     * {@link org.apache.hadoop.yarn.server.resourcemanager.ResourceManager#serviceInit(Configuration)}에 대한 pointcut
     */
    @Pointcut("execution(* org.apache.hadoop.yarn.server.resourcemanager.ResourceManager.serviceInit(..))")
    public void aspectTargetMethod() {
    }

    /**
     * {@link ResourceManagerAspectJ#aspectTargetMethod()} 의 target이 완료 되었을때 method 실행
     *
     * @param joinPoint jointpoint
     */
    @After("wisekb.agent.rm.ResourceManagerAspectJ.aspectTargetMethod()")
    public void afterInit(JoinPoint joinPoint) {
        ResourceManagerAgent.start(joinPoint.getThis());
    }
}