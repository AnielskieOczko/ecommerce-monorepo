package com.rj.ecommerce.api.shared.dto.user

@JvmRecord
data class UserStatisticsDTO(
    val totalUsers: Long,
    val activeUsers: Long,
    val inactiveUsers: Long,
    val newUsersThisMonth: Long,
    val roleDistribution: MutableMap<String?, Long?>?,
    val registrationsByMonth: MutableList<MonthlyRegistration?>?,
    val lastLoginDistribution: LoginDistribution?,
    val topCountries: MutableList<CountryStats?>?,
    val verificationStatus: VerificationStatus?
) {
    @JvmRecord
    data class MonthlyRegistration(val month: String?, val count: Long)

    @JvmRecord
    data class LoginDistribution(
        val last24Hours: Long,
        val lastWeek: Long,
        val lastMonth: Long,
        val inactive30Days: Long
    )

    @JvmRecord
    data class CountryStats(val country: String?, val count: Long)

    @JvmRecord
    data class VerificationStatus(val verified: Long, val unverified: Long)
}
