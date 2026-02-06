package com.example.accounting.infrastructure.web.exception;

import com.example.accounting.domain.shared.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("GET", "/api/test");
    }

    @Test
    @DisplayName("ResourceNotFoundExceptionを404で返す")
    void shouldReturn404ForResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", "123");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("User not found with id: '123'");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("BusinessExceptionを400で返す")
    void shouldReturn400ForBusinessException() {
        BusinessException ex = new BusinessException("INVALID_OPERATION", "Cannot perform this operation");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("INVALID_OPERATION");
        assertThat(response.getBody().message()).isEqualTo("Cannot perform this operation");
    }

    @Test
    @DisplayName("サポートされないHTTPメソッドを405で返す")
    void shouldReturn405ForUnsupportedMethod() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");

        ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupportedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(405);
        assertThat(response.getBody().error()).isEqualTo("Method Not Allowed");
    }

    @Test
    @DisplayName("不正なJSONを400で返す")
    void shouldReturn400ForMalformedJson() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON", (Throwable) null, null);

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadableException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Malformed Request");
    }

    @Test
    @DisplayName("バリデーションエラーを400で返す")
    void shouldReturn400ForValidationException() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "command");
        bindingResult.addError(new FieldError("command", "username", "must not be blank"));
        MethodParameter parameter = methodParameter("handle", String.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Validation Error");
        assertThat(response.getBody().message()).isEqualTo("Input validation failed");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
        assertThat(response.getBody().fieldErrors()).hasSize(1);
        assertThat(response.getBody().fieldErrors().getFirst().field()).isEqualTo("username");
        assertThat(response.getBody().fieldErrors().getFirst().message()).isEqualTo("must not be blank");
    }

    @Test
    @DisplayName("必須パラメータ欠如を400で返す")
    void shouldReturn400ForMissingParameter() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("limit", "int");

        ResponseEntity<ErrorResponse> response = handler.handleMissingParameterException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Missing Parameter");
        assertThat(response.getBody().message()).isEqualTo("Required parameter 'limit' is missing");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("型不一致のパラメータを400で返す")
    void shouldReturn400ForTypeMismatch() throws Exception {
        MethodParameter parameter = methodParameter("handle", String.class);
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc",
                Integer.class,
                "id",
                parameter,
                new IllegalArgumentException("type mismatch")
        );

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatchException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Type Mismatch");
        assertThat(response.getBody().message()).isEqualTo("Parameter 'id' should be of type Integer");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("サポートされないメディアタイプを415で返す")
    void shouldReturn415ForUnsupportedMediaType() {
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException(
                MediaType.APPLICATION_XML,
                List.of(MediaType.APPLICATION_JSON)
        );

        ResponseEntity<ErrorResponse> response = handler.handleMediaTypeNotSupportedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(415);
        assertThat(response.getBody().error()).isEqualTo("Unsupported Media Type");
        assertThat(response.getBody().message()).isEqualTo("Content type 'application/xml' is not supported");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("静的リソース未検出を404で返す")
    void shouldReturn404ForNoResourceFound() {
        NoResourceFoundException ex = new NoResourceFoundException(
                HttpMethod.GET,
                "/api/missing",
                "Not Found"
        );

        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("The requested resource was not found");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("AccessDeniedExceptionを403で返す")
    void shouldReturn403ForAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().error()).isEqualTo("Access Denied");
        assertThat(response.getBody().message()).isEqualTo("You do not have permission to access this resource");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("AuthorizationDeniedExceptionを403で返す")
    void shouldReturn403ForAuthorizationDeniedException() {
        AuthorizationDeniedException ex = new AuthorizationDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().error()).isEqualTo("Access Denied");
        assertThat(response.getBody().message()).isEqualTo("You do not have permission to access this resource");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("OptimisticLockExceptionを409で返す")
    void shouldReturn409ForOptimisticLockException() {
        OptimisticLockException ex = new OptimisticLockException("他のユーザーによって更新されています");

        ResponseEntity<ErrorResponse> response = handler.handleOptimisticLockException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Optimistic Lock Error");
        assertThat(response.getBody().message()).isEqualTo("他のユーザーによって更新されています");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("予期しない例外を500で返す")
    void shouldReturn500ForUnexpectedException() {
        RuntimeException ex = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorResponse> response = handler.handleAllUncaughtException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
    }

    @Test
    @DisplayName("ErrorResponseにtimestampが含まれる")
    void shouldIncludeTimestampInErrorResponse() {
        BusinessException ex = new BusinessException("ERROR", "Test error");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("BusinessExceptionのデフォルトエラーコード")
    void shouldUseDefaultErrorCodeForBusinessException() {
        BusinessException ex = new BusinessException("Simple error message");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("BUSINESS_ERROR");
    }

    private MethodParameter methodParameter(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = TestController.class.getDeclaredMethod(methodName, parameterTypes);
        return new MethodParameter(method, 0);
    }

    private static final class TestController {
        void handle(String value) {
            // intentionally empty
        }
    }
}
