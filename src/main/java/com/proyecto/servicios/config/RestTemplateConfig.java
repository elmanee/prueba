package com.proyecto.servicios.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 1. Usar SimpleClientHttpRequestFactory (nativo de Java HttpURLConnection)
        // Evita cualquier incompatibilidad o conflicto de versiones entre librerías externas de Apache Http Client
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());

        RestTemplate restTemplate = new RestTemplate(requestFactory);

        // 2. Configurar XmlMapper tolerante
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 3. Configurar Converter para XML con soporte amplio de tipos
        MappingJackson2XmlHttpMessageConverter xmlConverter = new MappingJackson2XmlHttpMessageConverter(xmlMapper);
        List<MediaType> supportedXmlTypes = new ArrayList<>();
        supportedXmlTypes.add(MediaType.APPLICATION_XML);
        supportedXmlTypes.add(MediaType.TEXT_XML);
        supportedXmlTypes.add(new MediaType("application", "*+xml"));
        supportedXmlTypes.add(MediaType.TEXT_HTML);
        xmlConverter.setSupportedMediaTypes(supportedXmlTypes);

        // 4. Registrar convertidores
        restTemplate.getMessageConverters().add(0, xmlConverter);
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        return restTemplate;
    }
}
