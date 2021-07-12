package com.wayapaychat.temporalwallet.util;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.entity.WalletConfig;
import com.wayapaychat.temporalwallet.response.InfoResponse;
import com.wayapaychat.temporalwallet.service.ConfigService;

@Service
public class ParamDefaultValidation {

	@Autowired
	ConfigService configService;

	public boolean validateDefaultCode(String code, String codeName) {
		boolean is_valid = false;
		SuccessResponse sx = (SuccessResponse) configService.getListDefaultCode().getBody();
		@SuppressWarnings("unchecked")
		List<WalletConfig> data = (List<WalletConfig>) sx.getData();
		InfoResponse validate = new InfoResponse(sx.getTimeStamp(), sx.getStatus(), sx.getMessage(), data);
		is_valid = returnValueIfMatch(validate,codeName, code);
		return is_valid;
	}

	public boolean returnValueIfMatch(InfoResponse info, String codeValue) {
		Optional<WalletConfig> ret = info.getData().stream()
					.filter(code -> code.getCodeName().equals(code.getCodeName())).findAny();

			if (ret.isPresent()) {
				return ret.get().getBankConfig().stream().anyMatch(e -> e.getCodeValue().equals(codeValue));		
			}
		return false;
	}

	public boolean returnValueIfMatch(InfoResponse info, String codeName, String codeValue) {

		Optional<WalletConfig> ret = info.getData().stream().filter(code -> code.getCodeName().equals(codeName))
				.findAny();

		if (ret.isPresent()) {
			return ret.get().getBankConfig().stream().anyMatch(e -> e.getCodeValue().equals(codeValue));
		}
		return false;
	}

}
