package com.silently9527.smartmvc;

import com.silently9527.smartmvc.config.AppConfig;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)  // Junit提供的扩展接口，这里指定使用SpringJUnit4ClassRunner作为Junit测试环境
@ContextConfiguration(classes = AppConfig.class)  // 加载配置文件
public class BaseJunit4Test {
}
