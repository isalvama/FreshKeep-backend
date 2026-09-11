package com.isalvama.fresh_keep.modules.space.application.port.in;

import com.isalvama.fresh_keep.modules.space.application.port.in.command.GetSpaceOverviewCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.GetSpaceOverviewResult;
import org.springframework.stereotype.Service;

@Service
public interface GetSpaceOverviewUseCase {

    GetSpaceOverviewResult execute (GetSpaceOverviewCommand command);

}
