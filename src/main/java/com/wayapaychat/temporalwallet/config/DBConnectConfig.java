package com.wayapaychat.temporalwallet.config;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@ConfigurationProperties()
@Slf4j
public class DBConnectConfig {
	
	@Value("${spring.datasource.url}")
	private String hostURL;
	
	@Value("${spring.datasource.username}")
	private String hostUsername;
	
	@Value("${spring.datasource.password}")
	private String hostPassword;
	
	@Value("${spring.datasource.driver-class-name}")
	private String hostDriver;
	
	public JdbcTemplate jdbcConnect() {
		final DriverManagerDataSource datasource = new DriverManagerDataSource();
		try {
			
			String jdbcUrl = hostURL.replace("temporal-service", "auth-service");
			datasource.setDriverClassName(hostDriver);
			datasource.setUrl(jdbcUrl);
			datasource.setUsername(hostUsername);
			datasource.setPassword(hostPassword);

			log.info("JDBC Database Connection: {} ",datasource.getConnection().getSchema());
		} catch (SQLException e) {
			log.error("Unable to connect: {} ",e.getMessage());
			e.printStackTrace();
		}
		return new JdbcTemplate(datasource);
	}

}
