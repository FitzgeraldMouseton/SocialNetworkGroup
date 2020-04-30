package project.controllers.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.dto.requestDto.NotificationSettingDto;
import project.dto.requestDto.PasswordSetDto;
import project.dto.requestDto.RegistrationRequestDto;
import project.dto.responseDto.MessageResponseDto;
import project.dto.responseDto.ResponseDto;
import project.handlerExceptions.BadRequestException400;
import project.handlerExceptions.EntityAlreadyExistException;
import project.models.NotificationType;
import project.models.Person;
import project.models.PersonNotificationSetting;
import project.services.AccountService;
import project.services.NotificationTypeService;
import project.services.PersonNotificationSettingsService;
import project.services.PersonService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/account/")
public class ApiAccountController {

    private AccountService accountService;
    private PersonService personService;
    private PersonNotificationSettingsService personNotificationSettingsService;
    private NotificationTypeService notificationTypeService;

    @Autowired
    public ApiAccountController(PersonService personService,
                                PersonNotificationSettingsService personNotificationSettingsService,
                                NotificationTypeService notificationTypeService,
                                AccountService accountService) {
        this.personService = personService;
        this.personNotificationSettingsService = personNotificationSettingsService;
        this.notificationTypeService = notificationTypeService;
        this.accountService = accountService;
    }

    @PostMapping(value = "register")
    public ResponseEntity<ResponseDto<MessageResponseDto>> register(@Valid @RequestBody RegistrationRequestDto dto) {
        log.info("контроллер Register отработал");

        if (!dto.getPasswd1().equals(dto.getPasswd2()))
            throw new BadRequestException400();

        try {
            accountService.register(dto);
        } catch(EntityAlreadyExistException e) {
            throw new BadRequestException400();
        }

        return ResponseEntity.ok(new ResponseDto<>(new MessageResponseDto()));
    }

    @PutMapping(value = "password/recovery")
    public ResponseEntity<ResponseDto<MessageResponseDto>> sendRecoveryEmail(@RequestBody Map<String, String> email) {
        return ResponseEntity.ok(personService.sendRecoveryPasswordEmail(email.get("email")));
    }

    @PutMapping("password/set")
    public ResponseEntity<ResponseDto<MessageResponseDto>> setNewPassword(
            @RequestBody PasswordSetDto passwordSetDto, HttpServletRequest request) {
        return ResponseEntity.ok(personService.setNewPassword(passwordSetDto, request));
    }

    @GetMapping("notifications")
    public ResponseEntity<?> getNotificationSettings(HttpServletRequest servletRequest) {
        log.info("trig get");
        Person person = personService.getPersonByToken(servletRequest);
        return ResponseEntity.ok(personNotificationSettingsService.findAllByPerson(person));
    }

    @PutMapping("notifications")
    public ResponseEntity<ResponseDto<PersonNotificationSetting>> updatePersonNotificationSettings(
            @RequestBody NotificationSettingDto dto, HttpServletRequest servletRequest) {
        log.info("trig put");
        Person person = personService.getPersonByToken(servletRequest);
        NotificationType notificationType = notificationTypeService.findByCode(dto.getNotificationType());
        return ResponseEntity.ok(personNotificationSettingsService.
                updateNotificationSetting(person, notificationType));
    }
}