# Backend代码搜索结果报告

生成时间: $(date)

## 1. Dashboard相关代码 (K-1接口 /api/dashboard/metrics)

### 1.1 Controller层
**文件**: /d/tmp/backend/src/main/java/cn/edu/bjfu/nekocafe/controller/StaffController.java

接口定义 (第34-38行):
```java
@GetMapping("/dashboard/metrics")
public Result<DashboardMetricsVO> getDashboardMetrics(
    @RequestParam Integer storeId,
    @RequestParam String range) {
    return Result.success(staffService.getDashboardMetrics(storeId, range));
}
```

### 1.2 Service层
**接口**: /d/tmp/backend/src/main/java/cn/edu/bjfu/nekocafe/service/StaffService.java
```java
DashboardMetricsVO getDashboardMetrics(Integer storeId, String range);
```

**实现**: /d/tmp/backend/src/main/java/cn/edu/bjfu/nekocafe
