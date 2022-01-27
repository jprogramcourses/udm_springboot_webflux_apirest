package com.bolsadeideas.springboot.webflux.app.handler;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Hace las funciones de controlador.
 * 
 * @author juand
 *
 */
@Component
public class ProductoHandler {

	private ProductoService service;
	
	private Validator validator;
	
	@Value("${config.uploads.path}")
	private String path;

	public ProductoHandler(ProductoService service, Validator validator) {
		this.service = service;
		this.validator = validator;
	}

	public Mono<ServerResponse> listar(ServerRequest request) {
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(service.findAll(), Producto.class);
	}

	public Mono<ServerResponse> ver(ServerRequest request) {
		String id = request.pathVariable("id");
		return service.findById(id).flatMap(
				p -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(p)))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> crear(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);

		return producto.flatMap(p -> {
			
			// Store information about the validation of the object
			Errors errors = new BeanPropertyBindingResult(p, Producto.class.getName());
					
			validator.validate(p, errors);
			
			if(errors.hasErrors()) {
				return Flux.fromIterable(errors.getFieldErrors())
						.map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
						.collectList()
						.flatMap(list -> ServerResponse.badRequest().body(BodyInserters.fromObject(list)));
			}else {
				if (p.getCreateAt() == null) {
					p.setCreateAt(new Date());
				}
				return service.save(p).flatMap(pdb -> ServerResponse
						.created(URI.create("/api/v2/productos/".concat(pdb.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(BodyInserters.fromObject(pdb)));
			}
			
			
		});
	}
	
	public Mono<ServerResponse> modificar(ServerRequest request){
		String id = request.pathVariable("id");
		Mono<Producto> monoProductoNuevo = request.bodyToMono(Producto.class);
		
		Mono<Producto> monoProductoExistente = service.findById(id);
		
		return monoProductoExistente.zipWith(monoProductoNuevo, (prExist, prNuevo) -> {
			prExist.setNombre(prNuevo.getNombre());
			prExist.setPrecio(prNuevo.getPrecio());
			prExist.setCategoria(prNuevo.getCategoria());
			return prExist;
		}).flatMap(p -> ServerResponse
				.created(URI.create("/api/v2/productos/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.save(p), Producto.class))
			.switchIfEmpty(ServerResponse.notFound().build());
	}
	
	public Mono<ServerResponse> eliminar(ServerRequest request){
		String id = request.pathVariable("id");
		Mono<Producto> productoDb = service.findById(id);
		
		return productoDb.flatMap(p -> service.delete(p).then(ServerResponse.noContent().build()))
				.switchIfEmpty(ServerResponse.notFound().build());
	}
	
	public Mono<ServerResponse> upload(ServerRequest request){
		String id = request.pathVariable("id");
		return request.multipartData()
				.map(multipart -> multipart
						.toSingleValueMap().get("file")).cast(FilePart.class).flatMap(file -> service.findById(id)
								.flatMap(p -> {
									p.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
									.replace(" ", "-")
									.replace(":", "")
									.replace("\\", ""));
									return file.transferTo(new File(path + p.getFoto())).then(service.save(p));
		})).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(p)))
		.switchIfEmpty(ServerResponse.notFound().build());
	}
	
	public Mono<ServerResponse> crearConFoto(ServerRequest request){
		
		Mono<Producto> producto = request.multipartData().map(multipart ->{
			FormFieldPart nombre = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
			FormFieldPart precio = (FormFieldPart) multipart.toSingleValueMap().get("precio");
			FormFieldPart categoriaId = (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
			FormFieldPart categoriaNombre = (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");
			
			Categoria categoria = new Categoria(categoriaNombre.value());
			categoria.setId(categoriaId.value());
			return new Producto(nombre.value(), Double.parseDouble(precio.value()), categoria);
		});
		
		return request.multipartData()
				.map(multipart -> multipart
						.toSingleValueMap().get("file")).cast(FilePart.class).flatMap(file -> producto
								.flatMap(p -> {
									p.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
									.replace(" ", "-")
									.replace(":", "")
									.replace("\\", ""));
									p.setCreateAt(new Date());
									return file.transferTo(new File(path + p.getFoto())).then(service.save(p));
		})).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(p)));
	}

}
