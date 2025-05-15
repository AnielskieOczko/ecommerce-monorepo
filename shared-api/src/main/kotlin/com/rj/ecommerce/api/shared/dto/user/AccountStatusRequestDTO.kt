package com.rj.ecommerce.api.shared.dto.user

import jakarta.validation.constraints.NotNull

data class AccountStatusRequest(val active: @NotNull(message = "Account status must be specified") Boolean?)
