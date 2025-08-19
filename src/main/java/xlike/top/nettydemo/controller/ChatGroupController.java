package xlike.top.nettydemo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xlike.top.nettydemo.common.R;
import xlike.top.nettydemo.service.ChatGroupService;

import java.util.List;

/**
 * 群组管理控制器
 * 提供 REST 接口给前端调用
 *
 * @author 
 */
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class ChatGroupController {

    private final ChatGroupService chatGroupService;

    /**
     * 判断用户是否在群组内
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否在群组内
     */
    @GetMapping("/{groupId}/user/{userId}/exists")
    public R<Boolean> isUserInGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        boolean result = chatGroupService.isUserInGroup(groupId, userId);
        return R.ok("查询成功", result);
    }

    /**
     * 获取群组内所有成员ID
     *
     * @param groupId 群组ID
     * @return 成员ID列表
     */
    @GetMapping("/{groupId}/members")
    public R<List<Long>> getGroupMemberIds(@PathVariable Long groupId) {
        List<Long> userIds = chatGroupService.getGroupMemberIds(groupId);
        return R.ok("查询成功", userIds);
    }

    /**
     * 获取群组成员数量
     *
     * @param groupId 群组ID
     * @return 成员数量
     */
    @GetMapping("/{groupId}/count")
    public R<Integer> getGroupMemberCount(@PathVariable Long groupId) {
        int count = chatGroupService.getGroupMemberCount(groupId);
        return R.ok("查询成功", count);
    }

    /**
     * 从群组中移除一个用户
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{groupId}/user/{userId}")
    public R<Boolean> removeUserFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        boolean result = chatGroupService.removeUserFromGroup(groupId, userId);
        return result ? R.ok("移除成功", true) : R.fail("移除失败");
    }

    /**
     * 往群组中添加新用户
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/{groupId}/user/{userId}")
    public R<Boolean> addUserToGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        boolean result = chatGroupService.addUserToGroup(groupId, userId);
        return result ? R.ok("添加成功", true) : R.fail("添加失败");
    }
}
