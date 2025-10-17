// package com.vocab.controller;

// import com.vocab.model.User;
// import com.vocab.model.VocabList;
// import com.vocab.repository.UserRepository;
// import com.vocab.repository.VocabListRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.web.bind.annotation.*;

// import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
// import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
// import com.google.api.client.http.javanet.NetHttpTransport;
// import com.google.api.client.json.jackson2.JacksonFactory;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import java.net.URLEncoder;
// import java.nio.charset.StandardCharsets;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
// import java.util.UUID;
// import java.util.concurrent.ConcurrentHashMap;

// @RestController
// @RequestMapping("/api/auth")
// @CrossOrigin(origins = "*")
// public class AuthController {
// private static final Logger log =
// LoggerFactory.getLogger(AuthController.class);

// @Autowired
// private UserRepository userRepository;

// @Autowired
// private VocabListRepository vocabListRepository;

// private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

// @Value("${google.oauth.client-ids:}")
// private List<String> googleClientIds;

// @Value("${google.oauth.web-client-id:}")
// private String webClientId;

// @Value("${google.oauth.web-client-secret:}")
// private String webClientSecret;

// @Value("${google.oauth.redirect-uri:}")
// private String googleRedirectUri;

// private final Map<String, String> stateToResult = new ConcurrentHashMap<>();
// private final Map<String, Long> stateExpiry = new ConcurrentHashMap<>();
// private static final long STATE_TTL_MS = 5 * 60_000;

// // other routes here

// @GetMapping("/google/web/start")
// public ResponseEntity<?> googleWebStart(@RequestParam(value = "state",
// required = false) String givenState) {
// log.info("[google/web/start] received state param: {}", givenState);
// String state = (givenState != null && !givenState.isBlank()) ? givenState :
// UUID.randomUUID().toString();
// log.debug("[google/web/start] using state: {}", state);

// String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
// + "?client_id=" + URLEncoder.encode(webClientId, StandardCharsets.UTF_8)
// + "&redirect_uri=" + URLEncoder.encode(googleRedirectUri,
// StandardCharsets.UTF_8)
// + "&response_type=code"
// + "&scope=" + URLEncoder.encode("openid email", StandardCharsets.UTF_8)
// + "&access_type=offline&prompt=select_account"
// + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);

// stateToResult.remove(state);
// stateExpiry.put(state, System.currentTimeMillis() + STATE_TTL_MS);
// log.info("[google/web/start] issued authUrl and stored state with TTL {} ms",
// STATE_TTL_MS);

// Map<String, Object> response = new HashMap<>();
// response.put("authUrl", authUrl);
// response.put("state", state);
// return ResponseEntity.ok(response);
// }

// @PostMapping("/google")
// public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
// try {
// log.info("[google] login attempt");
// String idTokenString = body.get("idToken");
// if (idTokenString == null || idTokenString.isEmpty()) {
// log.warn("[google] missing idToken in request body");
// return ResponseEntity.badRequest().body(Map.of("error", "idToken is
// required"));
// }

// GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
// new NetHttpTransport(), JacksonFactory.getDefaultInstance())
// .setAudience(googleClientIds)
// .build();

// GoogleIdToken idToken = verifier.verify(idTokenString);
// if (idToken == null) {
// log.warn("[google] token verification failed");
// return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
// .body(Map.of("error", "Invalid ID token"));
// }

// GoogleIdToken.Payload payload = idToken.getPayload();
// String email = payload.getEmail();
// boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
// if (email == null || !emailVerified) {
// log.warn("[google] email missing or unverified; email={}, verified={}",
// email, emailVerified);
// return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
// .body(Map.of("error", "Unverified Google account"));
// }

// Optional<User> userOpt = userRepository.findByEmail(email);
// User user = userOpt.orElseGet(() -> {
// User u = new User();
// u.setEmail(email);
// return userRepository.save(u);
// });
// log.info("[google] login success for userId={} email={}", user.getId(),
// email);

