package project.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.dto.requestDto.LoginRequestDto;
import project.dto.requestDto.RegistrationRequestDto;
import project.dto.responseDto.MessageResponseDto;
import project.dto.responseDto.PersonDtoWithToken;
import project.dto.responseDto.ResponseDto;
import project.handlerExceptions.EmailAlreadyRegisteredException;
import project.models.Person;
import project.models.Role;
import project.models.Token;
import project.models.VerificationToken;
import project.repositories.PersonRepository;
import project.repositories.TokenRepository;
import project.security.TokenProvider;
import project.services.email.EmailService;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    VerificationTokenService verificationTokenService;

    @Autowired
    EmailService emailService;



    //    @PostConstruct
//    public void init() {
//        Person person = new Person();
//        person.setFirstName("Ilya");
//        person.setEmail("il@mail.ru");
//
//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String password = passwordEncoder.encode("qweasdzxc");
//        person.setPassword(password);
//        personRepository.save(person);
//
//    }
    public boolean registrationPerson(RegistrationRequestDto dto) throws EmailAlreadyRegisteredException {
        Person exist = personRepository.findPersonByEmail(dto.getEmail()).orElse(null);
        if (exist != null) throw new EmailAlreadyRegisteredException();
        Person person = new Person();
        Role role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        person.setEmail(dto.getEmail());
        person.setPassword(encoder.encode(dto.getPasswd1()));
        person.setFirstName(dto.getFirstName());
        person.setLastName(dto.getLastName());
        person.setRegDate(new Date());
        person.setRoles(Collections.singleton(role));
        personRepository.save(person);
        return true;
    }

    public ResponseDto<PersonDtoWithToken> login(LoginRequestDto dto){
        String email = dto.getEmail();
        Person person = personRepository.findPersonByEmail(email).orElse(null);//необходимо оставить
        if (person == null) {
            throw new UsernameNotFoundException("User not found with email + " + email);
        }
        Token jwtToken = new Token();
        String token = tokenProvider.createToken(email);//необходимо оставить
        PersonDtoWithToken personDto = new PersonDtoWithToken();
        personDto.setId(person.getId());
        personDto.setFirstName(person.getFirstName());
        personDto.setLastName(person.getLastName());
        personDto.setRegDate(person.getRegDate());
        personDto.setBirthDate(person.getBirthDate());
        personDto.setEmail(person.getEmail());
        personDto.setPhone(person.getPhone());
        personDto.setPhoto(person.getPhoto());
        personDto.setAbout(person.getAbout());
        personDto.setCity(person.getCity());
        personDto.setCountry(person.getCountry());
        personDto.setMessagesPermission(person.getMessagesPermission());
        personDto.setLastOnlineTime(person.getLastOnlineTime());
        personDto.setBlocked(person.isBlocked());
        personDto.setToken(token);

        jwtToken.setToken(token);
        jwtToken.setDateCreated(Calendar.getInstance());
        jwtToken.setEmailUser(person.getEmail());
        tokenRepository.save(jwtToken);

        return new ResponseDto<>(personDto);
    }

    public ResponseDto<MessageResponseDto> sendRecoveryPasswordEmail(String email) {

        Person person = findPersonByEmail(email);
        if (person != null) {
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken(token, person.getId(), 20); //у нас же есть валидация токена? заменить
            String link = "http://localhost:8086/account/password/set/" + token;    //какая должна быть ссылка
            String message = String.format("Для восстановления пароля перейдите по ссылке %s", link );
            verificationTokenService.save(verificationToken);
            emailService.send(email, "Password recovery", message);

        } else {
            //обработать
        }
        return new ResponseDto<>(new MessageResponseDto());
    }

    public Person findPersonByEmail(String email){
        return personRepository.findPersonByEmail(email).orElse(null);
    }

    public boolean logout(HttpServletRequest request){
        String token = tokenProvider.resolveToken(request);
        tokenRepository.deleteByToken(token);
        return true;
    }

    public Person findPersonById(Integer id) {
        Optional<Person> optionalPerson = personRepository.findById(id);
        return optionalPerson.orElse(null);
    }

    public boolean blockPersonById(Integer id, Boolean block) {
        Person person = findPersonById(id);
        if (person != null) {
            person.setBlocked(block);
            personRepository.save(person);
            return true;
        }
        return false;
    }
}
