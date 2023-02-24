package com.vaadin.hackathon240.manolo.data.endpoint;

import com.vaadin.hackathon240.manolo.data.entity.Person;
import com.vaadin.hackathon240.manolo.data.service.PersonService;
import dev.hilla.Endpoint;
import dev.hilla.Nonnull;
import dev.hilla.exception.EndpointException;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Endpoint
@PermitAll
public class PersonEndpoint {

    private final PersonService service;

    public PersonEndpoint(PersonService service) {
        this.service = service;
    }

    @Nonnull
    public Page<@Nonnull Person> list(Pageable page) {
        return service.list(page);
    }

    public Optional<Person> get(@Nonnull Long id) {
        return service.get(id);
    }

    @Nonnull
    public Person update(@Nonnull Person entity) {
        try {
            return service.update(entity);
        } catch (OptimisticLockingFailureException e) {
            throw new EndpointException("Somebody else has updated the data while you were making changes.");
        }
    }

    public void delete(@Nonnull Long id) {
        service.delete(id);
    }

    public int count() {
        return service.count();
    }

}
