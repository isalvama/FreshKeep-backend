package com.isalvama.fresh_keep.modules.admin.application.port.in;

import com.isalvama.fresh_keep.modules.admin.application.command.GetRegisteredUsersCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetUserDetailsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetUserRegistrationsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.PageResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.UserDetailsResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.UserRegistrationMetricResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.UserSummaryResult;

import java.util.List;

public interface GetUsersUseCase {

    List<UserRegistrationMetricResult> getUserRegistrationMetrics(GetUserRegistrationsCommand command);

    PageResult<UserSummaryResult> getUsers(GetRegisteredUsersCommand command);

    UserDetailsResult getUser(GetUserDetailsCommand command);
}
