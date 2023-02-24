package com.vaadin.hackathon240.manolo.data.endpoint;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hackathon240.manolo.data.entity.User;
import com.vaadin.hackathon240.manolo.security.AuthenticatedUser;
import dev.hilla.Endpoint;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;

@Endpoint
@AnonymousAllowed
public class UserEndpoint {

    @Autowired
    private AuthenticatedUser authenticatedUser;

    public Optional<User> getAuthenticatedUser() {
        return authenticatedUser.get();
    }
}
