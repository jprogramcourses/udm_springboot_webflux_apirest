package com.bolsadeideas.springboot.webflux.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterFunctionConfig {
	
	@Bean
	public RouterFunction<ServerResponse> routes(){
		
		return RouterFunctions.route(null, null)
	}

}
