package localhost.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import static springfox.documentation.builders.RequestHandlerSelectors.any;

@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

    @Bean("api")
    public Docket api2() {

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder().title("Peopleflow").build())
                .groupName("Peopleflow api")
                .select()
                .apis(any())
                .build();
    }
}
