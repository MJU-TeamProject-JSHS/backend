//package com.dding.backend.user;
//
//import org.springframework.web.bind.annotation.*;
//import java.util.List;
//
//public class UserController {
//    private final UserRepository repo;
//    public UserController(UserRepository repo) {
//        this.repo = repo;
//    }
//
//    @GetMapping
//    public List<User> findAll() {
//        return repo.findAll();
//    }
//
//    @PostMapping
//    public User create(@RequestBody User user) {
//        return repo.save(user);
//    }
//}

package com.dding.backend.user;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping
    public List<String> list() {
        return List.of("test");
    }
}
