package dripnote.home.controller;

import dripnote.common.payload.response.ApiResponse;
import dripnote.home.payload.response.HomeResponse;
import dripnote.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/api/main")
    public ResponseEntity<ApiResponse<HomeResponse>> getMain() {
        return ResponseEntity.ok(ApiResponse.ok(homeService.getHome()));
    }
}
