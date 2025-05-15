package com.rj.ecommerce.api.shared.dto.user

@JvmRecord
data class AdminChangeUserAuthorityRequest(val authorities: MutableSet<String?>?)
