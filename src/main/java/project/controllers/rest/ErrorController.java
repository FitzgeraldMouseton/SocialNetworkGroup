package project.controllers.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import project.dto.error.Error;
import project.dto.error.enums.ErrorDescriptionEnum;
import project.dto.error.enums.ErrorEnum;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
public class ErrorController {

    @GetMapping("/error")
    public ResponseEntity<?> getError(HttpServletRequest request, ServletRequest requestSerc){
        log.info("Неавторизованный пользователь");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Error(ErrorEnum.UNAUTHORIZED.getError(), ErrorDescriptionEnum.UNAUTHORIZED.getError()));
    }
}
