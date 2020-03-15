package project.controllers.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.dto.LoginRequestDto;
import project.dto.LoginResponseDto;
import project.dto.RegistrationRequestDto;
import project.services.PersonService;

import javax.servlet.http.HttpServletRequest;


/**
 * Данный контроллер служит для примера urls */


@Slf4j
@RestController
@RequestMapping("/auth/")
public class ApiAuthController {


    private PersonService personService;

    @Autowired
    public ApiAuthController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping(value = "registration")
    @ResponseStatus(code = HttpStatus.CREATED)
    void registrations(@RequestBody RegistrationRequestDto dto){
        personService.registrationPerson(dto);
    }

    @PostMapping(value = "login")
    ResponseEntity login(@RequestBody LoginRequestDto dto){
        LoginResponseDto response = personService.login(dto);
        if(response == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не найден");
        return ResponseEntity.ok(response);//необходимо оставить
    }

    @GetMapping(value = "logout")
    ResponseEntity logout(HttpServletRequest request){
        return ResponseEntity.ok().body("Пользователь вышел");
    }




}
