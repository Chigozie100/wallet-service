<<<<<<< HEAD
/*package com.wayapaychat.temporalwallet.controller;
=======
package com.wayapaychat.temporalwallet.controller;
>>>>>>> master

import com.wayapaychat.temporalwallet.pojo.AccountPojo;
import com.wayapaychat.temporalwallet.pojo.UserPojo;
import com.wayapaychat.temporalwallet.service.AccountService;
import com.wayapaychat.temporalwallet.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
public class UserController {

    @Autowired
    UserService userService;

    @ApiOperation(value = "Create a User", hidden = false)
    @PostMapping(path = "/create-user")
    public ResponseEntity creteUseer(@RequestBody UserPojo userPojo) {
        return userService.createUser(userPojo);
    }

}
<<<<<<< HEAD
*/
=======
>>>>>>> master
