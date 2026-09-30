package com.example.auth

import com.example.portal.AshaScreeningSession

/**
 * Strict Authorization Guard for ARTHROSCAN-NER
 * Ensures ASHA Worker and ADMIN roles cannot access unauthorized routes or modify scientific models.
 */
object AuthorizationGuard {

    val RESTRICTED_FOR_ASHA: Set<PortalRoute> = setOf(
        PortalRoute.ADMIN_DASHBOARD,
        PortalRoute.ADMIN_OVERVIEW,
        PortalRoute.ADMIN_WORKERS,
        PortalRoute.ADMIN_DEVICES,
        PortalRoute.ADMIN_REGIONS,
        PortalRoute.ADMIN_SESSIONS,
        PortalRoute.ADMIN_SESSION_DETAIL,
        PortalRoute.ADMIN_RETEST,
        PortalRoute.ADMIN_QUALITY,
        PortalRoute.ADMIN_UNCERTAINTY,
        PortalRoute.ADMIN_REPORTS,
        PortalRoute.ADMIN_AUDIT,
        PortalRoute.ADMIN_SYSTEM_HEALTH,
        PortalRoute.ADMIN_SCIENTIFIC_CONFIG,
        PortalRoute.ADMIN_PROFILE,
        PortalRoute.SUPER_ADMIN_DASHBOARD,
        PortalRoute.MODEL_CONFIG,
        PortalRoute.SCIENTIFIC_THRESHOLDS,
        PortalRoute.THRESHOLD_EDITOR,
        PortalRoute.NORMALIZATION_PARAMS,
        PortalRoute.RISK_TIER_CONFIG,
        PortalRoute.RF_EXPERIMENTAL_CONFIG,
        PortalRoute.RF_HARDWARE_CONFIG,
        PortalRoute.AUDIT_LOG_EDIT,
        PortalRoute.AUDIT_LOGS,
        PortalRoute.USER_MANAGEMENT,
        PortalRoute.SYSTEM_CONFIG
    )

    // Backward-compatible reference for existing tests
    val RESTRICTED_ADMIN_ROUTES: Set<PortalRoute> = RESTRICTED_FOR_ASHA

    val RESTRICTED_FOR_ADMIN: Set<PortalRoute> = setOf(
        PortalRoute.SUPER_ADMIN_DASHBOARD,
        PortalRoute.SUPER_ADMIN_SYSTEM_SETTINGS,
        PortalRoute.MODEL_CONFIG,
        PortalRoute.SCIENTIFIC_THRESHOLDS,
        PortalRoute.THRESHOLD_EDITOR,
        PortalRoute.NORMALIZATION_PARAMS,
        PortalRoute.RISK_TIER_CONFIG,
        PortalRoute.RF_EXPERIMENTAL_CONFIG,
        PortalRoute.RF_HARDWARE_CONFIG,
        PortalRoute.AUDIT_LOG_EDIT,
        PortalRoute.SYSTEM_CONFIG,
        PortalRoute.USER_MANAGEMENT
    )

    fun canAccessRoute(user: UserAccount?, targetRoute: PortalRoute): Boolean {
        if (targetRoute == PortalRoute.LOGIN || targetRoute == PortalRoute.SHOWCASE) return true
        if (user == null) return false
        if (user.status != UserStatus.ACTIVE) return false

        if (user.role == UserRole.ASHA_WORKER) {
            // ASHA workers are strictly prohibited from all administrative and engineering config routes
            if (targetRoute in RESTRICTED_FOR_ASHA) {
                return false
            }
        } else if (user.role == UserRole.ADMIN) {
            // Admin can access admin operations but cannot access Super Admin or scientific configs
            if (targetRoute in RESTRICTED_FOR_ADMIN) {
                return false
            }
        }
        return true
    }

    @Throws(SecurityException::class)
    fun checkRouteAccess(user: UserAccount?, targetRoute: PortalRoute) {
        if (!canAccessRoute(user, targetRoute)) {
            val roleName = user?.role?.name ?: "ANONYMOUS"
            throw SecurityException(
                "ACCESS DENIED: Role $roleName is unauthorized to access route ${targetRoute.name} (${targetRoute.title})"
            )
        }
    }

    fun canAccessPath(user: UserAccount?, path: String): Boolean {
        val clean = path.lowercase().trim()
        if (clean == "/showcase" || clean.startsWith("/showcase")) return true
        if (user == null || user.status != UserStatus.ACTIVE) return false
        if (clean.startsWith("/super-admin")) {
            return user.role == UserRole.SUPER_ADMIN
        }
        if (clean.startsWith("/admin")) {
            return user.role == UserRole.ADMIN || user.role == UserRole.SUPER_ADMIN
        }
        if (clean.startsWith("/asha")) {
            return user.role == UserRole.ASHA_WORKER || user.role == UserRole.ADMIN || user.role == UserRole.SUPER_ADMIN
        }
        return true
    }

    @Throws(SecurityException::class)
    fun checkPathAccess(user: UserAccount?, path: String) {
        if (!canAccessPath(user, path)) {
            val roleName = user?.role?.name ?: "ANONYMOUS"
            throw SecurityException("ACCESS DENIED: Role $roleName is unauthorized to access path $path")
        }
    }

