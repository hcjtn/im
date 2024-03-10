package edu.nuc.hcj.im.service.config;

import edu.nuc.hcj.im.common.config.Appconfig;
import edu.nuc.hcj.im.common.enums.ImUrlRouteWayEnum;
import edu.nuc.hcj.im.common.enums.RouteHashMethodEnum;
import edu.nuc.hcj.im.common.route.RouteHandle;
import edu.nuc.hcj.im.common.route.algorithm.consistentHash.AbstractConsistentHash;
import edu.nuc.hcj.im.common.route.algorithm.consistentHash.ConsistentHashHandle;
import edu.nuc.hcj.im.common.route.algorithm.consistentHash.TreeMapConsistentHash;
import edu.nuc.hcj.im.common.route.algorithm.loop.LoopHandle;
import edu.nuc.hcj.im.common.route.algorithm.random.RandomHandle;
import edu.nuc.hcj.im.service.utils.SnowflakeIdWorker;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Created with IntelliJ IDEA.
 *
 * @author : hcj
 * @version : 1.0
 * @Project : IM-system
 * @Package : edu.nuc.hcj.im.service.config
 * @ClassName : BeanConfig.java
 * @createTime : 2023/12/20 17:27
 * @Description : 负责部分类的初始化  将实例化后的类注册到组件当中去
 */
@Configuration
public class BeanConfig {

    @Autowired
    Appconfig appConfig;

    @Bean
    public ZkClient buildZKClient() {
        return new ZkClient(appConfig.getZkAddr(),
                appConfig.getZkConnectTimeOut());
    }

    @Bean
    public RandomHandle randomHandle() {
        return new RandomHandle();
    }

    @Bean
    public LoopHandle loopHandle() {
        return new LoopHandle();
    }

    @Bean
    public ConsistentHashHandle hashHandle() {
        ConsistentHashHandle consistentHashHandle = new ConsistentHashHandle();
        TreeMapConsistentHash treeMapConsistentHash = new TreeMapConsistentHash();
        consistentHashHandle.setHash(treeMapConsistentHash);
        return consistentHashHandle;
    }

    @Bean
    public RouteHandle routeHandle() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // 获取到要执行的路由方法的数字编码
        Integer imRouteWay = appConfig.getImRouteWay();
        // 获取到对应的路由执行策略的 类的路径
        String routWay = ImUrlRouteWayEnum.getHandler(imRouteWay).getClazz();
        //使用反射方法执行 获取对应的路由策略对象类
        RouteHandle routeHandle = (RouteHandle) Class.forName(routWay).newInstance();
        // 如果是一致性hash
        if (ImUrlRouteWayEnum.getHandler(imRouteWay) == ImUrlRouteWayEnum.HASH) {
            // 获取对应的hashset方法
            Method setHash = Class.forName(routWay).getMethod("setHash", AbstractConsistentHash.class);
            Integer consistentHashWay = appConfig.getConsistentHashWay();
            // 获取到对应hashset的类 并实例化
            String clazz = RouteHashMethodEnum.getHandler(consistentHashWay).getClazz();
            AbstractConsistentHash abstractConsistentHash =
                    (AbstractConsistentHash) Class.forName(clazz).newInstance();
            //执行setHash方法
            setHash.invoke(routeHandle, abstractConsistentHash);
        }
        return routeHandle;
    }

    @Bean
    public EasySqlInjector easySqlInjector(){
        return new EasySqlInjector();
    }

    @Bean
    public SnowflakeIdWorker buildsnowflakeseq() throws Exception{
        return new SnowflakeIdWorker(0L);
    }
}
