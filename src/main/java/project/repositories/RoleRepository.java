package project.repositories;

import org.springframework.data.repository.CrudRepository;
import project.models.Role;

public interface RoleRepository extends CrudRepository<Role, Integer> {
    Role findByName(String name);
}
