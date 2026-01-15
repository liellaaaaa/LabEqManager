package org.cong.backend.user.service;

import org.cong.backend.user.dto.*;
import org.cong.backend.user.entity.User;
import org.cong.backend.user.repository.UserRepository;
import org.cong.backend.user.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public PageResponse<UserListResponse> getUserList(Integer page, Integer size,
                                                       String username, String name,
                                                       String department, String roleCode,
                                                       Integer status, String sortBy, String sortOrder) {
        // 分页参数
        int pageNum = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;
        
        // 排序参数
        String sortField = StringUtils.hasText(sortBy) ? sortBy : "createTime";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);
        
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);
        
        // 构建查询条件
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(username)) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }
            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }
            if (StringUtils.hasText(department)) {
                predicates.add(cb.equal(root.get("department"), department));
            }
            if (StringUtils.hasText(roleCode)) {
                predicates.add(cb.equal(root.get("roleCode"), roleCode));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<User> userPage = userRepository.findAll(spec, pageable);
        
        List<UserListResponse> list = userPage.getContent().stream()
                .map(this::toUserListResponse)
                .collect(Collectors.toList());
        
        return new PageResponse<>(list, userPage.getTotalElements(), pageNum, pageSize);
    }

    public UserListResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return toUserListResponse(user);
    }

    @Transactional
    public UserListResponse createUser(CreateUserRequest request) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 验证角色代码是否存在
        if (!roleRepository.findByCode(request.getRoleCode()).isPresent()) {
            throw new RuntimeException("角色代码不存在");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 暂时明文存储
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        user.setRoleCode(request.getRoleCode());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        
        User saved = userRepository.save(user);
        return toUserListResponse(saved);
    }

    @Transactional
    public UserListResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (StringUtils.hasText(request.getRoleCode())) {
            // 验证角色代码是否存在
            if (!roleRepository.findByCode(request.getRoleCode()).isPresent()) {
                throw new RuntimeException("角色代码不存在");
            }
            user.setRoleCode(request.getRoleCode());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        User updated = userRepository.save(user);
        return toUserListResponse(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public void updatePassword(Long id, UpdatePasswordRequest request, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 如果是用户修改自己的密码，需要验证旧密码
        boolean isSelfUpdate = user.getUsername().equals(currentUsername);
        if (isSelfUpdate) {
            if (!StringUtils.hasText(request.getOldPassword())) {
                throw new RuntimeException("修改自己密码时需要提供旧密码");
            }
            // 暂时明文比较，后续改为BCrypt
            if (!request.getOldPassword().equals(user.getPassword())) {
                throw new RuntimeException("旧密码错误");
            }
        }
        
        if (!StringUtils.hasText(request.getNewPassword())) {
            throw new RuntimeException("新密码不能为空");
        }
        
        user.setPassword(request.getNewPassword()); // 暂时明文存储
        userRepository.save(user);
    }

    @Transactional
    public void batchDeleteUsers(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("用户ID列表不能为空");
        }
        userRepository.deleteAllById(ids);
    }

    private UserListResponse toUserListResponse(User user) {
        UserListResponse response = new UserListResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setDepartment(user.getDepartment());
        response.setRoleCode(user.getRoleCode());
        response.setStatus(user.getStatus());
        response.setCreateTime(user.getCreateTime());
        return response;
    }
}

