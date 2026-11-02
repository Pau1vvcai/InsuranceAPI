package com.example.insuranceapi.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record CreateOrderRequest(

        @NotNull(message = "产品ID不能为空")
        @Positive(message = "产品ID必须大于0")
        Long productId,

        @NotNull(message = "投保人信息不能为空")
        @Valid
        Applicant applicant,

        @NotNull(message = "被保险人信息不能为空")
        @Valid
        InsuredPerson insuredPerson
) {

    public record Applicant(

            @NotBlank(message = "投保人姓名不能为空")
            @Size(max = 50, message = "投保人姓名不能超过50个字符")
            String name,

            @NotBlank(message = "投保人身份证号不能为空")
            @Pattern(
                    regexp = "^\\d{17}[\\dXx]$",
                    message = "投保人身份证号格式不正确"
            )
            String idCard,

            @NotBlank(message = "投保人手机号不能为空")
            @Pattern(
                    regexp = "^1[3-9]\\d{9}$",
                    message = "投保人手机号格式不正确"
            )
            String phone
    ) {
    }

    public record InsuredPerson(

            @NotBlank(message = "被保险人姓名不能为空")
            @Size(max = 50, message = "被保险人姓名不能超过50个字符")
            String name,

            @NotBlank(message = "被保险人身份证号不能为空")
            @Pattern(
                    regexp = "^\\d{17}[\\dXx]$",
                    message = "被保险人身份证号格式不正确"
            )
            String idCard,

            @NotNull(message = "与投保人关系不能为空")
            Relationship relationship
    ) {
    }
}