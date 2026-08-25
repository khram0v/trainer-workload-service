package io.github.khram0v.trainerworkload.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServiceAccessDeniedHandlerTest {

    @Mock private SecurityResponseWriter responseWriter;
    @InjectMocks private ServiceAccessDeniedHandler accessDeniedHandler;

    @Test
    void handle_delegatesToResponseWriter_withForbiddenStatus() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(request, response, new AccessDeniedException("denied"));

        verify(responseWriter).write(request, response, HttpStatus.FORBIDDEN, "Access denied");
    }
}
