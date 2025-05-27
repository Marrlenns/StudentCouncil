package kg.alatoo.studentcouncil.controllers;

import kg.alatoo.studentcouncil.entities.Role;
import kg.alatoo.studentcouncil.entities.User;
import kg.alatoo.studentcouncil.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        userRepo.save(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepo.findByUsername(username).orElseThrow();
        model.addAttribute("user", user);
        return "profile";
    }


    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User updatedUser, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepo.findByUsername(username).orElseThrow();

        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setUsername(updatedUser.getUsername());

        userRepo.save(user);
        redirectAttributes.addFlashAttribute("success", true);
        return "redirect:/profile";
    }
}