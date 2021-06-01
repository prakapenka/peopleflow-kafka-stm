package localhost.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;

@Data
public class Employee {

    @Email
    @ApiModelProperty(value = "Email used to uniquely identify employee",
            example = "john.smith@example.com")
    String email;

    @ApiModelProperty(example = "any-name")
    String name;
    @ApiModelProperty(example = "any-contract-info")
    String contractInfo;

    @ApiModelProperty(example = "18")
    @Min(0)
    int age;

}
