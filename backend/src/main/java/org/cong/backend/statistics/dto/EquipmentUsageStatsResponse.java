package org.cong.backend.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设备使用统计响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentUsageStatsResponse {
    
    /**
     * 设备使用统计列表
     */
    private List<EquipmentUsageItem> equipmentList;
    
    /**
     * 总设备数
     */
    private Long totalEquipmentCount;
    
    /**
     * 总使用次数
     */
    private Long totalUsageCount;
    
    /**
     * 设备使用统计项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipmentUsageItem {
        /**
         * 设备ID
         */
        private Long equipmentId;
        
        /**
         * 设备名称
         */
        private String equipmentName;
        
        /**
         * 设备型号
         */
        private String equipmentModel;
        
        /**
         * 资产编号
         */
        private String assetCode;
        
        /**
         * 使用次数（已归还的借用记录数）
         */
        private Long usageCount;
    }
}

