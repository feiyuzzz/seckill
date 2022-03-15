package com.ms.seckill.validator;

import com.ms.seckill.utils.VaildatorUtil;
import org.thymeleaf.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    private boolean required = false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (required) {
            return VaildatorUtil.isMobile(value);
        } else {
            if (StringUtils.isEmpty(value)){
                return true;
            }else {
                return VaildatorUtil.isMobile(value);
            }
        }
    }
}
