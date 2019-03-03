package com.nsv.jsmbaba;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.bind.annotation.*;

@RestController
public class VaultController {

    @Autowired
    private VaultTemplate vaultTemplate;



    @RequestMapping(value = "/getKey" ,method = RequestMethod.GET)
    public void getSecret(@RequestParam String key){

        System.out.println();
        System.out.println(vaultTemplate.opsForToken().toString());
        VaultResponse vaultResponse = vaultTemplate.read(key);
        System.out.println("key="+key+" .vaultResponse "+vaultResponse);
    }
}
