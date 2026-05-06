package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.VenueStaff;
import com.pickleball.domain.repositories.VenueStaffRepository;
import com.pickleball.infrastructure.persistence.entities.VenueStaffEntity;
import com.pickleball.infrastructure.persistence.entities.VenueStaffPermissionEntity;
import com.pickleball.infrastructure.persistence.mappers.VenueStaffMapper;
import com.pickleball.infrastructure.persistence.repositories.VenueStaffJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.VenueStaffPermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class VenueStaffRepositoryAdapter implements VenueStaffRepository {

    private final VenueStaffJpaRepository staffJpaRepository;
    private final VenueStaffPermissionJpaRepository permissionJpaRepository;
    private final VenueStaffMapper mapper;

    @Override
    @Transactional
    public VenueStaff save(VenueStaff staff) {
        VenueStaffEntity entity = mapper.toEntity(staff);
        VenueStaffEntity saved = staffJpaRepository.save(entity);

        if (staff.getPermissions() != null && !staff.getPermissions().isEmpty()) {
            permissionJpaRepository.deleteByStaffId(saved.getId());

            List<VenueStaffPermissionEntity> permEntities =
                mapper.toPermissionEntities(saved.getId(), staff.getPermissions());
            permissionJpaRepository.saveAll(permEntities);
        }

        List<VenueStaffPermissionEntity> permissions = permissionJpaRepository.findByStaffId(saved.getId());
        return mapper.toDomain(saved, permissions);
    }

    @Override
    public Optional<VenueStaff> findById(Long id) {
        return staffJpaRepository.findById(id)
                .map(entity -> {
                    List<VenueStaffPermissionEntity> permissions = permissionJpaRepository.findByStaffId(id);
                    return mapper.toDomain(entity, permissions);
                });
    }

    @Override
    public Optional<VenueStaff> findByUsername(String username) {
        return staffJpaRepository.findByUsername(username)
                .map(entity -> {
                    List<VenueStaffPermissionEntity> permissions = permissionJpaRepository.findByStaffId(entity.getId());
                    return mapper.toDomain(entity, permissions);
                });
    }

    @Override
    public List<VenueStaff> findByVenueId(Long venueId) {
        return staffJpaRepository.findByVenueId(venueId).stream()
                .map(entity -> {
                    List<VenueStaffPermissionEntity> permissions = permissionJpaRepository.findByStaffId(entity.getId());
                    return mapper.toDomain(entity, permissions);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<VenueStaff> findActiveByVenueId(Long venueId) {
        return staffJpaRepository.findByVenueIdAndIsActiveTrue(venueId).stream()
                .map(entity -> {
                    List<VenueStaffPermissionEntity> permissions = permissionJpaRepository.findByStaffId(entity.getId());
                    return mapper.toDomain(entity, permissions);
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUsername(String username) {
        return staffJpaRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        permissionJpaRepository.deleteByStaffId(id);
        staffJpaRepository.deleteById(id);
    }
}
