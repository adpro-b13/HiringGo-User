package id.ac.ui.cs.advprog.b13.hiringgo.user.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // BARU
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(AccessDeniedException.class) // BARU
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", "Anda tidak memiliki izin untuk mengakses sumber daya ini."); // Pesan lebih deskriptif
        // body.put("message", ex.getMessage()); // Atau pesan default dari exception
        body.put("path", request.getDescription(false).substring(4)); // Menghilangkan "uri="
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
}