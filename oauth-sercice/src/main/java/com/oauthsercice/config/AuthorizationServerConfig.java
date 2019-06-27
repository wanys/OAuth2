package com.oauthsercice.config;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerSecurityConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import javax.sql.DataSource;


//授权认证服务中心
@Configuration
@EnableWebSecurity  //开启授权认证服务中心
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    private int accessTokenValiditySeconds = 7200;
    private int refreshtokenvalidityseconds =7200;

    @Autowired
    // @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

      @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;
    @Bean
    public TokenStore tokenStore() {
        // return new InMemoryTokenStore(); //使用内存中的 token store
        return new JdbcTokenStore(dataSource); /// 使用Jdbctoken store
    }

@Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // withClient appid

        //申请获取APPID和APPKey
        clients.inMemory().withClient("client_1").secret(passwordEncoder().encode("123456"))
                .redirectUris("http://baidu.com")
                .authorizedGrantTypes("authorization_code")
                .scopes("all")
                .accessTokenValiditySeconds(accessTokenValiditySeconds)
               .refreshTokenValiditySeconds(refreshtokenvalidityseconds);
    }


  /*  // 设置token类型
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager()).allowedTokenEndpointRequestMethods(HttpMethod.GET,
                HttpMethod.POST);
    }*/

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(tokenStore()).authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService());// 必须设置
        // UserDetailsService
        // 否则刷新token 时会报错
    }



    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        // 允许表单认证
        oauthServer.allowFormAuthenticationForClients();
        // 允许check_token访问
        oauthServer.checkTokenAccess("permitAll()");
    }



    @Bean
    AuthenticationManager authenticationManager() {
        AuthenticationManager authenticationManager = new AuthenticationManager() {

            @Override
            public org.springframework.security.core.Authentication authenticate(org.springframework.security.core.Authentication authentication) throws org.springframework.security.core.AuthenticationException {
                return daoAuhthenticationProvider().authenticate(authentication);
            }

            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                return null;//daoAuhthenticationProvider().authenticate(authentication);
            }
        };
        return authenticationManager;
    }

    @Bean
    public AuthenticationProvider daoAuhthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService());
        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
        userDetailsService.createUser(User.withUsername("user_1").password(passwordEncoder().encode("123456"))
                .authorities("ROLE_USER").build());
        userDetailsService.createUser(User.withUsername("user_2").password(passwordEncoder().encode("1234567"))
                .authorities("ROLE_USER").build());
        return userDetailsService;
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        //
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder;
    }


    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println(passwordEncoder.encode("123456"));
    }

    //$2a$10$K8jaPH4.G.RyQ0Cjh1L01eqsgS32IulVGG8SjTWkBp5HRmic8TrUW
}
