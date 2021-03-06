package com.plestreet.community.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plestreet.community.domain.User;
import com.plestreet.community.dto.RequestSignInDto;
import com.plestreet.community.dto.SignUpDto;
import com.plestreet.community.exception.UserAlreadyExistException;
import com.plestreet.community.repository.UserRepo;
import com.plestreet.community.security.TokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

	private final UserRepo userRepo;
	private final TokenProvider tokenProvider;
	private final PasswordEncoder passwordEncoder;


	//로그인시 토큰값 반환
	public String handleLogin(RequestSignInDto requestSignInDto) {
		User user = userRepo.findByUserId(requestSignInDto.getUserId()).orElseThrow(() -> new UsernameNotFoundException("가입되어 있는 유저가 아닙니다"));
		if(!passwordEncoder.matches(requestSignInDto.getUserPwd(), user.getUserPwd())) {
			throw new UsernameNotFoundException("비밀번호가 잘못 되었습니다.");
		}
		return tokenProvider.createToken(String.valueOf(requestSignInDto.getUserId()));
	}

	//회원가입
	public void handleSignUp(SignUpDto signUpDto) {
		userExistCheck(signUpDto.getUserId());
		userRepo.save(
			User.builder()
				.userId(signUpDto.getUserId())
				.userPwd(passwordEncoder.encode(signUpDto.getUserPwd()))
				.userName(signUpDto.getUserName())
				.userPhone(signUpDto.getUserPhone())
				.build()
			);
	}

	public void userExistCheck(String toCheckUserId){
		if(userRepo.findByUserId(toCheckUserId).isPresent()) throw new UserAlreadyExistException("동일아이디가 존재합니다");
	}
}
