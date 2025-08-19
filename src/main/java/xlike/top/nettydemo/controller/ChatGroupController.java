package xlike.top.nettydemo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xlike.top.nettydemo.common.R;
import xlike.top.nettydemo.pojo.domain.ChatGroup;
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

    @GetMapping("/{userId}")
    public R<List<ChatGroup>> getGroupsForUser(@PathVariable Long userId) {
        return R.ok("OK", chatGroupService.getGroupsForUser(userId));
    }

    @PostMapping
    public R<String> createGroup(@RequestBody ChatGroup group) {
        boolean success = chatGroupService.createGroup(group);
        return success ? R.ok("群组创建成功", null) : R.fail("群组创建失败");
    }

    @DeleteMapping("/{groupId}")
    public R<String> deleteGroup(@PathVariable Long groupId) {
        boolean success = chatGroupService.deleteGroup(groupId);
        return success ? R.ok("群组删除成功", null) : R.fail("群组删除失败");
    }
}
