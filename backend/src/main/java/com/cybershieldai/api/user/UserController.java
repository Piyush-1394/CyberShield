package com.cybershieldai.api.user;

import com.cybershieldai.api.auth.dto.AuthDtos.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public List<UserResponse> list() {
        return users.list();
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public UserService.InviteResponse invite(@Valid @RequestBody UserService.InviteRequest req) {
        return users.invite(req);
    }

    @PatchMapping("/{id}/role")
    public UserResponse role(@PathVariable Long id, @Valid @RequestBody UserService.RoleRequest req) {
        return users.changeRole(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long id) {
        users.remove(id);
    }
}
