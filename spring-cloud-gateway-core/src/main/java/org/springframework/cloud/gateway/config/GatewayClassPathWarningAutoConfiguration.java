/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gateway.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(GatewayAutoConfiguration.class)
public class GatewayClassPathWarningAutoConfiguration {

	private static final Log log = LogFactory
			.getLog(GatewayClassPathWarningAutoConfiguration.class);

	private static final String BORDER = "\n\n**********************************************************\n\n";

	/**
	 * 用于检查项目是否错误导入了mvc相关的包
	 * @ConditionalOnBean         //	当给定的在bean存在时,则实例化当前Bean
	 * @ConditionalOnMissingBean  //	当给定的在bean不存在时,则实例化当前Bean
	 * @ConditionalOnClass        //	当给定的类名在类路径上存在，则实例化当前Bean
	 * @ConditionalOnMissingClass //	当给定的类名在类路径上不存在，则实例化当前Bean
	 */
	@Configuration
	@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
	protected static class SpringMvcFoundOnClasspathConfiguration {

		public SpringMvcFoundOnClasspathConfiguration() {
			log.warn(BORDER
					+ "Spring MVC found on classpath, which is incompatible with Spring Cloud Gateway at this time. "
					+ "Please remove spring-boot-starter-web dependency." + BORDER);
		}

	}
	/**
	 * 检查项目是否正确导入 spring-boot-starter-webflux 依赖，
	 * 如果不存在webflux的dispatcher就实例化该类，用来打印警告
	 */
	@Configuration
	@ConditionalOnMissingClass("org.springframework.web.reactive.DispatcherHandler")
	protected static class WebfluxMissingFromClasspathConfiguration {

		public WebfluxMissingFromClasspathConfiguration() {
			log.warn(BORDER + "Spring Webflux is missing from the classpath, "
					+ "which is required for Spring Cloud Gateway at this time. "
					+ "Please add spring-boot-starter-webflux dependency." + BORDER);
		}

	}

}
