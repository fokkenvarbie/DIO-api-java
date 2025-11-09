package one.digitalinnovation.beerstock.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI beerStockOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Beer Stock API")
                        .description("REST API for beer stock management")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Rodrigo Peleias")
                                .url("https://github.com/rpeleias")
                                .email("rodrigo.peleis@gmail.com")));
    }
}