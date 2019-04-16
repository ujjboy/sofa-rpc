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
package com.alipay.sofa.rpc.filter;

import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.common.utils.ExceptionUtils;
import com.alipay.sofa.rpc.config.AbstractInterfaceConfig;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.filter.validation.validation.Validator;
import com.alipay.sofa.rpc.filter.validation.validation.ValidatorFactory;
import com.alipay.sofa.rpc.message.MessageBuilder;

/**
 * @author <a href="mailto:zhanggeng.zg@antfin.com">zhanggeng</a>
 */
@Extension(value = "validation", order = -17000)
@AutoActive(consumerSide = true, providerSide = true)
public class ValidationFilter extends Filter {

    @Override
    public boolean needToLoad(FilterInvoker invoker) {
        AbstractInterfaceConfig config = invoker.getConfig();
        return config != null && config.hasValidation();
    }

    @Override
    public SofaResponse invoke(FilterInvoker invoker, SofaRequest request) throws SofaRpcException {
        String methodName = request.getMethodName();
        if (invoker.getBooleanMethodParam(methodName, RpcConstants.CONFIG_KEY_VALIDATION, false)) {
            String customImpl = invoker.getStringMethodParam(methodName, RpcConstants.HIDE_KEY_PREFIX + "customImpl",
                null);
            String className = request.getInterfaceName();
            Validator validator = ValidatorFactory.getValidator(className, customImpl);
            try {
                validator.validate(methodName, request.getMethodArgSigs(), request.getMethodArgs());
            } catch (Exception e) { // validate failure
                return MessageBuilder.buildSofaErrorResponse(ExceptionUtils.toString(e));
            }
        }
        return invoker.invoke(request);
    }
}