package baristation.home.controller;

import baristation.common.payload.response.ApiResponse;

import baristation.common.logging.TraceIdUtil;
import baristation.home.payload.response.HomeResponse;
import baristation.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/api/main")
    public ResponseEntity<ApiResponse<HomeResponse>> getMain() {

        log.info("[Home] getMain start. traceId={}", TraceIdUtil.getTraceId());
        HomeResponse response = homeService.getHome();
        log.info("[Home] getMain done. flavorCount={}, productCount={}, traceId={}",
                response.flavors().size(), response.products().size(), TraceIdUtil.getTraceId());
        return ApiResponse.ok(response);
        }
}
