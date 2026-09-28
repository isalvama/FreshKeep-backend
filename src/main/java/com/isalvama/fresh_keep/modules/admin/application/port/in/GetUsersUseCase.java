package com.isalvama.fresh_keep.modules.admin.application.port.in;

import com.isalvama.fresh_keep.modules.admin.application.command.GetRegisteredUsersCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetUserDetailsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetUserRegistrationsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.PageResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.UserDetailsResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.UserRegistrationMetricResult;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.RegisteredUserDto;

import java.util.List;

public interface GetUsersUseCase {

    List<UserRegistrationMetricResult> getUserRegistrationMetrics(GetUserRegistrationsCommand command);

    PageResult<RegisteredUserDto> getUsers(GetRegisteredUsersCommand command);

    UserDetailsResult getUser(GetUserDetailsCommand command);
}
