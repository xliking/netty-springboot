package xlike.top.nettydemo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xlike.top.nettydemo.common.R;
import xlike.top.nettydemo.pojo.domain.FriendGroup;
import xlike.top.nettydemo.service.FriendService;

import java.util.List;

@RestController
@RequestMapping("/api/friendGroup")
@RequiredArgsConstructor
public class FriendGroupController {

    private final FriendService friendService;

    @GetMapping("/{userId}")
    public R<List<FriendGroup>> getFriendGroups(@PathVariable Long userId) {
        return R.ok("OK", friendService.getFriendGroups(userId));
    }

    @PostMapping
    public R<String> addGroup(@RequestParam Long userId, @RequestParam String groupName) {
        boolean success = friendService.addFriendGroup(userId, groupName);
        return success ? R.ok("分组创建成功", null) : R.fail("分组创建失败");
    }

    @DeleteMapping("/{groupId}")
    public R<String> removeGroup(@PathVariable Long groupId) {
        boolean success = friendService.removeFriendGroup(groupId);
        return success ? R.ok("分组删除成功", null) : R.fail("分组删除失败");
    }
}
