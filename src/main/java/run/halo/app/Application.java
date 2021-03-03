package run.halo.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Halo main class.
 *
 * @author ryanwang
 * @date 2017-11-14
 */
//是一个复合注解，继承@Configuration，标注当前类是配置类，并将@Bean
//纳入spring容器中。
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // Customize the spring config location
//        设置系统属性
//        定制spring配置文件的路径
//        user.name的值是C:\Users\wangze
        System.setProperty("spring.config.additional-location",
                "optional:file:${user.home}/.halo/,optional:file:${user.home}/halo-dev/");
//        System.out.println(System.getProperty("user.home"));
        // Run application
//        启动应用
        SpringApplication.run(Application.class, args);
    }

}
