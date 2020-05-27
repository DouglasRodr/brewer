package com.algaworks.brewer.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SegurancaController {

	@GetMapping("/login")
	public ModelAndView login(@AuthenticationPrincipal User user) {
		if (user != null) {
			return new ModelAndView("redirect:/");
		}
		
		ModelAndView mv = new ModelAndView("Login");
		return mv;
	}
	
}