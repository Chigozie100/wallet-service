package com.wayapaychat.temporalwallet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wayapaychat.temporalwallet.dto.CreateSwitchDTO;
import com.wayapaychat.temporalwallet.dto.ToggleSwitchDTO;
import com.wayapaychat.temporalwallet.service.SwitchWalletService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/switch")
@Tag(name = "SWITCH-WALLET", description = "Switch Wallet Service API")
@Validated
public class SwitchAccountController {

	@Autowired
	private SwitchWalletService switchWalletService;
	
	@ApiOperation(value = "Create Switch for Toggle", notes = "Toggle Off/No", tags = { "SWITCH-WALLET" })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@PostMapping("/wallet")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
	public ResponseEntity<?> switchOperator(@RequestBody CreateSwitchDTO switchWallet) {
		return switchWalletService.CreateWalletOperator(switchWallet);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", dataTypeClass = String.class, value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Toggle Switch", notes = "Toggle Off/No", tags = { "SWITCH-WALLET" })
	@PutMapping("/wallet/toggle")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
	public ResponseEntity<?> switchToggle(@RequestBody ToggleSwitchDTO switchWallet) {
		return switchWalletService.UpdateSwitche(switchWallet);
	}

	@ApiOperation(value = "List Switches", notes = "Toggle Off/No", tags = { "SWITCH-WALLET" })
	@GetMapping("/wallet")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
	public ResponseEntity<?> ListSwitchOperator() {
		return switchWalletService.ListAllSwitches();
	}
	
	@ApiOperation(value = "List Switches", notes = "Toggle Off/No", tags = { "SWITCH-WALLET" })
	@GetMapping("/wallet/{identity}")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
	public ResponseEntity<?> GetSwitchOperator(@PathVariable("identity") String identity) {
		return switchWalletService.GetSwitch(identity);
	}
	
	@ApiOperation(value = "List Switches", notes = "Toggle Off/No", tags = { "SWITCH-WALLET" })
	@DeleteMapping("/wallet/{id}")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
	public ResponseEntity<?> DeleteSwitchOperator(@PathVariable("id") Long id) {
		return switchWalletService.DeleteSwitches(id);

	}
	
	@GetMapping(
            value = "/provider",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
	@ApiOperation(value = "Get Providers", notes = "Get providers", tags = { "SWITCH-WALLET" })
	@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_INITIATOR', 'ROLE_ADMIN_APPROVAL', 'ROLE_ADMIN_REPORT', 'ROLE_ADMIN_APP')")
	public ResponseEntity<?> getProviders() {
		return switchWalletService.getProvider();
	}
	
	@PutMapping(
            value = "/provider/enable/{providerId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(value = "Enable Provider", notes = "Enable a provider", tags = { "SWITCH-WALLET" })
	@PreAuthorize("hasAnyRole('ROLE_ADMIN_OWNER', 'ROLE_ADMIN_SUPER', 'ROLE_ADMIN_APP')")
	public ResponseEntity<?> enableProvider(@PathVariable("providerId") Long providerId) {
		return switchWalletService.enableProvider(providerId);
	}
	
}
