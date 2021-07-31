package com.wayapaychat.temporalwallet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/wallet")
public class SwitchAccountController {

	@Autowired
	private SwitchWalletService switchWalletService;

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Create Switch for Toggle", notes = "Toggle Off/No")
	@PostMapping("/switch")
	public ResponseEntity<?> switchOperator(@RequestBody CreateSwitchDTO switchWallet) {
		return switchWalletService.CreateWalletOperator(switchWallet);
	}

	@ApiImplicitParams({
			@ApiImplicitParam(name = "authorization", value = "token", paramType = "header", required = true) })
	@ApiOperation(value = "Toggle Switch", notes = "Toggle Off/No")
	@PutMapping("/switch/toggle")
	public ResponseEntity<?> switchToggle(@RequestBody ToggleSwitchDTO switchWallet) {
		return switchWalletService.UpdateSwitche(switchWallet);
	}

	@ApiOperation(value = "List Switches", notes = "Toggle Off/No")
	@GetMapping("/switch")
	public ResponseEntity<?> ListSwitchOperator() {
		return switchWalletService.ListAllSwitches();
	}
	
	@ApiOperation(value = "List Switches", notes = "Toggle Off/No")
	@GetMapping("/switch/{identity}")
	public ResponseEntity<?> GetSwitchOperator(@PathVariable("identity") String identity) {
		return switchWalletService.GetSwitch(identity);
	}
	
	@ApiOperation(value = "List Switches", notes = "Toggle Off/No")
	@DeleteMapping("/switch/{id}")
	public ResponseEntity<?> DeleteSwitchOperator(@PathVariable("id") Long id) {
		return switchWalletService.DeleteSwitches(id);
	}
}
