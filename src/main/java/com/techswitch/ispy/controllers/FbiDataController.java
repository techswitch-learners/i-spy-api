package com.techswitch.ispy.controllers;

import com.techswitch.ispy.models.request.SuspectFbiRequestModel;
import com.techswitch.ispy.services.FbiDataService;
import com.techswitch.ispy.services.SuspectsService;
import com.techswitch.ispy.services.token_validation.TokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5000",  "https://techswitch-i-spy-staging.herokuapp.com", "https://techswitch-i-spy.herokuapp.com"})
@RequestMapping(value = "/admin")
public class FbiDataController {

    private FbiDataService fbiDataService;
    private SuspectsService suspectsService;
    private TokenValidator tokenValidator;

    @Autowired
    public FbiDataController(FbiDataService fbiDataService, SuspectsService suspectsService, TokenValidator tokenValidator) {
        this.fbiDataService = fbiDataService;
        this.suspectsService = suspectsService;
        this.tokenValidator = tokenValidator;
    }

    @RequestMapping(value = "/fetch-fbi-data")
    public ResponseEntity fetchFbiData(@RequestHeader(value = "token", required = false)String token) throws IOException, ParseException {
        if (token == null) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        if (tokenValidator.validateToken(token)) {
            System.out.println("[METHOD - FETCHFBIDATA]: starting to get suspects from fbi api");
            List<SuspectFbiRequestModel> suspects = fbiDataService.getSuspectsFromFbiApi();
            System.out.println("[METHOD - FETCHFBIDATA]: finished to get suspects from fbi api");
            System.out.println("[METHOD - FETCHFBIDATA]: started to add suspects to database");
            int insertedSuspects = suspectsService.addSuspectsAndReturnNumberOfInsertedSuspects(suspects);
            System.out.println("[METHOD - FETCHFBIDATA]: Finished to add suspects to database");
            if (insertedSuspects == 0) {
                return ResponseEntity.ok().body(Collections.singletonMap("Success", "No data has been added. Database Up to date."));
            }
            return ResponseEntity.ok().body(Collections.singletonMap("Success", insertedSuspects + " row(s) has been added to database."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }
}
