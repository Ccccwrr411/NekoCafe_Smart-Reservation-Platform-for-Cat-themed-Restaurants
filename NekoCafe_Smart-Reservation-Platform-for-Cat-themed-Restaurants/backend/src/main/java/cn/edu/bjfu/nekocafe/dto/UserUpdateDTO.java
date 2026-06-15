package cn.edu.bjfu.nekocafe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息更新 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    
    private String nickName;
    
    private String avatarUrl;
    
    private String phone;
    
    private String email;
}