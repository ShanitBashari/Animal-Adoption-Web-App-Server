//package com.adoption.petadoptionserver.controller;
//
//import org.springframework.context.support.DefaultMessageSourceResolvable;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
//        Map<String, String> body = new HashMap<>();
//        body.put("message", ex.getMessage());
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
//    }
//
//    @ExceptionHandler(IllegalStateException.class)
//    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
//        Map<String, String> body = new HashMap<>();
//        body.put("message", ex.getMessage());
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
//        Map<String, String> body = new HashMap<>();
//
//        String message = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .findFirst()
//                .map(DefaultMessageSourceResolvable::getDefaultMessage)
//                .orElse("Validation failed");
//
//        body.put("message", message);
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
//        ex.printStackTrace();
//
//        Map<String, String> body = new HashMap<>();
//        body.put("message", "Unexpected server error");
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
//    }
//}