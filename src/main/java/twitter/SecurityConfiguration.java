package twitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
    private TwitterAuthenticationEntryPoint authenticationEntryPoint;
	
	@Autowired
	private PasswordEncoder passEncoder;
	
	@Bean
	public PasswordEncoder setPasswordEncoder(){
		return new BCryptPasswordEncoder();
	}

	@Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
	
		
		//String encoded = passEncoder.encode(password);	
				
        auth.inMemoryAuthentication().withUser("batman").password("batman").authorities("ROLE_USER");
        auth.inMemoryAuthentication().withUser("superman").password("superman").authorities("ROLE_USER");
        auth.inMemoryAuthentication().withUser("catwoman").password("catwoman").authorities("ROLE_USER");
        auth.inMemoryAuthentication().withUser("daredevil").password("daredevil").authorities("ROLE_USER");
        auth.inMemoryAuthentication().withUser("alfred").password("alfred").authorities("ROLE_USER");
        auth.inMemoryAuthentication().withUser("dococ").password("dococ").authorities("ROLE_USER");
        auth.inMemoryAuthentication().withUser("zod").password("zod").authorities("ROLE_USER");
        auth.inMemoryAuthentication().withUser("spiderman").password("spiderman").authorities("ROLE_USER");
        auth.inMemoryAuthentication().withUser("ironman").password("ironman").authorities("ROLE_USER");
        auth.inMemoryAuthentication().withUser("profx").password("profx").authorities("ROLE_USER");
    }
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/h2-console/*").permitAll()
            .anyRequest().authenticated()
            .and()
            .httpBasic()
            .authenticationEntryPoint(authenticationEntryPoint);

        http.csrf().disable();
        http.headers().frameOptions().disable();
    }
}