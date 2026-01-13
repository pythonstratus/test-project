# ENTITY RBAC Service - Master API Documentation

> Complete API Reference for the ENTITY RBAC (Role-Based Access Control) Service

**Version:** 2.0 | **Last Updated:** January 2026 | **Base URL:** `/api/rbac`

---

## Complete Endpoint Summary

### Core RBAC Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/rbac/users/{seid}/profile` | Get complete user profile with all assignments |
| `GET` | `/api/rbac/users/{seid}/elevel` | Get user's ELEVEL with name and description |
| `GET` | `/api/rbac/elevel/definitions` | Get all ELEVEL definitions |
| `GET` | `/api/rbac/menu/{seid}/permissions` | Get all menu items with accessibility status |
| `GET` | `/api/rbac/menu/{seid}/accessible` | Get list of accessible menu IDs |
| `GET` | `/api/rbac/staff/{seid}` | Get staff info for user |
| `GET` | `/api/rbac/staff/{seid}/is-staff` | Check if user is staff |
| `POST` | `/api/rbac/staff/{seid}/org/{orgCode}` | Update staff ORG (CF, CP, WI, AD) |
| `GET` | `/api/rbac/staff/orgs` | Get available ORG codes |
| `GET` | `/api/rbac/hierarchy/{seid}` | Get hierarchy access info |
| `GET` | `/api/rbac/hierarchy/{seid}/areas` | Get accessible areas |
| `GET` | `/api/rbac/assignments/{seid}` | Get all assignments for user |
| `GET` | `/api/rbac/assignments/{seid}/current` | Get current active assignment |
| `POST` | `/api/rbac/assignments/{seid}/switch/{roid}` | Switch to different assignment |
| `GET` | `/api/rbac/assignments/{seid}/has-multiple` | Check if user has multiple assignments |

### Change Access Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/rbac/context/{seid}` | Get current user context |
| `POST` | `/api/rbac/context/{seid}/change` | Change access context |
| `POST` | `/api/rbac/context/{seid}/reset` | Reset to default context |
| `GET` | `/api/rbac/menu/{seid}/change-access-visible` | Check if Change Access should be visible |
| `GET` | `/api/rbac/hierarchy/{seid}/nav/areas` | Get all Areas for navigation |
| `GET` | `/api/rbac/hierarchy/{seid}/area/{areaCode}/territories` | Get Territories in Area |
| `GET` | `/api/rbac/hierarchy/{seid}/territory/{territoryCode}/groups` | Get Groups in Territory |
| `GET` | `/api/rbac/hierarchy/{seid}/group/{groupCode}/officers` | Get ROs in Group |
| `GET` | `/api/rbac/hierarchy/validate/{code}` | Validate hierarchy code |
| `GET` | `/api/rbac/hierarchy/{seid}/search?q={term}` | Search hierarchy by name/code |
| `GET` | `/api/rbac/organizations/{seid}` | Get available organizations |
| `POST` | `/api/rbac/organizations/{seid}/change/{orgCode}` | Change organization |

### Change Role Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/rbac/role/config/{seid}` | Get change role config (determines GENERAL vs STAFF mode) |
| `GET` | `/api/rbac/role/current/{seid}` | Get current role |
| `GET` | `/api/rbac/role/general/{seid}/options` | Get general user role options |
| `POST` | `/api/rbac/role/general/{seid}/change` | Change role (general user) |
| `GET` | `/api/rbac/role/staff/{seid}/options` | Get staff user options |
| `POST` | `/api/rbac/role/staff/{seid}/change` | Change role (staff user) |
| `GET` | `/api/rbac/role/validate/level-value?level=X&value=Y` | Validate level value |
| `DELETE` | `/api/rbac/role/staff/{seid}/default` | Clear saved default |
| `GET` | `/api/rbac/role/levels` | Get level dropdown options |
| `GET` | `/api/rbac/role/org-functions` | Get org/function dropdown options |

---

## ELEVEL Reference

| ELEVEL | Name | Data Access | Case Assignment | Realignment | Utilities |
|--------|------|-------------|-----------------|-------------|-----------|
| 0 | National | All Areas/PODs | ❌ | ✅ | ❌ |
| 2 | Area | All PODs in Area | ❌ | ✅ | ❌ |
| 4 | Territory | All PODs in Territory | ❌ | ✅ | ❌ |
| 6 | Group Manager | All in Group | ✅ | ❌ | ❌ |
| 7 | Acting GM | All in Group | ✅ | ❌ | ❌ |
| 8 | Employee | Own cases only | ❌ | ❌ | ❌ |
| Staff* | Any | Based on ELEVEL | - | ✅ | ✅ |

*Staff = ROID prefix 859062

---

## Key Business Rules

1. **Staff Detection:** Based on ROID prefix `859062`, NOT the ISSTAFF field
2. **Assignment Switch:** Two-step process (reset all → activate selected)
3. **Change Role Modes:**
   - General User: Single dropdown with available ROIDs
   - Staff User: Level + Assignment + Org dropdowns + Level Value text box
4. **Hierarchy Codes:** 8 digits, format determines level (e.g., XX000000 = Area)
5. **Valid Area Codes:** 21, 22, 23, 24, 25, 26, 27, 35

---

## Source Files

### Entities
- `EntityUser.java` - ENTITY_USER table
- `Entemp.java` - ENTEMP table (employee master)
- `Enttitles.java` - ENTTITLES table (ELEVEL lookup)

### Repositories
- `EntempRepository.java` - All ENTEMP queries
- `EntityUserRepository.java` - User queries
- `EnttitlesRepository.java` - ELEVEL lookup

### Services
- `UserService.java` - User profile aggregation
- `ELevelService.java` - ELEVEL operations
- `MenuPermissionService.java` - Menu access rules
- `StaffService.java` - Staff detection & ORG management
- `AssignmentService.java` - Assignment switching
- `HierarchyService.java` - Area/POD scoping
- `ChangeAccessService.java` - Context management
- `HierarchyNavigationService.java` - Drill-down navigation
- `ChangeRoleService.java` - Role change logic

### Controllers
- `RbacController.java` - Core endpoints
- `ChangeAccessController.java` - Hierarchy navigation
- `ChangeRoleController.java` - Role switching

### DTOs
- `RbacDTO.java` - Core DTOs
- `HierarchyDTO.java` - Change Access DTOs
- `ChangeRoleDTO.java` - Change Role DTOs

### Config
- `CorsConfig.java` - CORS configuration
- `SwaggerConfig.java` - OpenAPI configuration

### Exceptions
- `UserNotFoundException.java`
- `GlobalExceptionHandler.java`

---

## Usage Examples

### Get User Profile
```bash
curl -X GET "http://localhost:8383/entity/api/rbac/users/AB1CD/profile"
```

### Switch Assignment
```bash
curl -X POST "http://localhost:8383/entity/api/rbac/assignments/AB1CD/switch/859062001"
```

### Change Role (General User)
```bash
curl -X POST "http://localhost:8383/entity/api/rbac/role/general/VS123/change" \
  -H "Content-Type: application/json" \
  -d '{"roid": "25072330"}'
```

### Change Role (Staff User)
```bash
curl -X POST "http://localhost:8383/entity/api/rbac/role/staff/VS123/change" \
  -H "Content-Type: application/json" \
  -d '{
    "level": "AREA",
    "levelValue": "21",
    "assignmentRoid": "85906265",
    "orgFunction": "FC",
    "keepAsDefault": true
  }'
```

---

*Total Endpoints: 35*
