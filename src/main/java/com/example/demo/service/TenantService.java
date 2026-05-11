package com.example.demo.service;

import com.example.demo.model.Tenant;
import com.example.demo.model.Room;
import com.example.demo.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RoomService roomService;

    @Transactional
    public Tenant registerAndAllocate(Tenant tenant, Long roomId) {
        // 1. Verify documents (Assume passed from controller check)
        tenant.setDocumentsVerified(true);
        
        // 2. Allocate Bed
        Room allocatedRoom = roomService.allocateBed(roomId);
        tenant.setRoom(allocatedRoom);
        
        // 3. Set Status
        tenant.setStatus(Tenant.Status.ACTIVE);
        
        // 4. Save Tenant
        return tenantRepository.save(tenant);
    }

    public Tenant updateDocumentStatus(Long tenantId, boolean status) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        tenant.setDocumentsVerified(status);
        return tenantRepository.save(tenant);
    }
}
