/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.rpc.filter.validation;

import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.alipay.sofa.rpc.log.Logger;
import com.alipay.sofa.rpc.log.LoggerFactory;
import com.alipay.sofa.rpc.test.ActivelyDestroyTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zhanggeng on 2019/4/16.
 *
 * @author <a href="mailto:zhanggeng.zg@antfin.com">zhanggeng</a>
 */
public class ValidationFilterTest extends ActivelyDestroyTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(ValidationFilterTest.class);

    @Test
    public void testValidationInConsumer() {

        ServerConfig serverConfig2 = new ServerConfig()
            .setPort(22222)
            .setDaemon(false);

        // ProviderConfig
        ProviderConfig<ValidationService> providerConfig = new ProviderConfig<ValidationService>()
            .setInterfaceId(ValidationService.class.getName())
            .setRef(new ValidationServiceImpl())
            .setUniqueId("test1")
            .setApplication(new ApplicationConfig().setAppName("sss"))
            .setServer(serverConfig2);
        providerConfig.export();

        // ConsumerConfig
        ConsumerConfig<ValidationService> consumerConfig = new ConsumerConfig<ValidationService>()
            .setInterfaceId(ValidationService.class.getName())
            .setInvokeType(RpcConstants.INVOKER_TYPE_SYNC)
            .setValidation(true) // enable validation
            .setApplication(new ApplicationConfig().setAppName("ccc"))
            .setTimeout(5000)
            .setUniqueId("test1")
            .setDirectUrl("bolt://127.0.0.1:22222?appName=sss");
        ValidationService service = consumerConfig.refer();

        try {
            ValidationTestObj obj = new ValidationTestObj();
            obj.setName("xxxxx");
            obj.setAge(1000);
            obj.setEmail("xxx.com");

            ValidationTestObj result = service.echoObj(obj);
            System.out.println(result.getName());
        } catch (Exception e) {
            LOGGER.info("", e);
            Assert.assertTrue(e.getMessage().contains("ConstraintViolationException"));
        }

        try {
            ValidationTestObj obj = new ValidationTestObj();
            obj.setName("zhang");
            obj.setAge(25);
            obj.setEmail("xxx@aa.com");

            ValidationTestObj result = service.echoObj(obj);
            System.out.println(result.getName());
        } catch (Exception e) {
            LOGGER.info("", e);
            Assert.fail();
        }

    }

    @Test
    public void testValidationInProvider() {

        ServerConfig serverConfig2 = new ServerConfig()
            .setPort(22223)
            .setDaemon(false);

        // ProviderConfig
        ProviderConfig<ValidationService> providerConfig = new ProviderConfig<ValidationService>()
            .setInterfaceId(ValidationService.class.getName())
            .setRef(new ValidationServiceImpl())
            .setValidation(true) // enable validation
            .setUniqueId("test2")
            .setApplication(new ApplicationConfig().setAppName("sss"))
            .setServer(serverConfig2);
        providerConfig.export();

        // ConsumerConfig
        ConsumerConfig<ValidationService> consumerConfig = new ConsumerConfig<ValidationService>()
            .setInterfaceId(ValidationService.class.getName())
            .setInvokeType(RpcConstants.INVOKER_TYPE_SYNC)
            .setApplication(new ApplicationConfig().setAppName("ccc"))
            .setTimeout(5000)
            .setUniqueId("test2")
            .setDirectUrl("bolt://127.0.0.1:22223?appName=sss");
        ValidationService service = consumerConfig.refer();

        try {
            ValidationTestObj obj = new ValidationTestObj();
            obj.setName("xxxxx");
            obj.setAge(1000);
            obj.setEmail("xxx.com");

            ValidationTestObj result = service.echoObj(obj);
            System.out.println(result.getName());
        } catch (Exception e) {
            LOGGER.info("", e);
            Assert.assertTrue(e.getMessage().contains("ConstraintViolationException"));
        }

        try {
            ValidationTestObj obj = new ValidationTestObj();
            obj.setName("zhang");
            obj.setAge(25);
            obj.setEmail("xxx@aa.com");

            ValidationTestObj result = service.echoObj(obj);
            System.out.println(result.getName());
        } catch (Exception e) {
            LOGGER.info("", e);
            Assert.fail();
        }

    }
}
