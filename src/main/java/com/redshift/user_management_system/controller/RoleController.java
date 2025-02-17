package com.redshift.user_management_system.controller;

import com.redshift.user_management_system.model.Role;
import com.redshift.user_management_system.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/role")
public class RoleController {
    @Autowired
    RoleRepository roleRepository;

    @PostMapping("/save")
    public String add(@RequestBody Role role){
        roleRepository.save(role);
        return "success";
    }
}
