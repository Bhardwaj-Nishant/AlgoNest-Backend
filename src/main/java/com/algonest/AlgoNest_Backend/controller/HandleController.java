package com.algonest.AlgoNest_Backend.controller;

import com.algonest.AlgoNest_Backend.dto.HandleRequest;
import com.algonest.AlgoNest_Backend.entity.PlatformHandle;
import com.algonest.AlgoNest_Backend.service.HandleService;
import com.algonest.AlgoNest_Backend.util.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/handles")
public class HandleController {

    private final HandleService handleService;
    private final AuthUtil authUtil;

    public HandleController(HandleService handleService, AuthUtil authUtil) {
        this.handleService = handleService;
        this.authUtil = authUtil;
    }

    @PostMapping
    public ResponseEntity<PlatformHandle> addHandle(@RequestBody HandleRequest request) {
        UUID userId = authUtil.getCurrentUserId();
        return ResponseEntity.ok(handleService.addHandle(userId, request.getPlatform(), request.getHandle()));
    }

    @GetMapping
    public ResponseEntity<List<PlatformHandle>> getMyHandles() {
        return ResponseEntity.ok(handleService.getHandlesForUser(authUtil.getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHandle(@PathVariable UUID id) {
        handleService.deleteHandle(id, authUtil.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    // ✅ NEW: Update handle (username only, no sync)
    @PutMapping("/{id}")
    public ResponseEntity<PlatformHandle> updateHandle(@PathVariable UUID id, @RequestBody HandleRequest request) {
        UUID userId = authUtil.getCurrentUserId();
        PlatformHandle updated = handleService.updateHandle(id, userId, request.getPlatform(), request.getHandle());
        return ResponseEntity.ok(updated);
    }
}