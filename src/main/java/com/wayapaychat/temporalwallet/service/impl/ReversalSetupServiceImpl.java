package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.entity.ReversalSetup;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.repository.ReversalSetupRepository;
import com.wayapaychat.temporalwallet.service.ReversalSetupService;
import static com.wayapaychat.temporalwallet.util.Constant.*;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ReversalSetupServiceImpl implements ReversalSetupService {


    private final ReversalSetupRepository reversalSetupRepository;

    @Autowired
    public ReversalSetupServiceImpl(ReversalSetupRepository reversalSetupRepository) {
        this.reversalSetupRepository = reversalSetupRepository;
    }


    @Override
    public ResponseEntity<?> create(Integer days) {
        try {
            ReversalSetup reversalSetup = new ReversalSetup();
            reversalSetup.setActive(false);
            reversalSetup.setCreatedAt(new Date());
            reversalSetup.setDays(days);
            reversalSetupRepository.save(reversalSetup);

            return new ResponseEntity<>(new SuccessResponse(DAYS_ADDED, reversalSetup), HttpStatus.OK);
        } catch (Exception e) {
            log.error("UNABLE TO CREATE: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public ResponseEntity<?> update(Integer days, Long id) {
        try {
            ReversalSetup reversalSetup = reversalSetupRepository.findById(id).orElse(null);
            if (reversalSetup !=null){
                reversalSetup.setActive(false);
                reversalSetup.setDays(days);
                reversalSetupRepository.save(reversalSetup);
            }
            return new ResponseEntity<>(new SuccessResponse(UPDATED_SUCCESSUFLLY, reversalSetup), HttpStatus.OK);
        } catch (Exception e) {
            log.error("UNABLE TO UPDATE: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> view(Long id) {
        try {
            Optional<ReversalSetup> reversalSetup = reversalSetupRepository.findById(id);
            if (!reversalSetup.isPresent()){
                throw new CustomException("No Record Found",HttpStatus.NOT_FOUND);
            }else {
                return new ResponseEntity<>(new SuccessResponse(RETRIEVE_DATA, reversalSetup.get()), HttpStatus.OK);
            }

        } catch (Exception e) {
            log.error(UNABLE_TO_UPDATE, e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> viewAll() {
        try {
            List<ReversalSetup> reversalSetups = reversalSetupRepository.findAll();
            if (reversalSetups.isEmpty()){
                throw new CustomException("No Record Found",HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new SuccessResponse(RETRIEVE_DATA, reversalSetups), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public ResponseEntity<?> toggle(Long id) {

        Optional<ReversalSetup> reversalSetup = reversalSetupRepository.findById(id);
        if (!reversalSetup.isPresent()){
          throw new CustomException(NO_RECORDS_FOUND,HttpStatus.NOT_FOUND);
        }else {
            ReversalSetup data = reversalSetup.get();
            data.setActive(!data.isActive());
            reversalSetupRepository.save(data);
            return new ResponseEntity<>(new SuccessResponse(TOGGLE_SUCCESSFULY, data), HttpStatus.OK);
        }
    }
}