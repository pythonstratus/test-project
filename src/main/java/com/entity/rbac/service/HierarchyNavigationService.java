package com.entity.rbac.service;

import com.entity.rbac.dto.HierarchyDTO.*;
import com.entity.rbac.entity.Entemp;
import com.entity.rbac.repository.EntempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HierarchyNavigationService {
    private final EntempRepository entempRepository;
    private final ELevelService eLevelService;

    private static final Map<String, String> AREA_NAMES = Map.of(
        "21", "Area 21 - Northeast", "22", "Area 22 - Mid-Atlantic", "23", "Area 23 - Southeast",
        "24", "Area 24 - Central", "25", "Area 25 - Southwest", "26", "Area 26 - Western",
        "27", "Area 27 - Northwest", "35", "Area 35 - Special Operations"
    );

    public HierarchyListDTO getAreas(String seid) {
        Integer elevel = eLevelService.getELevel(seid);
        if (elevel > 0) return getAreasForLimitedUser(seid, elevel);

        List<Object[]> areaCounts = entempRepository.getAreaCountsWithTerritories();
        List<HierarchyNodeDTO> items = areaCounts.stream()
            .map(row -> {
                Integer areacd = ((Number) row[0]).intValue();
                String areaCode = String.format("%02d", areacd);
                return HierarchyNodeDTO.builder().code(areaCode + "000000").level("AREA")
                    .displayName(AREA_NAMES.getOrDefault(areaCode, "Area " + areaCode))
                    .childCount(((Number) row[1]).intValue()).elevelEquivalent(2).parentCode("00000000").build();
            }).sorted(Comparator.comparing(HierarchyNodeDTO::getCode)).collect(Collectors.toList());

        return HierarchyListDTO.builder().parentLevel("NATIONAL").parentCode("00000000")
            .childLevel("AREA").totalCount(items.size()).items(items).build();
    }

    public HierarchyListDTO getTerritories(String seid, String areaCode) {
        Integer areaCd = Integer.parseInt(areaCode.substring(0, 2));
        List<Object[]> territoryCounts = entempRepository.getTerritoryCountsForArea(areaCd);
        List<HierarchyNodeDTO> items = territoryCounts.stream()
            .map(row -> {
                String podPrefix = (String) row[0];
                String territoryCode = String.format("%02d", areaCd) + String.format("%-2s", podPrefix).replace(' ', '0').substring(0, 2) + "0000";
                return HierarchyNodeDTO.builder().code(territoryCode).level("TERRITORY")
                    .displayName("Territory " + territoryCode.substring(0, 4))
                    .childCount(((Number) row[1]).intValue()).elevelEquivalent(4).parentCode(areaCode).build();
            }).sorted(Comparator.comparing(HierarchyNodeDTO::getCode)).collect(Collectors.toList());

        return HierarchyListDTO.builder().parentLevel("AREA").parentCode(areaCode)
            .childLevel("TERRITORY").totalCount(items.size()).items(items).build();
    }

    public HierarchyListDTO getGroups(String seid, String territoryCode) {
        Integer areaCd = Integer.parseInt(territoryCode.substring(0, 2));
        String territoryPrefix = territoryCode.substring(2, 4);
        List<Object[]> groupCounts = entempRepository.getGroupCountsForTerritory(areaCd, territoryPrefix);
        List<HierarchyNodeDTO> items = groupCounts.stream()
            .map(row -> {
                String podcd = (String) row[0];
                String groupCode = String.format("%02d", areaCd) + String.format("%-4s", podcd).replace(' ', '0').substring(0, 4) + "00";
                return HierarchyNodeDTO.builder().code(groupCode).level("GROUP")
                    .displayName("Group " + groupCode.substring(0, 6))
                    .childCount(((Number) row[1]).intValue()).elevelEquivalent(6).parentCode(territoryCode.substring(0, 4) + "0000").build();
            }).sorted(Comparator.comparing(HierarchyNodeDTO::getCode)).collect(Collectors.toList());

        return HierarchyListDTO.builder().parentLevel("TERRITORY").parentCode(territoryCode)
            .childLevel("GROUP").totalCount(items.size()).items(items).build();
    }

    public HierarchyListDTO getRevenueOfficers(String seid, String groupCode) {
        Integer areaCd = Integer.parseInt(groupCode.substring(0, 2));
        String podcd = groupCode.substring(2, 6);
        List<Entemp> ros = entempRepository.findByAreacdAndPodcdStartingWith(areaCd, podcd);
        List<HierarchyNodeDTO> items = ros.stream()
            .filter(e -> e.getElevel() != null && e.getElevel() > -2)
            .map(e -> {
                String roCode = String.format("%02d", areaCd) + String.format("%-6s", e.getPodcd()).replace(' ', '0');
                if (roCode.length() < 8) roCode = roCode + String.format("%02d", e.getRoid() % 100);
                return HierarchyNodeDTO.builder().code(roCode.substring(0, 8)).level("RO")
                    .displayName(e.getName() != null ? e.getName().trim() : "RO " + e.getRoid())
                    .childCount(0).elevelEquivalent(8).parentCode(groupCode).build();
            }).sorted(Comparator.comparing(HierarchyNodeDTO::getDisplayName)).collect(Collectors.toList());

        return HierarchyListDTO.builder().parentLevel("GROUP").parentCode(groupCode)
            .childLevel("RO").totalCount(items.size()).items(items).build();
    }

    public Integer getChildCount(String code, String level) {
        if (code == null) return 0;
        try {
            return switch (level) {
                case "NATIONAL" -> entempRepository.countDistinctAreas();
                case "AREA" -> entempRepository.countTerritoriesInArea(Integer.parseInt(code.substring(0, 2)));
                case "TERRITORY" -> entempRepository.countGroupsInTerritory(Integer.parseInt(code.substring(0, 2)), code.substring(2, 4));
                case "GROUP" -> entempRepository.countROsInGroup(Integer.parseInt(code.substring(0, 2)), code.substring(2, 6));
                default -> 0;
            };
        } catch (Exception e) { return 0; }
    }

    public List<HierarchyNodeDTO> searchHierarchy(String seid, String searchTerm) {
        if (searchTerm == null || searchTerm.length() < 2) return Collections.emptyList();
        List<HierarchyNodeDTO> results = new ArrayList<>();
        List<Entemp> nameMatches = entempRepository.findByNameContainingIgnoreCase(searchTerm);
        for (Entemp e : nameMatches) {
            if (e.getElevel() != null && e.getElevel() > -2) {
                String code = buildCodeFromEntemp(e);
                results.add(HierarchyNodeDTO.builder().code(code).level("RO")
                    .displayName(e.getName().trim() + " (" + e.getTitle() + ")").childCount(0).elevelEquivalent(e.getElevel()).build());
            }
        }
        return results.stream().limit(20).collect(Collectors.toList());
    }

    private HierarchyListDTO getAreasForLimitedUser(String seid, Integer elevel) {
        Optional<Entemp> assignment = entempRepository.findCurrentActiveAssignment(seid);
        if (assignment.isEmpty()) return HierarchyListDTO.builder().parentLevel("NATIONAL").childLevel("AREA").totalCount(0).items(Collections.emptyList()).build();
        Entemp e = assignment.get();
        String areaCode = String.format("%02d", e.getAreacd());
        Integer territoryCount = entempRepository.countTerritoriesInArea(e.getAreacd());
        List<HierarchyNodeDTO> items = List.of(HierarchyNodeDTO.builder().code(areaCode + "000000").level("AREA")
            .displayName(AREA_NAMES.getOrDefault(areaCode, "Area " + areaCode)).childCount(territoryCount).elevelEquivalent(2).parentCode("00000000").build());
        return HierarchyListDTO.builder().parentLevel("NATIONAL").parentCode("00000000").childLevel("AREA").totalCount(1).items(items).build();
    }

    private String buildCodeFromEntemp(Entemp e) {
        String areaStr = e.getAreacd() != null ? String.format("%02d", e.getAreacd()) : "00";
        String podStr = e.getPodcd() != null ? String.format("%-6s", e.getPodcd().trim()).replace(' ', '0') : "000000";
        if (podStr.length() > 6) podStr = podStr.substring(0, 6);
        return areaStr + podStr;
    }
}
