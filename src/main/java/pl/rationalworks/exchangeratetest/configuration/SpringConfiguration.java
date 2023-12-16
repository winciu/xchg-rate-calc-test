package pl.rationalworks.exchangeratetest.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import pl.rationalworks.exchangeratetest.properties.CustomCurrencyPropertyConverter;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

@Configuration
public class SpringConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        JavaTimeModule module = new JavaTimeModule();
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(module);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        return builder
                .messageConverters(converter)
                .setConnectTimeout(Duration.ofMillis(3000))
                .setReadTimeout(Duration.ofMillis(3000))
                .build();
    }

    /**
     * From https://reflectoring.io/spring-boot-configuration-properties/
     * @return custom {@link pl.rationalworks.exchangeratetest.model.Currency} converter required for
     * reading property files correctly.<br/>
     * Having *only* this method (bean declaration) in here does not work probably due to a bug in Spring.
     * To make it work you need to register the converter manually which is done in the {@link SpringConfiguration#conversionService(Set)} method.
     * <br/><br/>
     * Mor on this here: https://github.com/spring-projects/spring-boot/issues/28592
     */
    @Bean
    @ConfigurationPropertiesBinding
    public CustomCurrencyPropertyConverter customCurrencyPropertyConverter() {
        return new CustomCurrencyPropertyConverter();
    }

    /**
     * This Bean is required to properly handle custom {@link pl.rationalworks.exchangeratetest.model.Currency} values
     * set in the configuration property files.<br/><br/>
     * This method alone does not work. We need to have another bean explicitly defining a converter.
     * @param converters converted to register
     * @return conversion service object instance
     */
    @Bean
    public ConversionService conversionService(@Autowired Set<Converter> converters) {
        ConversionServiceFactoryBean factory = new ConversionServiceFactoryBean();
        factory.setConverters(converters);
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
