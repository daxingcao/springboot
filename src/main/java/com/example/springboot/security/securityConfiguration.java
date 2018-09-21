package com.example.springboot.security;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@Profile("dev")
@EnableWebSecurity
public class securityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private Environment env;
	@Autowired
	private AuthenticationFailureHandler authenticationFailureHandler;
	@Autowired
	private AuthenticationUserDetailsService<CasAssertionAuthenticationToken> userDetailService;
	@Autowired
	private LoginSuccessHandler loginSuccessHandler;
	@Autowired
	private Http401UnauthorizedEntryPoint authenticationEntryPoint;

	@Bean
	public ServiceProperties serviceProperties() {
		ServiceProperties sp = new ServiceProperties();
		// cas中心认证服务配置,登录成功后的返回地址
		sp.setService(env.getRequiredProperty("cas.client.url"));
		// 根据需要启用此参数，当url传递renew参数并且为true时，无论用户有无认证cookie都会强制进行验证
		sp.setSendRenew(false);
		return sp;
	}

	@Bean
	public AuthenticationFailureHandler authenticationFailureHander() {
		AuthenticationFailureHandler authFailHander = new SimpleUrlAuthenticationFailureHandler();
		return authFailHander;
	}

	@Bean
	public CasAuthenticationProvider casAuthenticationProvider() {
		CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
		casAuthenticationProvider.setAuthenticationUserDetailsService(userDetailService);
		casAuthenticationProvider.setServiceProperties(serviceProperties());
		casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator());
		casAuthenticationProvider.setKey("an_id_for_this_auth_provider_only");
		return casAuthenticationProvider;
	}

	@Bean
	public SessionAuthenticationStrategy sessionStrategy() {
		SessionFixationProtectionStrategy sessionStrategy = new SessionFixationProtectionStrategy();
		sessionStrategy.setMigrateSessionAttributes(false);
		return sessionStrategy;
	}

	@Bean
	public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
		// 设置cas验证地址
		return new Cas20ServiceTicketValidator(env.getRequiredProperty("cas.url.prefix"));
	}

	@Bean
	public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		casAuthenticationFilter.setAuthenticationManager(authenticationManager());
		casAuthenticationFilter.setAuthenticationDetailsSource(new WebAuthenticationDetailsSource());
		casAuthenticationFilter.setSessionAuthenticationStrategy(sessionStrategy());
		casAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
		// casAuthenticationFilter.setAuthenticationSuccessHandler(simpleUrlAuthenticationSuccessHandler());
		casAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
		// casAuthenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
		//设置cas的登录成功后的返回地址后缀,默认为4版本默认login/cas
		//4版本之前为j_spring_cas_security_check
		casAuthenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login/cas", "GET"));
		return casAuthenticationFilter;
	}
	
	@Bean
	public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
	    CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
	    casAuthenticationEntryPoint.setLoginUrl(env.getRequiredProperty("cas.url.login"));
	    casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
	    // move to /app/login due to cachebuster instead of api/authenticate
	    return casAuthenticationEntryPoint;
	}
	
	@Bean
	public SingleSignOutFilter singleSignOutFilter() {
	    SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
	    singleSignOutFilter.setCasServerUrlPrefix(env.getRequiredProperty("cas.url.prefix"));
	    singleSignOutFilter.setIgnoreInitConfiguration(true);
	    return singleSignOutFilter;
	}
	
	@Bean
	public LogoutFilter requestCasGlobalLogoutFilter() {
	    LogoutFilter logoutFilter =
	          new LogoutFilter(env.getRequiredProperty("cas.url.logout") + "?service="
	        	+ env.getRequiredProperty("app.service.domain"), new SecurityContextLogoutHandler());
	    logoutFilter.setLogoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"));
	    return logoutFilter;
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		//允许地址不需要权限验证
		web.ignoring()
			.antMatchers("/*.ico")
			.antMatchers("/*.html")
			.antMatchers("/common/*")
			.antMatchers("/static/*");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.addFilterBefore(casAuthenticationFilter(), BasicAuthenticationFilter.class)
			.addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class)
			.addFilterBefore(requestCasGlobalLogoutFilter(), LogoutFilter.class)
			.exceptionHandling()
			.authenticationEntryPoint(authenticationEntryPoint)
			.and()
			.csrf().disable()
			.logout()
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
			.invalidateHttpSession(true)
			.deleteCookies("JSESSIONID")
			.permitAll()
			.and()
			.authorizeRequests()
			.antMatchers("/error").permitAll()
			.antMatchers("user/login").permitAll()
			.antMatchers("/**").authenticated()
			.anyRequest().authenticated()
			.and()
			.formLogin()
			.loginPage("/login")
			.permitAll()
			.and().rememberMe();
	}

	@Autowired
	public void configureClobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(casAuthenticationProvider());
		auth.eraseCredentials(false);
	}

}
