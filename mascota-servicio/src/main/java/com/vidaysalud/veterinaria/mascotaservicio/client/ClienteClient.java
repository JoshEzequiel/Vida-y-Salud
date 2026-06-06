package com.vidaysalud.veterinaria.mascotaservicio.client;

import com.vidaysalud.veterinaria.mascotaservicio.exception.ReglaNegocioException;
import com.vidaysalud.veterinaria.mascotaservicio.exception.ServicioRemotoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteClient {
    private final WebClient.Builder webClientBuilder;
    @Value("${servicios.cliente.url}") private String baseUrl;

    public void validar(Integer id) {
        try {
            webClientBuilder.build().get().uri(baseUrl + "/" + id)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response ->
                            response.createException().map(ex -> new ReglaNegocioException("Cliente no existe con id: " + id)))
                    .toBodilessEntity().block(Duration.ofSeconds(4));
            log.info("Validación remota exitosa: Cliente id={}", id);
        } catch (ReglaNegocioException ex) {
            throw ex;
        } catch (WebClientResponseException.NotFound ex) {
            throw new ReglaNegocioException("Cliente no existe con id: " + id);
        } catch (WebClientRequestException ex) {
            throw new ServicioRemotoException("No fue posible conectar con cliente-servicio");
        } catch (Exception ex) {
            throw new ServicioRemotoException("Error consultando cliente-servicio");
        }
    }
}
