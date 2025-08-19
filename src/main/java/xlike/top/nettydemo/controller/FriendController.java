package xlike.top.nettydemo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xlike.top.nettydemo.common.R;
import xlike.top.nettydemo.pojo.domain.Friend;
import xlike.top.nettydemo.service.FriendService;

import java.util.List;

@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/{userId}")
    public R<List<Friend>> getFriends(@PathVariable Long userId) {
        return R.ok("OK", friendService.getFriendList(userId));
    }

    @PostMapping("/remark")
    public R<String> updateRemark(@RequestParam Long userId,
                                  @RequestParam Long friendId,
                                  @RequestParam String remark) {
        boolean success = friendService.updateRemark(userId, friendId, remark);
        return success ? R.ok("备注修改成功", null) : R.fail("备注修改失败");
    }

    @PostMapping("/move")
    public R<String> moveToGroup(@RequestParam Long userId,
                                 @RequestParam Long friendId,
                                 @RequestParam Long groupId) {
        boolean success = friendService.moveFriendToGroup(userId, friendId, groupId);
        return success ? R.ok("移动分组成功", null) : R.fail("移动分组失败");
    }
}
