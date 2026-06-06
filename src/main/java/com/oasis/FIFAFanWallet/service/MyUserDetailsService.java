package com.oasis.FIFAFanWallet.service;

import com.oasis.FIFAFanWallet.model.User;
import com.oasis.FIFAFanWallet.model.UserPrincipal;
import com.oasis.FIFAFanWallet.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException(username);
        }
        return new UserPrincipal(user);
    }
}
