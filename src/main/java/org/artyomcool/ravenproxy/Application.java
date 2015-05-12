package org.artyomcool.ravenproxy;

import org.artyomcool.ravenproxy.impl.ConfigRavenCache;
import org.artyomcool.ravenproxy.impl.ProguardStacktraceDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
@ComponentScan
@EnableWebMvc
public class Application extends WebMvcConfigurerAdapter {

    @Bean
    StacktraceDecoder stacktraceDecoder() {
        return new ProguardStacktraceDecoder();
    }

    @Bean
    RavenCache ravenCache() {
        return new ConfigRavenCache();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new GsonHttpMessageConverter());
    }

}
