package run.halo.app.controller.admin.api;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import run.halo.app.annotation.DisableOnCondition;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.model.dto.EnvironmentDTO;
import run.halo.app.model.dto.LoginPreCheckDTO;
import run.halo.app.model.dto.StatisticDTO;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.MFAType;
import run.halo.app.model.params.LoginParam;
import run.halo.app.model.params.ResetPasswordParam;
import run.halo.app.model.properties.PrimaryProperties;
import run.halo.app.model.support.BaseResponse;
import run.halo.app.security.token.AuthToken;
import run.halo.app.service.AdminService;
import run.halo.app.service.OptionService;

import javax.validation.Valid;

/**
 * Admin controller.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-19
 */
//日志注解
@Slf4j
//是@ResponseBody和@Controller的组合注解
@RestController
//将web请求映射到请求处理类中
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    private final OptionService optionService;

    public AdminController(AdminService adminService, OptionService optionService) {
        this.adminService = adminService;
        this.optionService = optionService;
    }

//    将Get请求映射到处理方法中
    @GetMapping(value = "/is_installed")
//    说明方法的作用
    @ApiOperation("Checks Installation status")
    public boolean isInstall() {
        return optionService.getByPropertyOrDefault(PrimaryProperties.IS_INSTALLED, Boolean.class, false);
    }
//    将POST请求映射到处理方法中
    @PostMapping("login/precheck")
    @ApiOperation("Login")
//    登录操作加锁，防止重复登录
    @CacheLock(autoDelete = false, prefix = "login_precheck")
//    @RequestBody将http请求正文插入方法中
//    @Valid验证数据
//    DTO表示数据传输对象
    public LoginPreCheckDTO authPreCheck(@RequestBody @Valid LoginParam loginParam) {
        final User user = adminService.authenticate(loginParam);
        return new LoginPreCheckDTO(MFAType.useMFA(user.getMfaType()));
    }

    @PostMapping("login")
//    说明方法的作用
    @ApiOperation("Login")
//    等操作加锁，防止重复操作
    @CacheLock(autoDelete = false, prefix = "login_auth")
    public AuthToken auth(@RequestBody @Valid LoginParam loginParam) {
        return adminService.authCodeCheck(loginParam);
    }

    @PostMapping("logout")
    @ApiOperation("Logs out (Clear session)")
    @CacheLock(autoDelete = false)
    public void logout() {
        adminService.clearToken();
    }

    @PostMapping("password/code")
    @ApiOperation("Sends reset password verify code")
    @CacheLock(autoDelete = false)
//    设置某些条件下禁止访问api
    @DisableOnCondition
    public void sendResetCode(@RequestBody @Valid ResetPasswordParam param) {
        adminService.sendResetPasswordCode(param);
    }

    @PutMapping("password/reset")
    @ApiOperation("Resets password by verify code")
    @CacheLock(autoDelete = false)
    @DisableOnCondition
    public void resetPassword(@RequestBody @Valid ResetPasswordParam param) {
        adminService.resetPasswordByCode(param);
    }

    @PostMapping("refresh/{refreshToken}")
    @ApiOperation("Refreshes token")
    @CacheLock(autoDelete = false)
//    @PathVariable注解识别url中一个模板
    public AuthToken refresh(@PathVariable("refreshToken") String refreshToken) {
        return adminService.refreshToken(refreshToken);
    }

    @GetMapping("counts")
    @ApiOperation("Gets count info")
//    说明方法过期
    @Deprecated
    public StatisticDTO getCount() {
        return adminService.getCount();
    }

    @GetMapping("environments")
    @ApiOperation("Gets environments info")
    public EnvironmentDTO getEnvironments() {
        return adminService.getEnvironments();
    }

//    和PostMapping等同，重点是更新信息
    @PutMapping("halo-admin")
    @ApiOperation("Updates halo-admin manually")
    @Deprecated
    public void updateAdmin() {
        adminService.updateAdminAssets();
    }

    @GetMapping(value = "halo/logfile")
    @ApiOperation("Gets halo log file content")
    @DisableOnCondition
    public BaseResponse<String> getLogFiles(@RequestParam("lines") Long lines) {
        return BaseResponse.ok(HttpStatus.OK.getReasonPhrase(), adminService.getLogFiles(lines));
    }
}