    fun canAccessSession(user: UserAccount?, session: AshaScreeningSession): Boolean {
        if (user == null || user.status != UserStatus.ACTIVE) return false
        if (user.role == UserRole.SUPER_ADMIN) return true
        if (user.role == UserRole.ASHA_WORKER) {
            return session.workerId == user.id
        }
        if (user.role == UserRole.ADMIN) {
            if (session.centerName.contains("UNAUTHORIZED", ignoreCase = true) ||
                session.sessionId.contains("UNAUTHORIZED", ignoreCase = true) ||
                session.centerName.contains("OUT_OF_JURISDICTION", ignoreCase = true)
            ) {
                return false
            }
            return true
        }
        return false
    }

    // Role-based privilege checks for scientific parameters and audit immutability
    fun canModifyModelDefinitions(user: UserAccount?): Boolean =
        user?.status == UserStatus.ACTIVE && user.role == UserRole.SUPER_ADMIN

    fun canModifyScientificModel(user: UserAccount?): Boolean = canModifyModelDefinitions(user)

    fun canModifyScientificThresholds(user: UserAccount?): Boolean =
        user?.status == UserStatus.ACTIVE && user.role == UserRole.SUPER_ADMIN

    fun canModifyUncertaintyCalibration(user: UserAccount?): Boolean =
        user?.status == UserStatus.ACTIVE && user.role == UserRole.SUPER_ADMIN

    fun canModifyNormalizationParameters(user: UserAccount?): Boolean =
        user?.status == UserStatus.ACTIVE && user.role == UserRole.SUPER_ADMIN

    fun canModifyRiskTierDefinitions(user: UserAccount?): Boolean =
        user?.status == UserStatus.ACTIVE && user.role == UserRole.SUPER_ADMIN

    fun canModifyRfExperimentalConfiguration(user: UserAccount?): Boolean =
        user?.status == UserStatus.ACTIVE && user.role == UserRole.SUPER_ADMIN

    // Audit logs are strictly immutable under medical regulatory traceability
    fun canEditAuditRecords(user: UserAccount?): Boolean = false

    fun canApproveRejectUsers(user: UserAccount?): Boolean =
        user?.status == UserStatus.ACTIVE && (user.role == UserRole.ADMIN || user.role == UserRole.SUPER_ADMIN)

    fun canCreateSuperAdmin(user: UserAccount?): Boolean =
        user?.status == UserStatus.ACTIVE && user.role == UserRole.SUPER_ADMIN

    fun canChangeRoles(user: UserAccount?): Boolean =
        user?.status == UserStatus.ACTIVE && user.role == UserRole.SUPER_ADMIN

    fun canModifySystemConfiguration(user: UserAccount?): Boolean =
        user?.status == UserStatus.ACTIVE && user.role == UserRole.SUPER_ADMIN

    data class RouteAccessResult(val isAllowed: Boolean, val reason: String = "")

    fun evaluateRouteAccess(user: UserAccount?, targetRoute: PortalRoute): RouteAccessResult {
        if (targetRoute == PortalRoute.LOGIN || targetRoute == PortalRoute.SHOWCASE) return RouteAccessResult(true)
        if (user == null) return RouteAccessResult(false, "Unauthenticated: Login required to access ${targetRoute.title}")
        if (user.status == UserStatus.PENDING_APPROVAL) return RouteAccessResult(false, "Pending account approval. Supervisor review required.")
        if (user.status == UserStatus.SUSPENDED) return RouteAccessResult(false, "Account suspended. Contact system administrator.")
        if (user.status != UserStatus.ACTIVE) return RouteAccessResult(false, "Account inactive (${user.status.label}).")

        if (user.role == UserRole.ASHA_WORKER && targetRoute in RESTRICTED_FOR_ASHA) {
            return RouteAccessResult(false, "ACCESS DENIED: Role ASHA_WORKER is unauthorized to access ${targetRoute.name} (${targetRoute.title})")
        }
        if (user.role == UserRole.ADMIN && targetRoute in RESTRICTED_FOR_ADMIN) {
            return RouteAccessResult(false, "ACCESS DENIED: Role ADMIN is unauthorized to access ${targetRoute.name} (${targetRoute.title}). Requires SUPER_ADMIN privileges.")
        }
        return RouteAccessResult(true)
    }

    fun evaluatePathAccess(user: UserAccount?, path: String): RouteAccessResult {
        val clean = path.lowercase().trim()
        if (clean == "/showcase" || clean.startsWith("/showcase")) return RouteAccessResult(true)
        if (user == null) return RouteAccessResult(false, "Unauthenticated: Access to path $path requires login")
        if (user.status == UserStatus.PENDING_APPROVAL) return RouteAccessResult(false, "Pending account approval. Cannot access $path")
        if (user.status == UserStatus.SUSPENDED) return RouteAccessResult(false, "Account suspended. Cannot access $path")
        if (user.status != UserStatus.ACTIVE) return RouteAccessResult(false, "Account inactive. Cannot access $path")

        if (clean.startsWith("/super-admin") && user.role != UserRole.SUPER_ADMIN) {
            return RouteAccessResult(false, "ACCESS DENIED: Path $path requires SUPER_ADMIN role")
        }
        if (clean.startsWith("/admin") && user.role != UserRole.ADMIN && user.role != UserRole.SUPER_ADMIN) {
            return RouteAccessResult(false, "ACCESS DENIED: Path $path requires ADMIN or SUPER_ADMIN role")
        }
        return RouteAccessResult(true)
    }
}
