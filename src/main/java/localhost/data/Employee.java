package localhost.data;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.Data;


@Data
public class Employee {

    @Email
    @Schema(description = "Email used to uniquely identify employee",
            example = "john.smith@example.com")
    String email;

    @Schema(example = "any-name")
    String name;
    @Schema(example = "any-contract-info")
    String contractInfo;

    @Schema(example = "18")
    @Min(0)
    int age;

}
