package ru.alta.thirdproj.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.alta.thirdproj.entites.Role;
import ru.alta.thirdproj.entites.User;
import ru.alta.thirdproj.repositories.UserRepositorySlqO2;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

   // private IRoleRepository roleRepository;
    private BCryptPasswordEncoder passwordEncoder;

    private UserRepositorySlqO2 userProvider;

   // private UserLoginRepositorySlqO2 loginRepository;

    @Autowired
    public void setUserProvider(UserRepositorySlqO2 userProvider) {
        this.userProvider = userProvider;
    }

//    @Autowired
//    public  void  setLoginRepository (UserLoginRepositorySlqO2 loginRepository){
//        this.loginRepository =loginRepository;
//    }

//    @Autowired
//    public void setRoleRepository(IRoleRepository iRoleRepository) {
//        this.roleRepository = iRoleRepository;
//    }


    @Autowired
    public void setPasswordEncoder(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


    public User findByUserName(String username) {

        return userProvider.getUser(username);

//        User  user =  userRepository.findOneByUserName(username);
//        user.setRoles(roles);
     //   return userRepository.findOneByUserName(username);
    }

    @Override
    public boolean save(User systemUser) {
        User user = new User();

//        if (findByUserName(systemUser.getUserName()) != null) {
//            return false;
//        }

        user.setUserName(systemUser.getUserName());
        user.setPassword(passwordEncoder.encode(systemUser.getPassword()));
        user.setUserName(systemUser.getUserName());

//        user.setEmail(systemUser.getEmail());

        //   user.setRoles(Arrays.asList(roleRepository.findOneByName("ROLE_EMPLOYEE")));


//        user.setRoles(Arrays.asList(roleGrpcRepository.findOneByName("ROLE_EMPLOYEE")));

        // userRepository.save(user);
        return true;
    }



//
//    @Override
//    @Transactional
//    public boolean save(SystemUser systemUser) {
//        User user = new User();
//
//        if (findByUserName(systemUser.getUserName()) != null) {
//            return false;
//        }
//
//        user.setUserName(systemUser.getUserName());
//        user.setPassword(passwordEncoder.encode(systemUser.getPassword()));
//        user.setFirstName(systemUser.getFirstName());
//        user.setLastName(systemUser.getLastName());
//        user.setEmail(systemUser.getEmail());
//
//        user.setRoles(Arrays.asList(iRoleRepository.findOneByName("ROLE_EMPLOYEE")));
//
//
////        user.setRoles(Arrays.asList(roleGrpcRepository.findOneByName("ROLE_EMPLOYEE")));
//
//        userRepository.save(user);
//        return true;
//    }

    @Override
   // @Transactional
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userProvider.getUser(userName);

        user.setRoles(userProvider.getUserRole((long) user.getUserId()));
        if (user == null) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getPassword(),
                mapRolesToAuthorities(user.getRoles()));
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }
}
