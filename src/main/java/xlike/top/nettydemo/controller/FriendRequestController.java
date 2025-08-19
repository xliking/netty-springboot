package xlike.top.nettydemo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xlike.top.nettydemo.common.R;
import xlike.top.nettydemo.pojo.domain.FriendRequest;
import xlike.top.nettydemo.service.FriendService;

import java.util.List;

@RestController
@RequestMapping("/api/friendRequest")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendService friendService;

    @PostMapping("/send")
    public R<String> sendRequest(@RequestParam Long fromUserId,
                                 @RequestParam Long toUserId,
                                 @RequestParam String message) {
        boolean success = friendService.sendFriendRequest(fromUserId, toUserId, message);
        return success ? R.ok("申请发送成功", null) : R.fail("申请发送失败");
    }

    @GetMapping("/{userId}")
    public R<List<FriendRequest>> getRequests(@PathVariable Long userId) {
        return R.ok("OK", friendService.getFriendRequests(userId));
    }

    @PostMapping("/handle")
    public R<String> handleRequest(@RequestParam Long requestId,
                                   @RequestParam boolean accept) {
        boolean success = friendService.handleFriendRequest(requestId, accept);
        return success ? R.ok("处理成功", null) : R.fail("处理失败");
    }
}
