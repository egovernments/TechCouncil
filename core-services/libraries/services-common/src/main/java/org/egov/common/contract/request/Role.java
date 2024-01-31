package org.egov.common.contract.request;

import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Role {
    private Long id;

    @Size(max = 128)
    private String name;

    @Size(max = 50)
    private String code;

    @Size(max = 256)
    private String tenantId;
}
