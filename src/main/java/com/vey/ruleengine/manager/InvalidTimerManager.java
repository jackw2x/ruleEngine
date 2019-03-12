package com.vey.ruleengine.manager;

import com.vey.ruleengine.contansts.TimerConstants;
import com.vey.ruleengine.model.ExecuteRule;
import com.vey.ruleengine.utils.DateUtils;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Auther vey
 * @Date 2018/11/5
 */
@Component
public class InvalidTimerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EffectTimerManager.class);

    @Resource
    private AssignmentManager assignmentManager;
    private ConcurrentHashMap<String, Timeout> timeoutMap = new ConcurrentHashMap<>();
    private HashedWheelTimer timer;

    @PostConstruct
    public void init() {
        timer = new HashedWheelTimer(TimerConstants.TIMER_TICK, TimeUnit.SECONDS);
    }

    /**
     * 把还未失效的任务加入定时器，到期自动失效
     *
     * @param executeRule
     */
    public void add(ExecuteRule executeRule) {
        LOGGER.info("[InvalidTimerManager.add] add InvalidTimerManager start! executeRule: {}, timeoutSize: {}", executeRule, timeoutMap.size());

        remove(executeRule);

        long delay = DateUtils.calculateSeconds(executeRule.getEffectStartTime(), executeRule.getEffectEndTime());
        Timeout timeout = timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                LOGGER.info("[InvalidTimerManager.add] add InvalidTimerManager processing! executeRule: {}, timeoutSize: {}", executeRule, timeoutMap.size());
                assignmentManager.remove(executeRule.getCode());
                timeoutMap.remove(executeRule.getCode());
                LOGGER.info("[InvalidTimerManager.add] add InvalidTimerManager processing! executeRule: {}, timeoutSize: {}", executeRule, timeoutMap.size());
            }
        }, delay, TimeUnit.SECONDS);
        timeoutMap.put(executeRule.getCode(), timeout);
        LOGGER.info("[InvalidTimerManager.add] add InvalidTimerManager success! executeRule: {}, timeoutSize: {}, delay: {}", executeRule, timeoutMap.size(), delay);
    }

    /**
     * 从定时器中删除待失效任务
     *
     * @param executeRule
     */
    public void remove(ExecuteRule executeRule) {
        LOGGER.info("[InvalidTimerManager.remove] remove InvalidTimerManager start! executeRule: {}, timeoutSize: {}", executeRule, timeoutMap.size());
        Timeout timeout = timeoutMap.get(executeRule.getCode());
        if (timeout != null) {
            timeout.cancel();
            timeoutMap.remove(executeRule.getCode());
            LOGGER.info("[InvalidTimerManager.remove] remove InvalidTimerManager start! executeRule: {}, timeoutSize: {}", executeRule, timeoutMap.size());
        }
    }
}
