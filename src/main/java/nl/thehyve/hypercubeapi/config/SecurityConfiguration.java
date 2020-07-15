package nl.thehyve.hypercubeapi.config;

import org.keycloak.adapters.*;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.session.*;

@Configuration
@ComponentScan(
        basePackageClasses = KeycloakSecurityComponents.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
                pattern = "org.keycloak.adapters.springsecurity.management.HttpSessionManager"))
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class SecurityConfiguration extends KeycloakWebSecurityConfigurerAdapter {

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(keycloakAuthenticationProvider());
    }

    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    @Primary
    @Configuration
    static
    class CustomKeycloakSpringBootConfigResolver implements KeycloakConfigResolver {

        private final AdapterConfig adapterConfig;
        private KeycloakDeployment keycloakDeployment;

        @Autowired
        CustomKeycloakSpringBootConfigResolver(AdapterConfig adapterConfig) {
            this.adapterConfig = adapterConfig;
        }

        @Override
        public KeycloakDeployment resolve(HttpFacade.Request request) {
            if (this.keycloakDeployment == null) {
                this.keycloakDeployment = KeycloakDeploymentBuilder.build(this.adapterConfig);
            }
            return this.keycloakDeployment;
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/error").permitAll()
                .antMatchers(
                    "/swagger-ui.html/**",
                    "/configuration/**",
                    "/swagger-resources/**",
                    "/v2/api-docs",
                    "/webjars/springfox-swagger-ui/**",
                    "/actuator/health"
                    ).permitAll()
                .anyRequest().authenticated()
                .and()
                .csrf().disable();
    }

}
