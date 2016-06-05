package com.shivang.twitter.config;

import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import static springfox.documentation.schema.AlternateTypeRules.newRule;

@Configuration
public class SwaggerConfig {

    @Autowired
    private TypeResolver typeResolver;

    @Bean
    public Docket TwitterFeedApi(SwaggerApiInfo swaggerApiInfo) {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .tags(new Tag("Twitter TwitterUser Service", "Service for twitter users and their feeds"))
                .ignoredParameterTypes(Authentication.class)
                .alternateTypeRules(
                        newRule(typeResolver.resolve(DeferredResult.class,
                                typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                                typeResolver.resolve(WildcardType.class)))
                .apiInfo(apiInfo(swaggerApiInfo.getTitle(),
                        swaggerApiInfo.getDescription(),
                        swaggerApiInfo.getContactName(),
                        swaggerApiInfo.getContactUrl(),
                        swaggerApiInfo.getContactEmail(),
                        swaggerApiInfo.getVersion()))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.shivang.twitter"))
                .build();
    }

    private ApiInfo apiInfo(String title, String description, String contactName,
                            String contactUrl, String contactEmail, String version) {
        return new ApiInfoBuilder()
                .title(title)
                .description(description)
                .contact(new Contact(contactName, contactUrl, contactEmail))
                .version(version)
                .build();
    }

    @ConfigurationProperties(prefix = "springfox.documentation.swagger")
    @Component
    public static class SwaggerApiInfo {

        private String version;
        private String title;
        private String description;
        private String contactName;
        private String contactUrl;
        private String contactEmail;

        public SwaggerApiInfo() {
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getContactName() {
            return contactName;
        }

        public void setContactName(String contactName) {
            this.contactName = contactName;
        }

        public String getContactUrl() {
            return contactUrl;
        }

        public void setContactUrl(String contactUrl) {
            this.contactUrl = contactUrl;
        }

        public String getContactEmail() {
            return contactEmail;
        }

        public void setContactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
        }
    }
}