// Map<String, Object> response = new HashMap<>();
// response.put("message", "Login successful");
// response.put("userId", user.getId());
// return ResponseEntity.ok(response);
// } catch (Exception e) {
// log.error("[google] unexpected error during login", e);
// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
// .body(Map.of("error", "Google login failed"));
// }
// }

// @GetMapping("/google/web/callback")
// public ResponseEntity<?> googleWebCallback(@RequestParam String code,
// @RequestParam String state) {
// try {
// log.info("[google/web/callback] received code for state={}", state);
// Long exp = stateExpiry.get(state);
// if (exp == null || System.currentTimeMillis() > exp) {
// log.warn("[google/web/callback] state expired or unknown: {}", state);
// stateToResult.put(state,
// "{\"status\":\"error\",\"error\":\"state_expired\"}");
// return ResponseEntity.ok("You may close this window.");
// }

// org.springframework.util.LinkedMultiValueMap<String, String> form = new
// org.springframework.util.LinkedMultiValueMap<>();
// form.add("code", code);
// form.add("client_id", webClientId);
// form.add("client_secret", webClientSecret);
// form.add("redirect_uri", googleRedirectUri);
// form.add("grant_type", "authorization_code");

// org.springframework.http.HttpHeaders headers = new
// org.springframework.http.HttpHeaders();
// headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

// org.springframework.web.client.RestTemplate rt = new
// org.springframework.web.client.RestTemplate();
// java.util.Map tokenResp = rt.postForEntity(
// "https://oauth2.googleapis.com/token",
// new org.springframework.http.HttpEntity<>(form, headers),
// java.util.Map.class).getBody();

// String idTokenString = tokenResp != null ? (String) tokenResp.get("id_token")
// : null;
// if (idTokenString == null) {
// log.warn("[google/web/callback] token response did not include id_token");
// stateToResult.put(state, "{\"status\":\"error\",\"error\":\"no_id_token\"}");
// return ResponseEntity.ok("You may close this window.");
// }

// GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
// new NetHttpTransport(), JacksonFactory.getDefaultInstance())
// .setAudience(java.util.List.of(webClientId))
// .build();

// GoogleIdToken idToken = verifier.verify(idTokenString);
// if (idToken == null) {
// log.warn("[google/web/callback] id_token verification failed for state={}",
// state);
// stateToResult.put(state,
// "{\"status\":\"error\",\"error\":\"invalid_token\"}");
// return ResponseEntity.ok("You may close this window.");
// }

// String email = idToken.getPayload().getEmail();
// Optional<User> userOpt = userRepository.findByEmail(email);
// User user = userOpt.orElseGet(() -> {
// User u = new User();
// u.setEmail(email);
// return userRepository.save(u);
// });
// log.info("[google/web/callback] login success for userId={} email={}
// state={}", user.getId(), email, state);

// stateToResult.put(state, "{\"status\":\"success\",\"userId\":\"" +
// user.getId() + "\"}");
// return ResponseEntity.ok("Login successful. You may close this window.");
// } catch (Exception e) {
// log.error("[google/web/callback] exception during code exchange or
// verification for state={}", state, e);
// stateToResult.put(state,
// "{\"status\":\"error\",\"error\":\"exchange_failed\"}");
// return ResponseEntity.ok("Login failed. You may close this window.");
// }
// }

// @GetMapping("/google/web/status")
// public ResponseEntity<?> googleWebStatus(@RequestParam String state) {
// log.debug("[google/web/status] poll for state={}", state);
// String result = stateToResult.get(state);
// if (result == null) {
// log.trace("[google/web/status] state pending: {}", state);
// return ResponseEntity.ok(Map.of("status", "pending"));
// }
// try {
// com.fasterxml.jackson.databind.ObjectMapper mapper = new
// com.fasterxml.jackson.databind.ObjectMapper();
// Map parsed = mapper.readValue(result, Map.class);
// log.debug("[google/web/status] state resolved: {} -> {}", state, parsed);
// return ResponseEntity.ok(parsed);
// } catch (Exception e) {
// log.error("[google/web/status] failed to parse result for state={}", state,
// e);
// return ResponseEntity.ok(Map.of("status", "error", "error", "parse_failed"));
// }
// }
// }