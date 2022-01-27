package com.bolsadeideas.springboot.webflux.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.bolsadeideas.springboot.webflux.app.handler.ProductoHandler;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductoService;

@Configuration
public class RouterFunctionConfig {
	
	@Autowired
	private ProductoService service;
	
//	@Bean
//	public RouterFunction<ServerResponse> routes(){
//		
//		/**
//		 * Los parámetros que acepta el método route son:
//		 * <ul>
//		 * <li>RequestPredicate: Mapea la ruta url</li>
//		 * <li>HandlerFunction: Implementación de la respuesta del request</li>
//		 * </ul>
//		 * 
//		 * Clase de ejemplo incluyendo el handler.
//		 */
//		return RouterFunctions.route(RequestPredicates.GET("/api/v2/productos").or(RequestPredicates.GET("/api/v3/productos")), request -> {
//			return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
//					.body(service.findAll(), Producto.class);
//		});
//	}
	
	/**
	 * Clase de ejemplo desacoplando el handler.
	 * 
	 * @param handler
	 * @return
	 */
	@Bean
	public RouterFunction<ServerResponse> routesWithHandler(ProductoHandler handler){
		return RouterFunctions
				.route(RequestPredicates.GET("/api/v2/productoshandler"), request -> handler.listar(request))
				.andRoute(RequestPredicates.GET("/api/v2/productos/{id}"), request -> handler.ver(request))
				.andRoute(RequestPredicates.POST("/api/v2/productos"), request -> handler.crear(request))
				.andRoute(RequestPredicates.PUT("/api/v2/productos/{id}"), handler::modificar)
				.andRoute(RequestPredicates.DELETE("/api/v2/productos/{id}"), handler::eliminar)
				.andRoute(RequestPredicates.POST("/api/v2/productos/upload/{id}"), request -> handler.upload(request))
				.andRoute(RequestPredicates.POST("/api/v2/productos/crearconfoto"), request -> handler.crearConFoto(request));
		
	}

}
