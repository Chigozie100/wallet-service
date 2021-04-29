package com.wayapaychat.temporalwallet.security;

import org.springframework.security.core.Authentication;


public interface AuthenticatedUserFacade {
	Authentication getAuthentication();
}
