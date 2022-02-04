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
import ru.alta.thirdproj.entites.UserLogin;
import ru.alta.thirdproj.repositories.UserLoginRepositorySlqO2;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserLoginServiceImpl implements UserLoginService {

    private BCryptPasswordEncoder passwordEncoder;

    private UserLoginRepositorySlqO2 userProvider;

    @Autowired
    public void setUserProvider(UserLoginRepositorySlqO2 userProvider) {
        this.userProvider = userProvider;
    }

    @Autowired
    public void setPasswordEncoder(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserLogin findLoginUserByUserName(String username) {
        return userProvider.getUser(username);
    }

    @Override
    public boolean save(UserLogin systemUser) {
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



    @Override
    // @Transactional
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserLogin user = userProvider.getUser(userName);

        user.setRoles(userProvider.getUserRole(user.getUserId()));
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
