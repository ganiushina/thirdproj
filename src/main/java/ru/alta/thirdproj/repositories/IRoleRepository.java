package ru.alta.thirdproj.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.alta.thirdproj.entites.Role;

@Repository
public interface IRoleRepository extends CrudRepository<Role, Long> {
	Role findOneByName(String theRoleName);
}
