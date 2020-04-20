package project.controllers.rest;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import project.security.TokenProvider;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(value = {"/insert.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/delete.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource("/test.properties")
class ApiFriendsControllerTest {

    @Autowired
    private MockMvc mvc;

    private final String token;
    private final String token2;

    @Autowired
    public ApiFriendsControllerTest(TokenProvider tokenProvider) {
        token = tokenProvider.createToken("test1@mail.ru");
        token2 = tokenProvider.createToken("test2@mail.ru");
    }

    @Test
    void getFriendList() throws Exception {
        mvc.perform(get("/api/v1/friends")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.data[0].email", is("test2@mail.ru")))
                .andExpect(jsonPath("$.data[1].email", is("test3@mail.ru")));
    }

    @Test
    void sendFriendRequest() throws Exception {
        mvc.perform(post("/api/v1/friends/3")
                //.contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token2))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void deleteFriend() throws Exception {
        mvc.perform(delete("/api/v1/friends/10")
                .header("Authorization", token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getFriendRequests() throws Exception {
        mvc.perform(get("/api/v1/friends/request")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", token2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.data[0].email", is("test3@mail.ru")));
    }

    @Test
    void recommendations() throws Exception {
        mvc.perform(get("/api/v1/friends/recommendations")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", token2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.data[0].email", is("test3@mail.ru")));

    }
}