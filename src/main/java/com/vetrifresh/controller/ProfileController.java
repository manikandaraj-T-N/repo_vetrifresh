package com.vetrifresh.controller;

// import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashMap;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.vetrifresh.model.User;
import com.vetrifresh.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ResponseBody;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        if (userDetails == null) return "redirect:/login";

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String name,
                                @RequestParam String phone,
                                @RequestParam String address,
                                @RequestParam String city,
                                @RequestParam String pincode,
                                RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        user.setCity(city);
        user.setPincode(pincode);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/profile";
    }

    @PostMapping("/profile/update")
public String updateProfile(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam(required = false) String phone,
                            @RequestParam(required = false) String address,
                            @RequestParam(required = false) String city,
                            @RequestParam(required = false) String pincode,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {

    User user = userRepository.findByEmail(principal.getName())
                              .orElseThrow();
    user.setName(name);
    user.setEmail(email);
    user.setPhone(phone);
    user.setAddress(address);
    user.setCity(city);
    user.setPincode(pincode);
    userRepository.save(user);

    redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
    return "redirect:/profile";
}


@PostMapping("/profile/upload-image")
@ResponseBody
public Map<String, Object> uploadProfileImage(
        @RequestParam("file") MultipartFile file,
        Principal principal) {

    Map<String, Object> response = new HashMap<>();

    try {
        // Save file to /images/profiles/ folder
        String uploadDir = "src/main/resources/static/images/profiles/";
        new java.io.File(uploadDir).mkdirs();

        String filename = "user_" + principal.getName().hashCode()
                          + "_" + System.currentTimeMillis()
                          + getExtension(file.getOriginalFilename());

        Path path = Paths.get(uploadDir + filename);
        Files.write(path, file.getBytes());

        // Save URL to DB
        String imageUrl = "/images/profiles/" + filename;
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        user.setProfileImage(imageUrl);
        userRepository.save(user);

        response.put("success", true);
        response.put("imageUrl", imageUrl);

    } catch (Exception e) {
        response.put("success", false);
        response.put("error", e.getMessage());
    }

    return response;
}

private String getExtension(String filename) {
    if (filename == null) return ".jpg";
    int dot = filename.lastIndexOf('.');
    return dot >= 0 ? filename.substring(dot) : ".jpg";
}

@ModelAttribute("currentUser")
public User currentUser(@AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) return null;
    return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
}

}
