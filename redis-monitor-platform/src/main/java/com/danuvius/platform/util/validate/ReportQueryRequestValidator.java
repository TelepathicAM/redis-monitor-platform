package com.danuvius.platform.util.validate;

import com.danuvius.platform.common.constant.CommonConstant;
import com.danuvius.platform.common.exception.validator.IdValidator;
import com.danuvius.platform.common.exception.validator.StringValidator;
import com.danuvius.platform.model.dto.monitor.ReportQueryRequest;

public class ReportQueryRequestValidator {
    public static void validate(ReportQueryRequest req) {
        IdValidator.snowflake(req.getInstanceId(), "instanceId");
        StringValidator.notBlank(req.getReportType(), "reportType");
        StringValidator.isEnumValue(req.getReportType(), CommonConstant.REPORT_TYPES, "reportType");

    }
}
