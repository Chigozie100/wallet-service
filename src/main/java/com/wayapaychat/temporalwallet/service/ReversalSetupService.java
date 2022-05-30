package com.wayapaychat.temporalwallet.service;

import org.springframework.http.ResponseEntity;

public interface ReversalSetupService {
    ResponseEntity<?> create(Integer days);
    ResponseEntity<?> update(Integer days, Long id);
    ResponseEntity<?> view(Long id);
    ResponseEntity<?> viewAll();
    ResponseEntity<?> toggle(Long id);

}
